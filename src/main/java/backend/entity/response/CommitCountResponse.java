package backend.entity.response;

public class CommitCountResponse extends JsonResponse{

    public CommitCountResponse() {
        super();
        setInfo("CommitCountResponse");
    }

    public CommitCountResponse(String jsonInfo) {
        super();
        setInfo("CommitCountResponse");
        setJsonInfo(jsonInfo);
    }

}
