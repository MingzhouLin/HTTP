import java.util.ArrayList;

public class CurlCommandLine {
    public final boolean valid = true;
    public int serverPort = 8007;
    public int routerPort = 3000;
    public String requestType;
    public boolean verbose;
    public boolean haveHeaders;
    public ArrayList<String> Headers;
    public boolean haveInlineData;
    public String inlineData;
    public boolean haveFile;
    public String file;
    public String url;
    public boolean isHelp;
    public boolean output;
    public String outputFile;

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public Request request;

    public CurlCommandLine() {
    }

    public boolean isValid() {
        return valid;
    }


    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
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

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isHaveHeaders() {
        return haveHeaders;
    }

    public void setHaveHeaders(boolean haveHeaders) {
        this.haveHeaders = haveHeaders;
    }

    public ArrayList<String> getHeaders() {
        return Headers;
    }

    public void setHeaders(ArrayList<String> headers) {
        Headers = headers;
    }

    public boolean isHaveInlineData() {
        return haveInlineData;
    }

    public void setHaveInlineData(boolean haveInlineData) {
        this.haveInlineData = haveInlineData;
    }

    public String getInlineData() {
        return inlineData;
    }

    public void setInlineData(String inlineData) {
        this.inlineData = inlineData;
    }

    public boolean isHaveFile() {
        return haveFile;
    }

    public void setHaveFile(boolean haveFile) {
        this.haveFile = haveFile;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public boolean isHelp() {
        return isHelp;
    }

    public void setHelp(boolean help) {
        isHelp = help;
    }

    public boolean isOutput() {
        return output;
    }

    public void setOutput(boolean output) {
        this.output = output;
    }
}
