package backend.entity.response;

public class ProgressResponse extends CommonResponse {

    private int progressNow;
    private int progressTarget;
    private String progressInfo;

    public ProgressResponse() {
        super("ProgressResponse");
    }

    public ProgressResponse(int progressNow, int progressTarget, String progressInfo) {
        super("ProgressResponse");
        this.progressNow = progressNow;
        this.progressTarget = progressTarget;
        this.progressInfo = progressInfo;
    }

    public int getProgressNow() {
        return progressNow;
    }

    public void setProgressNow(int progressNow) {
        this.progressNow = progressNow;
    }

    public int getProgressTarget() {
        return progressTarget;
    }

    public void setProgressTarget(int progressTarget) {
        this.progressTarget = progressTarget;
    }

    public String getProgressInfo() {
        return progressInfo;
    }

    public void setProgressInfo(String progressInfo) {
        this.progressInfo = progressInfo;
    }
}
