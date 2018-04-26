package backend.entity.response;

public class CommonResponse {

    protected String info;

    public CommonResponse() {}

    public CommonResponse(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
