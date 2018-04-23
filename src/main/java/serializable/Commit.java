package serializable;

import java.io.Serializable;
import java.util.Date;

public class Commit implements Serializable {

    private int commitIndex;
    private String commitName;
    private Date commitTime;
    private String commitShortMessage;
    private String commitFullMessage;

    public Commit(int commitIndex, String commitName, Date commitTime, String commitShortMessage, String commitFullMessage) {
        this.commitIndex = commitIndex;
        this.commitName = commitName;
        this.commitTime = commitTime;
        this.commitShortMessage = commitShortMessage;
        this.commitFullMessage = commitFullMessage;
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public String getCommitName() {
        return commitName;
    }

    public void setCommitName(String commitName) {
        this.commitName = commitName;
    }

    public Date getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(Date commitTime) {
        this.commitTime = commitTime;
    }

    public String getCommitShortMessage() {
        return commitShortMessage;
    }

    public void setCommitShortMessage(String commitShortMessage) {
        this.commitShortMessage = commitShortMessage;
    }

    public String getCommitFullMessage() {
        return commitFullMessage;
    }

    public void setCommitFullMessage(String commitFullMessage) {
        this.commitFullMessage = commitFullMessage;
    }
}
