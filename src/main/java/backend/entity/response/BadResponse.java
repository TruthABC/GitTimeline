package backend.entity.response;

public class BadResponse extends CommonResponse {

    private String badInfo;

    public BadResponse() {
        super("BadResponse");
    }

    public BadResponse(String badInfo) {
        super("BadResponse");
        this.badInfo = badInfo;
    }

    public String getBadInfo() {
        return badInfo;
    }

    public void setBadInfo(String badInfo) {
        this.badInfo = badInfo;
    }
}
