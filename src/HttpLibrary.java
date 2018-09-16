import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class HttpLibrary {
    public static Charset utf8 = StandardCharsets.UTF_8;
    public CurlCommandLine curlCommandLine;

    public HttpLibrary(CurlCommandLine curlCommandLine) {
        this.curlCommandLine = curlCommandLine;
    }

    public static String sendRequest(Request request) throws IOException {
        System.out.println(request);
        SocketAddress endpoint = new InetSocketAddress(request.host, request.port);
        try (SocketChannel socket = SocketChannel.open()) {
            socket.connect(endpoint);
            ByteBuffer buf = utf8.encode(request.toString());
            socket.write(buf);
            buf.clear();

            buf = ByteBuffer.allocate(1024);
            socket.read(buf);
            buf.flip();
            return utf8.decode(buf).toString();
        }
    }

    public String send() throws IOException {
        Request request = curlCommandLine.request;
        String responseLine= sendRequest(request);
        Response response = Tool.convertToResponse(responseLine);
        String result = Tool.handleResponse(curlCommandLine, response);
        return result;
    }
}

class Tool {
    public static Response convertToResponse(String responseLine) {
        String[] seperate = responseLine.split("\r\n\r\n");
        Response response = new Response(seperate[0], seperate[1]);
        return response;
    }

    public static String handleResponse(CurlCommandLine curlCommandLine, Response response) throws IOException {
        if (curlCommandLine.verbose) {
            return response.toString();
        } else {
            /*
                Redirection.
             */
            String[] elements = response.header.split("\r\n");
            int statusCode = Integer.parseInt(elements[0].substring(9, 12));
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
                return HttpLibrary.sendRequest(Tool.buildRequest(redirectLocation));
            } else if (statusCode >= 200 && statusCode < 300) {
                //Without -v.
                return response.body;
            }
        }
        return response.toString();
    }

    public static Request buildRequest(String url){
        String host = HttpClient.getHostFromUrl(url);
        return new Request("get", host, HttpClient.getPathFromUrl(url, host), new ArrayList<String>());
    }

}