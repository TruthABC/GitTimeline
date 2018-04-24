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
        loadProject();//有缓存则载入
        downloadProject();//先删除缓存再clone
        downloadProject();//先删除缓存再clone
    }

    //载入项目（无网络，幂等）
    private static void loadProject() {
        ProjectManager pm = new ProjectManager(projectUrl);
        Project p;

        System.out.println("--- After ProjectManager Constructed ---");
        System.out.println("pm.isProjectCached() - " + pm.isProjectCached());
        p = pm.getProjectCache();
        if (p != null) {
            System.out.println("p.getProjectUrl() - " + p.getProjectUrl());
            System.out.println("p.getProjectPureName() - " + p.getPureName());
            System.out.println("p.getProjectPath() - " + p.getProjectPath());
            System.out.println("p.getLastUpdate() - " + p.getLastUpdate());
            for (Commit c : p.getCommitList()) {
                System.out.println("[commit - " + c.getCommitIndex() + "] " + c.getCommitShortMessage());
            }
        }
    }

    //下载项目（有网络，不幂等）
    private static void downloadProject() {
        ProjectManager pm = new ProjectManager(projectUrl);
        Project p;

        pm.downloadProject();

        System.out.println("--- After Project Downloaded ---");
        System.out.println("pm.isProjectCached() - " + pm.isProjectCached());
        p = pm.getProjectCache();
        if (p != null) {
            System.out.println("p.getProjectUrl() - " + p.getProjectUrl());
            System.out.println("p.getProjectPureName() - " + p.getPureName());
            System.out.println("p.getProjectPath() - " + p.getProjectPath());
            System.out.println("p.getLastUpdate() - " + p.getLastUpdate());
            for (Commit c : p.getCommitList()) {
                System.out.println("[commit - " + c.getCommitIndex() + "] " + c.getCommitShortMessage());
            }
        }

    }

}
