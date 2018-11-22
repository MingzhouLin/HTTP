import RUDP.ClientUDP;
import RUDP.Packet;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static java.nio.channels.SelectionKey.OP_READ;

public class HttpLibrary {
    public static Charset utf8 = StandardCharsets.UTF_8;
    public CurlCommandLine curlCommandLine;

    public HttpLibrary(CurlCommandLine curlCommandLine) {
        this.curlCommandLine = curlCommandLine;
    }

//    public static Packet sendRequest(SocketAddress routerAddr, InetSocketAddress serverAddr, String payload){
//        try (DatagramChannel channel = DatagramChannel.open()) {
//            Packet p=new Packet.Builder()
//                    .setType(Packet.Type.SYN.getValue())
//                    .setSequenceNumber(1L) //Need more Consideration
//                    .setPortNumber(serverAddr.getPort())
//                    .setPeerAddress(serverAddr.getAddress())
//                    .setPayload(payload.getBytes())
//                    .create();
//            channel.send(p.toBuffer(), routerAddr);
//
//            channel.configureBlocking(false);
//            Selector selector = Selector.open();
//            channel.register(selector, OP_READ);
//            selector.select(5000);
//            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
//            buf.flip();
//            Packet recv= Packet.fromBuffer(buf);
//            return recv;
//        } catch (ClosedChannelException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public Response send(ClientUDP client) {
        Request request = curlCommandLine.request;
        String responseLine = "";
        try {
            client.send(request.toString());
            ByteBuffer buf = ByteBuffer.allocate(65534);
            client.receive(buf);
            responseLine = utf8.decode(buf).toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Response response = Tool.convertToResponse(responseLine);
        return response;
    }
}

class Tool {
    public static Response convertToResponse(String responseLine) {
        String[] seperate = responseLine.split("\r\n\r\n");
        if (seperate.length == 2) {
            return new Response(seperate[0], seperate[1]);
        } else if (seperate.length == 1) {
            return new Response(seperate[0], "");
        }
        return new Response();
    }

//    public static boolean ifRedirection(Response response){
//        String[] elements = response.header.split("\r\n");
//        int statusCode = Integer.parseInt(elements[0].substring(9, 12));
//        if (statusCode > 300 && statusCode < 400) return true;
//        return false;
//    }
//
//    public static Response redirect(Response response) throws IOException {
//        String[] elements = response.header.split("\r\n");
//        String redirectLocation = "";
//        for (String element :
//                elements) {
//            if (element.contains("Location: ")) {
//                redirectLocation = element.substring(element.indexOf(":") + 1);
//                break;
//            }
//        }
//        System.out.println(response);
//        System.out.println("The page had been redirected to : " + redirectLocation + "\n");
//        String redirectResponse= HttpLibrary.sendRequest(Tool.buildRequest(redirectLocation));
//        return convertToResponse(redirectResponse);
//    }

    public static Request buildRequest(String url) {
        String host = httpc.getHostFromUrl(url);
        return new Request("get", host, httpc.getPathFromUrl(url, host), new ArrayList<String>());
    }
}