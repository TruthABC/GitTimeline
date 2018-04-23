package serializable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class APICounters implements Serializable {

    private Date lastUpdate;
    private ArrayList<APICounter> apiCounterList;

    public APICounters(Date lastUpdate, ArrayList<APICounter> apiCounterList) {
        this.lastUpdate = lastUpdate;
        this.apiCounterList = apiCounterList;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public ArrayList<APICounter> getApiCounterList() {
        return apiCounterList;
    }

    public void setApiCounterList(ArrayList<APICounter> apiCounterList) {
        this.apiCounterList = apiCounterList;
    }
}
