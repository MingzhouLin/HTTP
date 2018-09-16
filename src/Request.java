import java.util.ArrayList;

public class Request {
    public int port;
    public String host;
    public String path;
    public String httpVersionInfo;
    public String queryParameter;
    public ArrayList<String> requestHeader;
    public String requestBody;

    public Request(){}

    public Request(int port, String host, String queryParameter, ArrayList<String> requestHeader, String requestBody) {
        this.port = port;
        this.host = host;
        this.queryParameter = queryParameter;
        this.requestHeader = requestHeader;
        this.requestBody = requestBody;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    public ArrayList<String> getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(ArrayList<String> requestHeader) {
        this.requestHeader = requestHeader;
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

    public String getHttpVersionInfo() {
        return httpVersionInfo;
    }

    public void setHttpVersionInfo(String httpVersionInfo) {
        this.httpVersionInfo = httpVersionInfo;
    }
}
