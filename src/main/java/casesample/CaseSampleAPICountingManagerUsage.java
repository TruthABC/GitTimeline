package casesample;

import main.APICountingManager;
import main.ProjectManager;
import serializable.APICounter;
import serializable.APICounters;
import serializable.Commit;
import serializable.Project;

import java.io.IOException;

/**
 * 测试，试用APICountingManager对象
 */
public class CaseSampleAPICountingManagerUsage {

    private static final String projectUrl = "https://github.com/MrDoomy/Torch";
//    private static final String projectUrl = "https://github.com/sikaozhe1997/Xin-Yue";

    public static void main(String[] args) {
        Project p = loadOrDownloadProject();
        countAPI(p);
    }

    private static Project loadOrDownloadProject() {
        ProjectManager pm = new ProjectManager(projectUrl);
        Project p;

        System.out.println("--- After ProjectManager Constructed ---");
        System.out.println("pm.isProjectCached() - " + pm.isProjectCached());

        if (!pm.isProjectCached()) {
            pm.downloadProject();
        }
        p = pm.getProjectCache();

        System.out.println("--- After Project Ready ---");
        System.out.println("p.getProjectUrl() - " + p.getProjectUrl());
        System.out.println("p.getProjectPureName() - " + p.getPureName());
        System.out.println("p.getProjectPath() - " + p.getProjectPath());
        System.out.println("p.getLastUpdate() - " + p.getLastUpdate());
        for (Commit c : p.getCommitList()) {
            System.out.println("[commit - " + c.getCommitIndex() + "] " + c.getCommitShortMessage());
        }

        return p;
    }

    private static void countAPI(Project p) {
        APICountingManager am = new APICountingManager(p);
        System.out.println("--- After APICountingManager Constructed ---");

        System.out.println("am.getCountingNow() = " + am.getCountingNow());
        System.out.println("p.getCommitList().size() = " + p.getCommitList().size());

//        am.deleteCountingCache();

        while (am.hasNextCommit()) {
            am.analyseNextCommit();
        }

        System.out.println("--- After All Commits Analysed ---");
        System.out.println("am.getNewCountingCount() = " + am.getNewCountingCount());
        System.out.println("am.getCheckoutFault() = " + am.getCheckoutFault());
        System.out.println("am.getCountAPIFault() = " + am.getCountAPIFault());

        System.out.println("am.getCountingNow() = " + am.getCountingNow());
        System.out.println("p.getCommitList().size() = " + p.getCommitList().size());

        System.out.println("am.hasNextCommit() = " + am.hasNextCommit());

        APICounters a = am.getCountingCache();
        System.out.println("a.getLastUpdate() = " + a.getLastUpdate());
        for (APICounter aa: a.getApiCounterList()) {
            System.out.println("    aa.getCommitIndex() = " + aa.getCommitIndex());
            for (String s: aa.getApiFrequency().keySet()) {
                System.out.println("        s = " + s);
                System.out.println("        aa.getApiFrequency().get(s) = " + aa.getApiFrequency().get(s));
            }
        }

        if (am.getNewCountingCount() > 0) {
            try {
                am.saveCountingCache();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

}
