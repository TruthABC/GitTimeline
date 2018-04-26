package backend.entity.message;

public class ProjectMessage {

    private String projectName;

    public ProjectMessage() {}

    public ProjectMessage(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
}
