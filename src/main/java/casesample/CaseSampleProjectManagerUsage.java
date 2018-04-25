package casesample;

import main.ProjectManager;
import serializable.Commit;
import serializable.Project;

/**
 * 测试，试用ProjectManager对象
 */
public class CaseSampleProjectManagerUsage {

    private static final String projectUrl = "https://github.com/sikaozhe1997/Xin-Yue";

    public static void main(String[] args) {
        downloadProject(projectUrl);//构造时读缓存；执行downloadProject时删除缓存执行download
    }

    //下载项目（有网络，不幂等）
    private static void downloadProject(String projectUrl) {
        ProjectManager pm = new ProjectManager(projectUrl);
        System.out.println("--- After ProjectManager Constructed ---");
        System.out.println("pm.isProjectCached() - " + pm.isProjectCached());
        showProject(pm.getProjectCache());

        pm.downloadProject();
        System.out.println("--- After Project Downloaded ---");
        System.out.println("pm.isProjectCached() - " + pm.isProjectCached());
        showProject(pm.getProjectCache());
    }

    public static void showProject(Project p) {
        if (p != null) {
            System.out.println("p.getProjectUrl() - " + p.getProjectUrl());
            System.out.println("p.getProjectPureName() - " + p.getPureName());
            System.out.println("p.getProjectPath() - " + p.getProjectPath());
            System.out.println("p.getLastUpdate() - " + p.getLastUpdate());
            for (Commit c : p.getCommitList()) {
                System.out.println("[commit - " + c.getCommitIndex() + "] " + c.getCommitShortMessage());
            }
        } else {
            System.out.println("p == null");
        }
    }

}
