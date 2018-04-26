package backend.entity.response;

public class TestConnectionResponse extends CommonResponse {

    private String fullName;

    public TestConnectionResponse() {
        super("TestConnectionResponse");
    }

    public TestConnectionResponse(String fullName) {
        super("TestConnectionResponse");
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
