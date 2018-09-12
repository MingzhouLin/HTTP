import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {
    public static final int PORT_NUMBER=80;
    public static Charset utf8 = StandardCharsets.UTF_8;

    public Request cmdParser(String cmd) {
        return new Request();
    }

    public static void sendRequest(Request request) throws IOException {
        SocketAddress endpoint = new InetSocketAddress(request.host, PORT_NUMBER);
        try (SocketChannel socket = SocketChannel.open()) {
            socket.connect(endpoint);
            ByteBuffer buf = utf8.encode(request.content);
            int n = socket.write(buf);
            buf.clear();

            readFully(socket, buf, n);
        }
    }

    // readFully reads until the request is fulfilled or the socket is closed
    private static void readFully(SocketChannel socket, ByteBuffer buf, int size) throws IOException {
        buf = ByteBuffer.allocate(1024);
        socket.read(buf);
        buf.flip();
        System.out.println("Replied: " + utf8.decode(buf));
    }

    public Request test () {
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
        sendRequest(request);
    }
}
