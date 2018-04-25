package casesample;

import main.APICountingManager;
import main.ProjectManager;
import serializable.APICounter;
import serializable.APICounters;
import serializable.Commit;
import serializable.Project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 测试，试用APICountingManager对象
 */
public class CaseSampleAPICountingManagerUsage {

    private static final String projectUrl = "https://github.com/pinguo-yuyidong/Camera2";
//    private static final String projectUrl = "https://github.com/MrDoomy/Torch";
//    private static final String projectUrl = "https://github.com/sikaozhe1997/Xin-Yue";

    /**
     * 先确保项目存在；然后计数；然后读取缓存并操作（对比ProjectAPICounterNaive生成的"output.txt"文件）
     * @param args
     */
    public static void main(String[] args) throws IOException {
        Project p = loadOrDownloadProject(projectUrl);
        APICountingManager am = anylyseAllCommit(p);
        printToFile(am);
    }

    private static Project loadOrDownloadProject(String projectUrl) {
        ProjectManager pm = new ProjectManager(projectUrl);
        System.out.println("--- After ProjectManager Constructed ---");
        System.out.println("pm.isProjectCached() - " + pm.isProjectCached());

        if (!pm.isProjectCached()) {
            pm.downloadProject();
            System.out.println("--- After Project Downloaded ---");
            System.out.println("pm.isProjectCached() - " + pm.isProjectCached());
        }

        CaseSampleProjectManagerUsage.showProject(pm.getProjectCache());
        return pm.getProjectCache();
    }

    private static APICountingManager anylyseAllCommit(Project p) throws IOException {
        APICountingManager am = new APICountingManager(p);
        System.out.println("--- After APICountingManager Constructed ---");
        System.out.println("am.getCountingNow() = " + am.getCountingNow());
        System.out.println("p.getCommitList().size() = " + p.getCommitList().size());

        am.deleteCountingCache();

        while (am.hasNextCommit()) {
            am.analyseNextCommit();
        }

        System.out.println("--- After All Commits Analysed ---");
        System.out.println("am.getNewCountingCount() = " + am.getNewCountingCount());
        System.out.println("am.getCheckoutFault() = " + am.getCheckoutFault());
        System.out.println("am.getCountAPIFault() = " + am.getCountAPIFault());

        System.out.println("am.getCountingNow() = " + am.getCountingNow());
        System.out.println("p.getCommitList().size() = " + p.getCommitList().size());

        APICounters a = am.getCountingCache();
        System.out.println("a.getLastUpdate() = " + a.getLastUpdate());
//        for (APICounter aa: a.getApiCounterList()) {
//            System.out.println("    aa.getCommitIndex() = " + aa.getCommitIndex());
//            for (String s: aa.getApiFrequency().keySet()) {
//                System.out.println("        s = " + s);
//                System.out.println("        aa.getApiFrequency().get(s) = " + aa.getApiFrequency().get(s));
//            }
//        }

        if (am.getNewCountingCount() > 0) {
            try {
                am.saveCountingCache();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        return am;
    }

    private static void printToFile(APICountingManager am) throws IOException {
        /* init */
        File outFile = new File("output.txt");
        if (outFile.exists()) {
            if (!outFile.delete()) {
                throw new IOException("Cannot Delete " + outFile.getPath());
            }
        }
        PrintWriter out = new PrintWriter(outFile);

        /* project */
        out.println("projectUrl: " + am.getProject().getProjectUrl());
        out.println("pureName: " + am.getProject().getPureName());
        out.println("projectPath: " + am.getProject().getProjectPath());

        /* 枚举：某次commit */
        Project project = am.getProject();
        APICounters apiCounters = am.getCountingCache();
        for (int i = 0; i < project.getCommitList().size(); i++){
            /* commit */
            Commit commit = project.getCommitList().get(i);
            out.println("[commit - " + i + "] " + commit.getCommitShortMessage());

            /* APICounting排序并输出 */
            APICounter apiCounter = apiCounters.getApiCounterList().get(i);
            List<Map.Entry<String, Long>> mapping = new ArrayList<Map.Entry<String, Long>>(apiCounter.getApiFrequency().entrySet());
            mapping.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            for (Map.Entry<String, Long> m: mapping) {
                out.print("    " + m.getKey() + "(" + m.getValue() + ")");
            }
            out.println();
        }

        /* 提示信息 */
        System.out.println("checkoutFault: " + am.getCheckoutFault());
        System.out.println("countAPIFault: " + am.getCountAPIFault());

        /* finish */
        out.close();
    }

}
