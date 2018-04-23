package serializable;

import java.io.Serializable;
import java.util.HashMap;

public class APICounter implements Serializable {

    private int commitIndex;
    private HashMap<String, Long> apiFrequency;

    public APICounter(int commitIndex, HashMap<String, Long> apiFrequency) {
        this.commitIndex = commitIndex;
        this.apiFrequency = apiFrequency;
    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(int commitIndex) {
        this.commitIndex = commitIndex;
    }

    public HashMap<String, Long> getApiFrequency() {
        return apiFrequency;
    }

    public void setApiFrequency(HashMap<String, Long> apiFrequency) {
        this.apiFrequency = apiFrequency;
    }
}
