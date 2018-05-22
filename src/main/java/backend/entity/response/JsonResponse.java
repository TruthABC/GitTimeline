package backend.entity.response;

public class JsonResponse extends CommonResponse  {

    private String jsonInfo;

    public JsonResponse() {
        super("JsonResponse");
    }

    public JsonResponse(String jsonInfo) {
        super("JsonResponse");
        this.jsonInfo = jsonInfo;
    }

    public String getJsonInfo() {
        return jsonInfo;
    }

    public void setJsonInfo(String jsonInfo) {
        this.jsonInfo = jsonInfo;
    }

}
