import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

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

    public Response send(){
        Request request = curlCommandLine.request;
        String responseLine= null;
        try {
            responseLine = sendRequest(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Response response = Tool.convertToResponse(responseLine);
//        while (Tool.ifRedirection(response)){
//            response=Tool.redirect(response);
//        }
        return response;
    }
}

class Tool {
    public static Response convertToResponse(String responseLine) {
        String[] seperate = responseLine.split("\r\n\r\n");
        if (seperate.length==2) {
            return new Response(seperate[0], seperate[1]);
        }else if (seperate.length==1){
            return new Response(seperate[0], "");
        }
        return new Response();
    }

    public static boolean ifRedirection(Response response){
        String[] elements = response.header.split("\r\n");
        int statusCode = Integer.parseInt(elements[0].substring(9, 12));
        if (statusCode > 300 && statusCode < 400) return true;
        return false;
    }

    public static Response redirect(Response response) throws IOException {
        String[] elements = response.header.split("\r\n");
        String redirectLocation = "";
        for (String element :
                elements) {
            if (element.contains("Location: ")) {
                redirectLocation = element.substring(element.indexOf(":") + 1);
                break;
            }
        }
        System.out.println(response);
        System.out.println("The page had been redirected to : " + redirectLocation + "\n");
        String redirectResponse= HttpLibrary.sendRequest(Tool.buildRequest(redirectLocation));
        return convertToResponse(redirectResponse);
    }

    public static Request buildRequest(String url){
        String host = httpc.getHostFromUrl(url);
        return new Request("get", host, httpc.getPathFromUrl(url, host), new ArrayList<String>());
    }
}