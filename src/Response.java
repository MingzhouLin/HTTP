public class Response {
    public String header;
    public String body;
    public String status;
    public String code;

    public Response() {
    }

    public Response(String header, String body) {
        this.header = header;
        this.body = body;
        this.status = header.split("\r\n")[0];
        this.code = this.status.split(" ")[1];
    }

    public String toString() {
        return header + "\r\n\r\n" + body;
    }
}
