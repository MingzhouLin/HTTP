import java.util.ArrayList;

public class Request {
    public int serverPort = 8007;
    public int routerPort = 3000;
    public String host;
    public String path;
    public final String httpVersionInfo = "HTTP/1.0";
    public String queryParameter;
    public ArrayList<String> requestHeaders;
    public String requestBody;
    //need a request operation.
    public String requestType;
    public final String endSign = "\r\n";

    public Request() {
    }

    public Request(String requestType, String host, String path, ArrayList<String> requestHeaders ) {
        this.requestType = requestType;
        this.host = host;
        this.path = path;
        this.requestHeaders = requestHeaders;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getRouterPort() {
        return routerPort;
    }

    public void setRouterPort(int routerPort) {
        this.routerPort = routerPort;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(String queryParameter) {
        this.queryParameter = queryParameter;
    }

    public ArrayList<String> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(ArrayList<String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String toString() {
        StringBuilder request = new StringBuilder();
        request.append(requestType.toUpperCase() + " " + path + " " + httpVersionInfo + endSign);
        request.append("Host:" + host + endSign);
        for (String header :
                requestHeaders) {
            request.append(header + endSign);
        }
        request.append(endSign);
        if (requestType.equals("post")) {
            request.append(requestBody + endSign);
        }
        return request.toString();
    }
}
