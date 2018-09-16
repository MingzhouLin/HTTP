public class Response {
    public String header;
    public String body;

    public Response(String header, String body) {
        this.header = header;
        this.body = body;
    }

    public String toString(){
        return header+body;
    }
}
