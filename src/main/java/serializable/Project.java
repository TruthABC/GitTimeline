package serializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Project implements Serializable {

    private String projectUrl;
    private String pureName;
    private String projectPath;
    private Date lastUpdate;
    private ArrayList<Commit> commitList;

    public Project(String projectUrl, String pureName, String projectPath, Date lastUpdate, ArrayList<Commit> commitList) {
        this.projectUrl = projectUrl;
        this.pureName = pureName;
        this.projectPath = projectPath;
        this.lastUpdate = lastUpdate;
        this.commitList = commitList;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getPureName() {
        return pureName;
    }

    public void setPureName(String pureName) {
        this.pureName = pureName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ArrayList<Commit> getCommitList() {
        return commitList;
    }

    public void setCommitList(ArrayList<Commit> commitList) {
        this.commitList = commitList;
    }
}
