import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class httpfs {
    public static Charset utf8 = StandardCharsets.UTF_8;
    public int port;
    public boolean debugMessage;
    public String directoryPath;

    public httpfs(int port, boolean debugMessage, String directoryPath) {
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
                    Content content = constructContent(request);
                    CompletableFuture
                            .supplyAsync(() -> process(content))
                            .thenAccept(s -> {
                                String responds = constructResponse(s, content);
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

    private Content constructContent(String req) {
        synchronized (this) {
            Content content = new Content();
            content.request = req;
            if (debugMessage) {
                System.out.println(req + "\r\n");
            }
            String header = req.split("\r\n\r\n")[0];
            String[] firstLine = header.split("\r\n")[0].split(" ");
            content.operation = Operation.GET.equals(Operation.valueOf(firstLine[0].toUpperCase())) ? Operation.GET : Operation.POST;
            content.path = firstLine[1];
            String[] lines = header.split("\r\n");
            if (lines.length > 2) {
                content.hasHeader = true;
                for (int i = 2; i < lines.length; i++) {
                    content.headers.add(lines[i].trim());
                }
            }
            if (content.operation.equals(Operation.POST)) {
                content.text = req.split("\r\n\r\n")[1];
            }
            return content;
        }
    }

    private String constructResponse(String body, Content content) {
        synchronized (this) {
            StringBuilder builder = new StringBuilder();
            String header = headerOfResponds(body, content);
            builder.append(header);
            builder.append(body);
            if (debugMessage) {
                System.out.println(builder.toString() + "\r\n");
            }
            return builder.toString();
        }
    }

    private String headerOfResponds(String body, Content content) {
        StringBuilder builder = new StringBuilder();
        if (body.contains("Error")) {
            builder.append("HTTP/1.0 " + body.substring(body.indexOf(":") + 1) + "\r\n");
        } else {
            builder.append("HTTP/1.0 200 OK\r\n");
        }
        builder.append("Connection: keep-alive\r\n");
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy  HH:mm:ss");
        builder.append("Date: " + df.format(new Date()) + "\r\n");
        builder = addContentType(builder, content);
        builder.append("Content-Length:" + body.length() + "\r\n");
        builder.append("\r\n");
        return builder.toString();
    }

    private StringBuilder addContentType(StringBuilder builder, Content content) {
        String path = content.path;
        if (content.operation.equals(Operation.GET) && !path.equals("/")) {
            switch (path.substring(path.indexOf(".") + 1)) {
                case "html": {
                    builder.append("Content-Type: text/html\r\n");
                    builder.append("Content-Disposition: inline\r\n");
                    break;
                }
                case "json": {
                    builder.append("Content-Type: application/json\r\n");
                    builder.append("Content-Disposition: inline\r\n");
                    break;
                }
                case "txt": {
                    builder.append("Content-Type: text/plain\r\n");
                    builder.append("Content-Disposition: inline\r\n");
                    break;
                }
                default: {
                    builder.append("Content-Type: text/plain\r\n");
                    builder.append("Content-Disposition: attachment\r\n");
                }
            }
        } else {
            builder.append("Content-Type: text/plain\r\n");
            builder.append("Content-Disposition: attachment\r\n");
        }
        return builder;
    }

    private String process(Content content) {
        if (content.operation.equals(Operation.GET)) {
            if (content.path.equals("/")) {
                return readFileList();
            } else {
                if (startWithDot(content.path)) {
                    String errorMessage = "Error:401 Unauthorized";
                    if (debugMessage) {
                        System.out.println(errorMessage);
                        System.out.println("Error Request: " + content.request);
                    }
                    return errorMessage;
                } else {
                    return readFile(content);
                }
            }
        } else {
            return writeFile(content.text, content.path);
        }
    }

    private boolean startWithDot(String path){
        String pattern = "/\\..*";
        boolean isMatch = Pattern.matches(pattern, path);
        return isMatch;
    }

    private String readFile(Content content) {
        synchronized (this) {
            File file = new File(directoryPath + content.path);
            StringBuilder builder = new StringBuilder();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine();
                while (line != null) {
                    builder.append(line + "\r\n");
                    line = reader.readLine();
                }
            } catch (FileNotFoundException e) {
                String errorMessage = "Error:404 Not Found";
                if (debugMessage) {
                    System.out.println(errorMessage);
                    System.out.println("Error Reqeuest: " + content.request);
                }
                return errorMessage;
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (debugMessage) {
                System.out.println("The content of " + directoryPath + content.path + " has been read");
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
                return "Error:500 Internal Service Error";
            }
            if (debugMessage) {
                System.out.println("Created a new file " + directoryPath + path);
            }
            return content;
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
        httpfs server = new httpfs((int) opts.valueOf("p"), (boolean) opts.has("v"), (String) opts.valueOf("d"));
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
        parser.accepts("d").withRequiredArg().ofType(String.class).defaultsTo("/Users/linmingzhou/Documents/Concordia/Comp 6461/HTTP");
        OptionSet opts = parser.parse(options.split(" "));
        return opts;
    }
}

class Content {
    public String request;
    public Operation operation;
    public String path;
    public String text;
    public boolean hasHeader;
    public LinkedList<String> headers = new LinkedList<>();
}

enum Operation {
    GET, POST
}