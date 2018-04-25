package main;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import javaparser.JapaAst;
import jgit.JGitExample;
import org.eclipse.jgit.diff.DiffEntry;
import serializable.APICounter;
import serializable.APICounters;
import serializable.AndroidAPISetCache;
import serializable.Project;
import toolkit.DBConnect;
import toolkit.FileTool;
import toolkit.Global;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class APICountingManager {

    /* Set By User */
    private int showDetail = 0;//是否打印每个API判断的结果，1打印；0不打印

    /* Set Only By "initAndroidAPISet()", Which Is Only Called By Constructor */
    private HashSet<String> androidAPISet;//安卓API的数据库内容，内存存储，加速运行

    /* Cannot Set By User */
    private Project project;//即将分析的项目及其目录信息，有该对象说明缓存是存在的

    private APICounters countingCache = null;
    private int countingNow = 0;//项目分析总的进度

    private int newCountingCount;//记录新分析的commit的次数
    private int checkoutFault;//记录checkout失败次数
    private int countAPIFault;//记录countAPI失败次数

    public APICountingManager(Project project) {
        initAndroidAPISet();
        initByProject(project);
    }

    /**
     * [Public Methods (1/5)]根据project初始化APICountingManager；被构造函数自动调用；会尝试载入缓存
     * @param project
     */
    public void initByProject(Project project) {
        System.out.println("--- APICountingManager: Initializing By Project ---");

        /* 初始化变量 */
        this.project = project;
        newCountingCount = 0;//记录新分析的commit的次数
        checkoutFault = 0;//记录checkout失败次数
        countAPIFault = 0;//记录countAPI失败次数

        /* 提示信息 */
        System.out.println("initByProject(): projectUrl = " + project.getProjectUrl());
        System.out.println("initByProject(): pureName = " + project.getPureName());
        System.out.println("initByProject(): projectPath(Abosolute) = " + project.getProjectPath());

        /* 紧接着需要载入缓存信息 */
        try {
            loadCountingCache();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("initByProject(): Cache Loading Error. (Suggestion: initByProject() Again)");
        }

    }

    /**
     * [Public Methods (2/5)]删除旧缓存（通常不使用，除非希望在不更新项目的情况下，重新分析
     */
    public void deleteCountingCache() {
        System.out.println("--- Deleting Counting Cache ---");
        File cacheFile = new File(project.getPureName() + ".asv");//asv - APICounters save file
        if (cacheFile.exists()) {
            FileTool.deleteDir(cacheFile);
            System.out.println("deleteCountingCache(): Old Counting Cache Deleted");
        } else {
            System.out.println("deleteCountingCache(): Old Counting Cache Not Exist");
        }
        countingCache = null;
        countingNow = 0;
    }

    /**
     * [Public Methods (3/5)]将缓存从内存写到文件中
     * @throws IOException
     */
    public void saveCountingCache() throws IOException {
        System.out.println("--- Saving Counting Cache ---");
        if (newCountingCount <= 0) {//没有任何进展则不予存储
            return;
        }

        File cacheFile = new File(project.getPureName() + ".asv");//asv - APICounters save file
        if (cacheFile.exists()) {
            FileTool.deleteDir(cacheFile);
            System.out.println("saveCountingCache(): Old Counting Deleted");
        } else {
            System.out.println("saveCountingCache(): Old Counting Not Exist");
        }
        if (!cacheFile.createNewFile()) {//已经执行了上面的语句保证这个文件已经删除过了
            System.out.println("saveCountingCache(): Error Creating Cache File.");
            System.exit(1);
        }

        //写入缓存
        FileOutputStream fos = new FileOutputStream(cacheFile);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(countingCache);
        oos.close();
        fos.close();

        System.out.println("saveCountingCache(): Cache Saved.");
    }

    /**
     * [Public Methods (4/5)]是否还有下一个待分析commit
     * @return 有true 没有false
     */
    public boolean hasNextCommit() {
        return countingNow < project.getCommitList().size();
    }

    /**
     * [Public Methods (5/5)]分析下一个待分析commit
     * @return 返回下一个commit的API计数Map
     */
    public APICounter analyseNextCommit() {
        System.out.println("--- Analysing Next Commit [" + countingNow + "] ---");

        //如果没有下一次的未分析commit直接返回null
        if (!hasNextCommit()) {
            return null;
        }

        //提示信息
        System.out.println("analyseNextCommit(): processNow = " + countingNow + "/"+ project.getCommitList().size());
        //首次commit，需要实例化一个APICounters对象给countingCache
        if (countingNow == 0) {
            APICounter ret = new APICounter(countingNow, analyseFirstCommit());
            countingCache = new APICounters(new Date(), new ArrayList<APICounter>());
            countingCache.getApiCounterList().add(ret);
            countingNow++;
            newCountingCount++;
            return ret;
        }
        //非首次commit
        String oldCommitName = project.getCommitList().get(countingNow-1).getCommitName();
        String newCommitName = project.getCommitList().get(countingNow).getCommitName();
        HashMap<String, Long> diff = analyseCommitDiff(oldCommitName, newCommitName);
        HashMap<String, Long> base = new HashMap<String, Long>(countingCache.getApiCounterList().get(countingNow-1).getApiFrequency());

        for (String s: diff.keySet()) {
            long temp;
            if (!base.containsKey(s)) {
                temp = diff.get(s);
            } else {
                temp = base.get(s) + diff.get(s);
            }
            if (temp > 0) {
                base.put(s, temp);
            } else if (temp < 0) {
                System.out.println("analyseNextCommit(): Error - Should not be printed[1]");
            } else {
                if (base.containsKey(s)) {
                    base.remove(s);
                }
            }
        }

        APICounter ret = new APICounter(countingNow, base);
        countingCache.setLastUpdate(new Date());
        countingCache.getApiCounterList().add(ret);
        countingNow++;
        newCountingCount++;
        return ret;
    }

    /**
     * private且只执行一次，仅被构造函数调用，用于构造androidAPISet（内存化判定安卓API）
     */
    private void initAndroidAPISet() {
        //read from cache file
        File androidAPISetCacheFile = new File(AndroidAPISetCache.CACHE_FILE_NAME);
        if (androidAPISetCacheFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(androidAPISetCacheFile);
                ObjectInputStream ois = new ObjectInputStream(fis);
                AndroidAPISetCache aasc = (AndroidAPISetCache) ois.readObject();
                androidAPISet = aasc.getSet();
                ois.close();
                fis.close();
                System.out.println("initAndroidAPISet(): From File Totally (" + androidAPISet.size() + ") Android APIs");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("initAndroidAPISet(): File Cache read Operation GG");
                androidAPISetCacheFile.delete();
            }
            return;
        }

        //read from DB
        androidAPISet = new HashSet<String>();
        try {
            DBConnect dbConnect = new DBConnect(Global.DB_URL, Global.DB_USER, Global.DB_PASSWORD);
            ResultSet resultSet = dbConnect.executeQuery("SELECT `classname` FROM android_api.apis");
            while(resultSet.next()){
                String classname = resultSet.getString("classname");
                if (!androidAPISet.contains(classname)){
                    androidAPISet.add(classname);
                }
            }
            resultSet.close();
            dbConnect.close();
            System.out.println("initAndroidAPISet(): From DB Totally (" + androidAPISet.size() + ") Android APIs");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("initAndroidAPISet(): DataBase Operation GG");
            System.exit(1);
        }
        //DB to file chace
        try {
            if (!androidAPISetCacheFile.createNewFile()) {
                throw new IOException();
            }
            FileOutputStream fos = new FileOutputStream(androidAPISetCacheFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(new AndroidAPISetCache(androidAPISet));
            oos.close();
            fos.close();
            System.out.println("initAndroidAPISet(): Cache Save Operation Finished");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("initAndroidAPISet(): Cache Save Operation GG");
        }
    }

    /**
     * 读取固定文件中的APICounters对象；默认用户不会删除或者修改Counting缓存文件，不会新建缓存文件；在初始化时自动被调用
     * private 且仅一处调用：被initByProject()
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void loadCountingCache() throws IOException, ClassNotFoundException {
        File cacheFile = new File(project.getPureName() + ".asv");//asv - APICounters save file
        if (!cacheFile.exists()) {
            System.out.println("loadCountingCache(): No Cache To Load.");
            return;
        }

        //若缓存文件存在，则载入缓存理应正确
        FileInputStream fis = new FileInputStream(cacheFile);
        ObjectInputStream ois = new ObjectInputStream(fis);
        countingCache = (APICounters) ois.readObject();
        ois.close();
        fis.close();
        countingNow = countingCache.getApiCounterList().size();
        System.out.println("loadCountingCache(): Cache Loaded");
    }

    /**
     * 分析首次commit的API调用统计信息
     * private 且仅一处调用：被analyseNextCommit()
     * @return 返回分析后的结果
     */
    private HashMap<String, Long> analyseFirstCommit() {
        /* 切换到首次commit */
        //分支切换出错的原因，1、数据库的commits表的msg字段单引号注入错误（解决） 2、文件输入流没有close（解决）
        try {
            JGitExample jGit = new JGitExample(project.getProjectPath() + "/.git");
            jGit.checkout(project.getCommitList().get(0).getCommitName());
            jGit.close();
        } catch (Exception e) {
            System.out.println("analyseFirstCommit(): Checkout Error");
            checkoutFault++;
            return new HashMap<String, Long>();
        }

        /* 得到所有.java文件 */
        List<File> javaFiles = new ArrayList<>();
        FileTool.listFiles(javaFiles, new File(project.getProjectPath()),".java");

        /* 统计引用的安卓API数 */
        HashMap<String, Long> ret = new HashMap<String, Long>();
        for (File jf: javaFiles) {
            countAPI(jf, ret);
        }

        System.out.println("analyseFirstCommit(): Analysed End reach");
        return ret;
    }

    /**
     * 分析两次commit的API计数差值Map
     * private 且仅一处调用：被analyseNextCommit()
     * @param oldCommitName
     * @param newCommitName
     * @return
     */
    private HashMap<String, Long> analyseCommitDiff(String oldCommitName, String newCommitName) {
        /* Step 0 得到版本差异的DiffEntry列表 */
        JGitExample jGit = new JGitExample(project.getProjectPath() + "/.git");
        List<DiffEntry> diffList = jGit.getDiffList(oldCommitName, newCommitName);
//        for (DiffEntry entry: diffList) {
//            System.out.println("\t" + entry.getOldPath() + " -> " + entry.getNewPath());
//        }

        /* Step 1.1 得到old版本diff的.java文件列表（需要先checkout）*/
        if (!jGit.checkout(oldCommitName)) {
            //通常不出错，分支切换出错的原因，1、数据库的commits表的msg字段单引号注入错误（解决） 2、文件输入流没有close（解决）
            System.out.println("analyseCommitDiff(): Checkout Old Commit Error - " + oldCommitName);
            checkoutFault++;
            return new HashMap<String, Long>();
        }
        List<File> oldJavaFiles = new ArrayList<File>();
        for (DiffEntry entry: diffList) {
            String path = project.getProjectPath() + "/" + entry.getOldPath();
            File file = new File(path);
            if (file.exists() && file.isFile() && path.length()>5 && path.substring(path.length()-5).equals(".java")) {
                oldJavaFiles.add(file);
            }
        }

        /* Step 1.2 统计old版本diff的安卓API数 */
        HashMap<String, Long> apiFrequencyOld = new HashMap<String, Long>();
        for (File jf: oldJavaFiles) {
            countAPI(jf, apiFrequencyOld);
        }

        /* Step 2.1 得到new版本diff的.java文件列表（需要先checkout） */
        if (!jGit.checkout(newCommitName)) {
            //通常不出错，分支切换出错的原因，1、数据库的commits表的msg字段单引号注入错误（解决） 2、文件输入流没有close（解决）
            System.out.println("analyseCommitDiff(): Checkout New Commit Error - " + newCommitName);
            checkoutFault++;
            return new HashMap<String, Long>();
        }
        List<File> newJavaFiles = new ArrayList<File>();
        for (DiffEntry entry: diffList) {
            String path = project.getProjectPath() + "/" + entry.getNewPath();
            File file = new File(path);
            if (file.exists() && file.isFile() && path.length()>5 && path.substring(path.length()-5).equals(".java")) {
                newJavaFiles.add(file);
            }
        }

        /* Step 2.2 统计new版本diff的安卓API数 */
        HashMap<String, Long> apiFrequencyNew = new HashMap<String, Long>();
        for (File jf: newJavaFiles) {
            countAPI(jf, apiFrequencyNew);
        }

        /* Stemp 3 求得两次版本的API调用次数变更情况 */
        for (String s: apiFrequencyOld.keySet()) {
            if(!apiFrequencyNew.keySet().contains(s)){
                apiFrequencyNew.put(s, 0 - apiFrequencyOld.get(s));
            } else {
                apiFrequencyNew.put(s, apiFrequencyNew.get(s) - apiFrequencyOld.get(s));
            }
        }

        jGit.close();
        System.out.println("analyseCommitDiff(): Analysed End reach");
        return apiFrequencyNew;
    }

    /**
     * 计算该文件中的安卓API种类与个数,累加进入apiFrequency(HashMap)
     * private且仅被调用三次，均为analyseNextCommit()方法
     * @param file 一个目标java文件
     * @param apiFrequency 最终统计结果Map的引用
     */
    private void countAPI(File file, HashMap<String, Long> apiFrequency){
        try {
            FileInputStream inputStream = new FileInputStream(file);
            try {
                CompilationUnit cu = JavaParser.parse(inputStream);
                JapaAst japaAst = new JapaAst();
                HashMap<String, Long> apiFrequencyInOne = japaAst.parse(cu);
                for (String s: apiFrequencyInOne.keySet()) {
                    if(androidAPISet.contains(s)){
                        if (showDetail > 0) {
                            System.out.println("[√]" + s);
                        }
                        if(!apiFrequency.keySet().contains(s)){
                            apiFrequency.put(s, apiFrequencyInOne.get(s));
                        } else {
                            apiFrequency.put(s, apiFrequency.get(s) + apiFrequencyInOne.get(s));
                        }
                    } else {
                        if (showDetail > 0) {
                            System.out.println("[x]" + s);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("countAPI(): File Input Stream Error - " + file.getPath());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("countAPI(): Counting API Error - " + file.getPath());
                countAPIFault++;
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("countAPI(): File Open/Close Error - " + file.getPath());
        }
    }

    /* Setters & Getters Following */
    public void setShowDetail(int showDetail) {
        this.showDetail = showDetail;
    }

    public Project getProject() {
        return project;
    }

    public APICounters getCountingCache() {
        return countingCache;
    }

    public int getCountingNow() {
        return countingNow;
    }

    public int getNewCountingCount() {
        return newCountingCount;
    }

    public int getCheckoutFault() {
        return checkoutFault;
    }

    public int getCountAPIFault() {
        return countAPIFault;
    }
}
