import java.util.Set;

public class Request {
    public String host;
    public String content;
    //This is for additional condition such as -v -d -h ...
    public Set<String> addition;
    public Request() {
    }

    public Request(String host, String content) {
        this.host = host;
        this.content = content;
    }
}
