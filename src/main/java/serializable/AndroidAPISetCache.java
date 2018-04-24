package serializable;

import java.io.Serializable;
import java.util.HashSet;

public class AndroidAPISetCache implements Serializable {

    public static final transient String CACHE_FILE_NAME = "AndroidAPISetCache";//".aas" = AndroidAPISetCache
    private HashSet<String> set;

    public AndroidAPISetCache(HashSet<String> set) {
        this.set = set;
    }

    public HashSet<String> getSet() {
        return set;
    }

    public void setSet(HashSet<String> set) {
        this.set = set;
    }
}
