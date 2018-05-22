package main;

import jgit.JGitExample;
import org.eclipse.jgit.revwalk.RevCommit;
import serializable.Commit;
import serializable.Project;
import toolkit.FileTool;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectManager {

    /* Not Accessible To User */
    private String projectUrl;//该项目的在线git仓库地址，可以作为唯一的uuid "https://github.com/MrDoomy/Torch"
    private String pureName;//除去url中https及作者信息，项目的真正名字 "Torch"
    private String projectPath;//指定下载项目的本地路径(路径下必须有.git文件夹) "C:/Users/Shijian/temp/Torch"

    /* Getter Available To User */
    private Project projectCache = null;
    private boolean isProjectCached = false;

    /**
     * Constructor; Have One Sentence: Call initByUrl()
     * Only Constructor - Default or Empty Constructor not Permitted
     * @param projectUrl
     */
    public ProjectManager(String projectUrl) {
        initByUrl(projectUrl);
    }

    /**
     * [Public Methods (1/3)]根据projectUrl初始化ProjectManager；被构造函数调用；尝试载入缓存；不尝试下载项目
     *  尝试载入缓存过程正常return true
     *  尝试载入缓存过程异常return false（例如文件无法访问）
     * @param projectUrl
     */
    public boolean initByUrl(String projectUrl) {
        System.out.println("--- ProjectManager: Initializing By Url ---");

        /* 初始化1：其中projectPath需要再次初始化 */
        this.projectUrl = projectUrl;
        this.projectPath = new File("").getAbsolutePath();

        /* 初始化2：得到项目真正名称pureName，并重新构造projectPath */
        int i;
        for (i = this.projectUrl.length()-1; i >= 0; i--){
            if (this.projectUrl.charAt(i) == '/'){
                break;
            }
        }
        this.pureName = this.projectUrl.substring(i+1);
        this.projectPath += "/" + this.pureName;

        /* 提示信息 */
        System.out.println("initByUrl(): projectUrl = " + this.projectUrl);
        System.out.println("initByUrl(): pureName = " + this.pureName);
        System.out.println("initByUrl(): projectPath(Abosolute) = " + this.projectPath);

        /* 紧接着需要载入缓存信息 */
        try {
            loadProjectCache();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("initByUrl(): Cache Loading Error. (Suggestion: initByUrl() Again)");
            return false;
        }
        return true;
    }

    /**
     * [Public Methods (2/3)]下载项目仓库，收集数据并制作缓存；首先尝试删除旧缓存
     *  1. Commit解析过程不成功this.projectCache大概率(size()==0)，符合格式，但没有意义
     *  2. 文件操作（删除+新建）必须成功，否则System.exit(1)
     */
    public void downloadProject() {
        System.out.println("--- ProjectManager: Downloading Project ---");

        //删除旧缓存文件
        deleteAllCache();
        
        //删除旧缓存对应的仓库
        FileTool.deleteDir(new File(projectPath));
        
        //克隆项目，得到新的仓库到本地
        if (JGitExample.clone(projectUrl, projectPath)) {
            System.out.println("downloadProject(): Cloned - "+ projectUrl);
        } else {
            System.out.println("downloadProject(): Cloning Error - "+ projectUrl + " (Suggestion: Try Again)");
        }

        //制作缓存填充this.projectCache：使用jGit遍历本地仓库历史收集数据
        fillProjectCache();

        //将缓存写入本地文件
        try {
            saveProjectCache();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("downloadProject(): Error Saving Project Cache.");
            System.exit(1);
        }
    }

    /**
     * [Public Methods (3/3)]删除旧缓存（附带的先要删除APICounting缓存）
     *  内部一处被调用：被downloadProject()
     */
    public void deleteAllCache() {
        File countingCacheFile = new File(pureName + ".asv");//asv - APICounters save file
        if (countingCacheFile.exists()) {
            FileTool.deleteDir(countingCacheFile);
            System.out.println("deleteAllCache(): Old Counting Cache Deleted");
        } else {
            System.out.println("deleteAllCache(): Old Counting Cache Not Exist");
        }

        File projectCacheFile = new File(pureName + ".psv");//psv - project save file
        if (projectCacheFile.exists()) {
            FileTool.deleteDir(projectCacheFile);
            System.out.println("deleteAllCache(): Old Project Cache Deleted");
        } else {
            System.out.println("deleteAllCache(): Old Project Cache Not Exist");
        }
        projectCache = null;
        isProjectCached = false;
    }

    /**
     * 读取固定文件中的Project对象；默认用户不会删除或者修改仓库缓存文件，不会新建缓存文件；在初始化时自动被调用
     * private 且仅一处调用：被initByUrl()
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void loadProjectCache() throws IOException, ClassNotFoundException {
        File cacheFile = new File(pureName + ".psv");//psv - project save file
        if (!cacheFile.exists()) {
            projectCache = null;
            isProjectCached = false;
            System.out.println("loadProjectCache(): No Cache To Load");
            return;
        }

        //若缓存文件存在，则载入缓存理应正确
        FileInputStream fis = new FileInputStream(cacheFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        projectCache = (Project) ois.readObject();
        ois.close();
        fis.close();
        isProjectCached = true;
        System.out.println("loadProjectCache(): Cache Loaded");
    }

    /**
     * 制作缓存填充projectCache：使用jGit遍历仓库历史收集数据
     * private 且仅一处调用：被downloadProject()
     */
    private void fillProjectCache() {
        /* 实例化JGitExample对象 */
        JGitExample jGit = new JGitExample(projectPath + "/.git");

        /* 得到master的所有commit，时间由远到近，从早到晚 */
        List<RevCommit> commitList = jGit.getGitCommitList();

        /* 释放jGit */
        jGit.close();

        /* 枚举所有commit，得到数据，填充ProjectCache */
        Project project = new Project(projectUrl, pureName, projectPath, new Date(), new ArrayList<Commit>());
        for (int i = 0; i < commitList.size(); i++){
            RevCommit revCommit = commitList.get(i);

            String commitName = revCommit.getName();
            long time = (long) revCommit.getCommitTime() * 1000; //返回的是int，从1970开始的秒数
            Date commitTime = new Date(time);
            String commitShortMessage = revCommit.getShortMessage();
            String commitFullMessage = revCommit.getFullMessage();

            Commit commit = new Commit(i, commitName, commitTime, commitShortMessage, commitFullMessage);
            project.getCommitList().add(commit);
        }
        projectCache = project;
    }

    /**
     * 保存项目缓存到本地，调用前需要承诺projectCache有意义（调用fillProjectCache）
     * private 且仅一处调用：被downloadProject()
     * @throws IOException
     */
    private void saveProjectCache() throws IOException {
        File cacheFile = new File(pureName + ".psv");//psv - project save file
        if (!cacheFile.createNewFile()) {//可以保证这个文件已经删除过了，因为一定已经执行了deleteProjectCache
            System.out.println("saveProjectCache(): Error Creating Cache File.");
            System.exit(1);
        }

        //写入缓存
        FileOutputStream fos = new FileOutputStream(cacheFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(projectCache);
        oos.close();
        fos.close();

        isProjectCached = true;
        System.out.println("saveProjectCache(): Cache Saved.");
    }

    /* Getters Following: */
    public Project getProjectCache() {
        return projectCache;
    }

    public boolean isProjectCached() {
        return isProjectCached;
    }
}
