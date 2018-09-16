import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.SQLOutput;
import java.util.Scanner;

public class Client {
    public static final int PORT_NUMBER = 80;
    public static Charset utf8 = StandardCharsets.UTF_8;

    public static SocketChannel sendRequest(Request request) throws IOException {
        SocketAddress endpoint = new InetSocketAddress(request.host, PORT_NUMBER);
        try (SocketChannel socket = SocketChannel.open()) {
            socket.connect(endpoint);
            ByteBuffer buf = utf8.encode(request.content);
            socket.write(buf);
            buf.clear();

            return socket;
        }
    }

    // readFully reads until the request is fulfilled or the socket is closed
    public static String readFully(SocketChannel socket) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(1024);
        socket.read(buf);
        buf.flip();
        return utf8.decode(buf).toString();
    }

    public Request test() {
        StringBuffer content = new StringBuffer();
        content.append("GET /status/418 HTTP/1.0\r\n");
        content.append("Host:httpbin.org\r\n");
        content.append("\r\n");
        String host = "httpbin.org";

        Request request = new Request(host, content.toString());
        return request;
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        String cmd = scanner.nextLine();
        Client client = new Client();
//        Request request = client.cmdParser(cmd);
        Request request = client.test();
        SocketChannel socket = sendRequest(request);
        String response = readFully(socket);
        String result = Tool.handleResponse(request, response);
        System.out.println("Reply:" + result);
    }
}

class Tool {
    public static String handleResponse(Request request, String response) throws IOException {
        Client client = new Client();
        if (request.addition.contains("-v")) {
            return response;
        } else {
            /*
                Redirection.
             */
            String[] elements = response.split("\r\n");
            int statusCode = Integer.parseInt(elements[1].substring(9, 12));
            if (statusCode > 300 && statusCode < 400) {
                String redirectLocation = "";
                for (String element :
                        elements) {
                    if (element.contains("Location: ")) {
                        redirectLocation = element.substring(element.indexOf(":") + 1);
                        break;
                    }
                }
                System.out.println(response);
                System.out.println("The page had been moved. Redirect to : " + redirectLocation + "\n");
                //TODO: Need a common function to build Request.
                SocketChannel socket = Client.sendRequest(new Request());
                return Client.readFully(socket);
            } else if (statusCode > 200 && statusCode < 300) {
                //Without -v.
                return elements[elements.length - 1];
            }
        }
        return response;
    }

    public static Request cmdParser(String cmd) {
        return new Request();
        /*
            TODO: you need parse the cmd and store the useful info into Request.
            Notice that it's better to create a common request content building function which can be used to build redirection request.
         */
    }
}