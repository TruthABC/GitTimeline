package backend.entity.response;

public class GreetingResponse extends CommonResponse {

    private final long id;
    private final String content;

    public GreetingResponse(long id, String content) {
        super("GreetingResponse");
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}