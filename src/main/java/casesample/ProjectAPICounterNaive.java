package casesample;

import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import javaparser.JapaAst;
import jgit.JGitExample;
import org.eclipse.jgit.revwalk.RevCommit;
import toolkit.DBConnect;
import toolkit.FileTool;
import toolkit.Global;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProjectAPICounterNaive {

    public static void main(String args[]) throws IOException {

        ProjectAPICounterNaive pac = new ProjectAPICounterNaive("https://github.com/MrDoomy/Torch");

        /* 下载项目到本地 */
        pac.downloadProject();

        /* 静态解析某项目每次commit的每个安卓API信息 */
        pac.countProjectAPI();

        /* 收尾 */
        pac.finish();
    }

    private int showDetail = 0;//是否打印每个API判断的结果，1打印；0不打印

    private int checkoutFault = 0;//记录checkout失败次数
    private int countAPIFault = 0;//记录countAPI失败次数

    private String projectUrl;//该项目的在线git仓库地址，可以作为唯一的uuid "https://github.com/MrDoomy/Torch"
    private String pureName;//除去url中https及作者信息，项目的真正名字 "Torch"
    private String projectPath;//指定下载项目的本地路径(路径下必须有.git文件夹) "C:/Users/Shijian/temp/Torch"

    private DBConnect dbConnect;//项目数据的数据库

    private Set<String> AndroidAPISet;//安卓API的数据库内容，内存存储，加速运行

    /**
     * 空构造函数
     */
    public ProjectAPICounterNaive(){}

    /**
     * 构造函数：根据projectUrl构造
     */
    public ProjectAPICounterNaive(String projectUrl) {
        initByUrl(projectUrl);
    }

    /**
     * 根据Url初始化对象
     */
    public void initByUrl(String projectUrl) {
        /* 初始化1：其中projectPath需要再次初始化 */
        this.projectUrl = projectUrl;
        this.projectPath = new File("").getAbsolutePath();
        this.dbConnect = new DBConnect(Global.DB_URL, Global.DB_USER, Global.DB_PASSWORD);

        /* 初始化2：得到项目真正名称pureName，并重新构造projectPath */
        int i;
        for (i = this.projectUrl.length()-1; i >= 0; i--){
            if (this.projectUrl.charAt(i) == '/'){
                break;
            }
        }
        this.pureName = this.projectUrl.substring(i+1);
        this.projectPath += "/" + this.pureName;
    }

    /**
     * 伪析构函数
     */
    public void finish(){
        dbConnect.close();
    }

    /**
     * git clone 项目到指定目录
     */
    public void downloadProject(){
        if (new File(projectPath).exists()) {
            System.out.println("发现项目缓存["+ projectUrl +"]");
        } else {
            System.out.println("正在clone项目["+ projectUrl +"]");
            JGitExample.clone(projectUrl, projectPath);
        }

    }

    /**
     * 静态解析某项目每次commit的每个安卓API信息，打印到文件中
     */
    public void countProjectAPI() throws IOException {
        /* init */
        File outFile = new File("output.txt");
        if (outFile.exists()) {
            if (!outFile.delete()) {
                throw new IOException("Cannot Delete " + outFile.getPath());
            }
        }
        PrintWriter out = new PrintWriter(outFile);

        /* project */
        out.println("projectUrl: " + projectUrl);
        out.println("pureName: " + pureName);
        out.println("projectPath: " + projectPath);

        /* 实例化JGitExample对象 */
        JGitExample jGit = new JGitExample();
        jGit.initGitRoot(projectPath + "/.git");

        /* 得到master的所有commit，时间由近到远 */
        List<RevCommit> commitList = jGit.getGitCommitList();

        /* 枚举：某次commit */
        for (int i = 0; i < commitList.size(); i++){
            /* commit */
            RevCommit commit = commitList.get(i);
            System.out.println("[commit - " + i + "] " + commit.getShortMessage());
            out.println("[commit - " + i + "] " + commit.getShortMessage());

            /* 切换到某次commit */
            //分支切换出错的原因，1、数据库的commits表的msg字段单引号注入错误 2、文件输入流没有close
            try {
                jGit.checkout(commit.getName());
            } catch (Exception e) {
                System.out.println("切换分支出错");
                checkoutFault++;
                continue;
            }

            /* 得到所有.java文件 */
            List<File> javaFiles = new ArrayList<File>();
            FileTool.listFiles(javaFiles, new File(projectPath),".java");

            /* 统计引用的安卓API数 */
            Map<String, Long> apiFrequency = new HashMap<>();
            for (File jf: javaFiles) {
                countAPI(jf, apiFrequency);
            }

            /* APICounting排序并输出 */
            List<Map.Entry<String, Long>> mapping = new ArrayList<Map.Entry<String, Long>>(apiFrequency.entrySet());
            mapping.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            for (Map.Entry<String, Long> m: mapping) {
                out.print("    " + m.getKey() + "(" + m.getValue() + ")");
            }
            out.println();
        }

        /* 提示信息 */
        System.out.println("checkoutFault: " + checkoutFault);
        System.out.println("countAPIFault: " + countAPIFault);

        /* finish */
        out.close();
        jGit.close();
    }

    /**
     * 计算该文件中的安卓API种类与个数
     * @param file 一个目标java文件
     * @param apiFrequency 最终统计结果Map的引用
     */
    private void countAPI(File file, Map<String, Long> apiFrequency){
        try {
            FileInputStream inputStream = new FileInputStream(file);
            try {
                CompilationUnit cu = JavaParser.parse(inputStream);
                JapaAst japaAst = new JapaAst();
                HashMap<String, Long> apiFrequencyInOne = japaAst.parse(cu);
                for (String s: apiFrequencyInOne.keySet()) {
                    if(isAndroidAPI(s)){
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
            } catch (Exception e) {
                e.printStackTrace();
                countAPIFault++;
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否为安卓API（内存化版本）
     * @param classname 待判断的api的类名
     * @return true是安卓API false不是安卓API
     */
    private boolean isAndroidAPI(String classname){
        if (AndroidAPISet == null) {
            AndroidAPISet = new HashSet<String>();
            initAndroidAPISet(AndroidAPISet);
        }
        return AndroidAPISet.contains(classname);
    }

    /**
     * 只执行一次，用于isAndroidAPI
     * @param AndroidAPISet
     */
    private void initAndroidAPISet(Set<String> AndroidAPISet) {
        try {
            ResultSet resultSet = dbConnect.executeQuery("SELECT `classname` FROM android_api.apis");
            while(resultSet.next()){
                String classname = resultSet.getString("classname");
                if (!AndroidAPISet.contains(classname)){
                    AndroidAPISet.add(classname);
                }
            }
            resultSet.close();
            System.out.println("[信息]一共有(" + AndroidAPISet.size() + ")个安卓API");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
