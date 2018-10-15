import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class HttpServer {
    public static Charset utf8 = StandardCharsets.UTF_8;
    public int port;
    public boolean debugMessage;
    public String directoryPath;

    public HttpServer(int port, boolean debugMessage, String directoryPath) {
        this.port = port;
        this.debugMessage = debugMessage;
        this.directoryPath = directoryPath;
    }

    private void handleRequest(SocketChannel socket) {
        try (SocketChannel client = socket) {
            ByteBuffer buf = ByteBuffer.allocate(1024);
            while (true) {
                int nr = client.read(buf);

                if (nr == -1)
                    break;

                if (nr > 0) {
                    buf.flip();
                    String request = utf8.decode(buf).toString();
                    buf.clear();
                    Content content = new Content();
                    String[] firstLine = request.split("\r\n")[0].split(" ");
                    content.type = Type.GET.equals(Type.valueOf(firstLine[0].toUpperCase())) ? Type.GET : Type.POST;
                    content.path = firstLine[1];
                    if (content.type.equals(Type.POST)) {
                        content.text = request.split("\r\n\r\n")[1];
                    }
                    CompletableFuture
                            .supplyAsync(() -> process(content))
                            .thenAccept(s -> {
                                String responds = constructRespond(s);
                                ByteBuffer buff = utf8.encode(responds);
                                try {
                                    client.write(buff);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                buff.clear();
                            });
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String constructRespond(String body) {
        synchronized (this) {
            StringBuilder builder = new StringBuilder();
            if (body.contains("500") || body.contains("404")) {
                builder.append(body + "\r\n\r\n");
                builder.append(body);
                return builder.toString();
            } else {
                builder.append("Ok 200\r\n\r\n");
                builder.append(body);
                return builder.toString();
            }
        }
    }

    private String process(Content content) {
        synchronized (this) {
            if (content.type.equals(Type.GET)) {
                if (content.path.equals("/")) {
                    return readFileList();
                } else {
                    return readFile(content.path);
                }
            } else {
                return writeFile(content.text, content.path);
            }
        }
    }

    private String readFile(String path) {
        synchronized (this) {
            File file = new File(directoryPath + path);
            StringBuilder builder = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line + "\r\n");
                    line = reader.readLine();
                }
            } catch (FileNotFoundException e) {
                return "HTTP ERROR 404";
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (debugMessage) {
                System.out.println("The content of " + directoryPath + path + " has been read");
            }
            return builder.toString();
        }
    }

    private String writeFile(String content, String path) {
        synchronized (this) {
            File writename = new File(directoryPath + path);
            try {
                writename.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(writename));
                out.write(content);
                out.flush();
                out.close();
            } catch (IOException e) {
                return "HTTP ERROR 500";
            }
            if (debugMessage) {
                System.out.println("Created a new file " + directoryPath + path);
            }
            return "Content " + content + " is wrote";
        }
    }

    private String readFileList() {
        synchronized (this) {
            StringBuilder builder = new StringBuilder();
            File file = new File(directoryPath);
            File[] tempList = file.listFiles();
            for (File f :
                    tempList) {
                if (f.isFile()) {
                    builder.append(f.toString() + "\r\n");
                }
            }
            if (debugMessage) {
                System.out.println("The file list under directory " + directoryPath + " has been successfully read");
            }
            return builder.toString();
        }
    }

    private void listenAndServe() throws IOException {
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress(port));
            System.out.println("server is listening at " + port);
            while (true) {
                SocketChannel client = server.accept();
                // We may use a custom Executor instead of ForkJoinPool in a real-world application
                handleRequest(client);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String startLine = scanner.nextLine();
        OptionSet opts = parser(startLine);
        HttpServer server = new HttpServer((int) opts.valueOf("p"), (boolean) opts.has("v"), (String) opts.valueOf("d"));
        try {
            server.listenAndServe();
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    public static OptionSet parser(String options) {
        OptionParser parser = new OptionParser("vp::d:");
        parser.accepts("v").withOptionalArg();
        parser.accepts("p").withOptionalArg().ofType(Integer.class).defaultsTo(8080);
        parser.accepts("d").withRequiredArg().ofType(String.class).defaultsTo("directory");
        OptionSet opts = parser.parse(options.split(" "));
        return opts;
    }
}

class Content {
    public Type type;
    public String path;
    public String text;
}

enum Type {
    GET, POST
}
