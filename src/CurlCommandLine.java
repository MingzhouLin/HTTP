import java.util.ArrayList;

public class CurlCommandLine {
    public boolean valid;
    public String requestType;
    public boolean verbose;
    public boolean haveHeaders;
    public ArrayList<String> Headers;
    public boolean haveInlineData;
    public String inlineData;
    public boolean haveFile;
    public String file;
    public String url;

    public CurlCommandLine() {
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
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
}
