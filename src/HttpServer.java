import joptsimple.OptionParser;
import joptsimple.OptionSet;
import sun.dc.pr.PRError;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

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
//                    System.out.println(request);
                    Content content = new Content();
                    String[] firstLine = request.split("\r\n")[0].split(" ");
                    if (request.contains("\r\n\r\n")) {
                        content.text = request.split("\r\n\r\n")[1];
                    }
                    content.type = Type.GET.equals(firstLine[0]) ? Type.GET : Type.POST;
                    content.path = firstLine[1];
                    String responds = process(content);
                    buf = utf8.encode(responds);
                    buf.flip();
                    client.write(buf);
                    buf.clear();
                }
            }
        } catch (IOException e) {
            System.out.println("error");
        }
    }

    private String process(Content content) {
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

    private String readFile(String path) {
        File file = new File(directoryPath + path);
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != "") {
                builder.append(line + "\r\n");
                line = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            return "HTTP ERROR 404";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    private String writeFile(String content, String path) {
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
        return "Ok 200";
    }

    private String readFileList() {
        StringBuilder builder = new StringBuilder();
        File file = new File(directoryPath);
        File[] tempList = file.listFiles();
        for (File f :
                tempList) {
            if (f.isFile()) {
                builder.append(f.toString() + "\r\n");
            }
        }
        return builder.toString();
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
        OptionParser parser = new OptionParser();
        parser.accepts("v");
        parser.accepts("p").withOptionalArg().ofType(Integer.class).defaultsTo(8080);
        parser.accepts("d").withRequiredArg().ofType(String.class).defaultsTo("file");
        OptionSet opts = parser.parse(options);
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
