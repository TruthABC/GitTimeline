package javaparser;

import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Japa Abstract syntax tree
 * Get Class Full Name.
 * Created by wangxin on 10/06/2017.
 */
public class JapaAst {
    private HashMap<String,Long> names;// 存取 待匹配类名和出现次数 对
    private LinkedList<String> packages;// 存取前缀包名

    private String packageName = "";
    private HashMap<String,Long>  completenames;// 存取 完整类名和出现次数 对

    public JapaAst() {
    }

    /**
     * 输入要静态解析的CompliationUnit,返回一个包含该文件中所有声明到的完整类名的list,以string方式存储
     */
    public HashMap<String,Long> parse(CompilationUnit cu) throws Exception {
        names = new HashMap<> ();
        packages = new LinkedList<>();
        completenames = new HashMap<>();

        add2List(packages, "java.lang");// java会自动引入java.lang包
        CompilationUnit result = cu;
        result.accept(new MyVisitor(), null);
        HashMap<String,Long> rs = handleNames();
        return  rs;
    }

    /**
     * 合成完整类名所在位置
     */
    private HashMap<String,Long> handleNames() {
        for (String clazzName : names.keySet()) {
            if (!isIncluded(clazzName)) {
                long cnt = names.get(clazzName);
                try {
                    add2map(completenames, Thread.currentThread().getContextClassLoader().loadClass(clazzName).getName(),cnt);
                } catch (Exception e) {
                    if (e instanceof ClassNotFoundException) {
                        match2Package(clazzName, completenames, cnt);
                    }
                } catch (Error e) {

                }
            }
        }
        return completenames;
    }

    /**
     * 若在map中,已有最大匹配的完整类名路径,则无需再次匹配添加包的路径前缀,而是将其cnt融合,然后删除无完整路径项
     */
    private boolean isIncluded(String clazzName) {
        String rg = ".*\\." + clazzName + "$";
        Pattern pattern = Pattern.compile(rg);
        for (String name : completenames.keySet()) {
            Matcher match = pattern.matcher(name);
            if (match.find()) {
                completenames.put(name, completenames.get(name) + names.get(clazzName));
                return true;
            }
        }
        return false;
    }

    /**
     * 当单个类名无法匹配时,运用排列组合方法从前缀packages中匹配包名
     */
    private void match2Package(String clazzName, HashMap<String,Long> map, long cnt) {
        for (String packageName : packages) {
            try {
                add2map(map, Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + clazzName).getName(),cnt);
                return;
            } catch (Exception | Error e) {
                // skip
            }
        }
    }


    /**
     * 将输入的className,不重复地插入到list中,并返回这个list
     */
    private LinkedList<String> add2List(LinkedList<String> list, String className) {
        if (!list.contains(className)) {
            list.addLast(className);
        }
        return list;
    }

    /**
     * 将输入的className,重复地插入到map中,并返回这个map
     */
    private HashMap<String,Long> add2map(HashMap<String, Long> map, String className, long cnt) {

        if(map.keySet().contains(className)){
            map.put(className,map.get(className)+cnt);
        }
        else{
            map.put(className,cnt);
        }
        return map;
    }

    private String filterAngleBracket(String type) {
        if (type.contains("<")) {
            int index = type.indexOf("<");
            type = type.substring(0, index);
        }
        return type;
    }

    private String filterSquareBracket(String type) {
        type = type.replaceAll("\\[\\]", "");
        if (type.contains("<")) {
            type = filterAngleBracket(type);
        }
        return type;
    }

    class MyVisitor extends VoidVisitorAdapter<Void> {
        private MyVisitor() {

        }

        // 抽取package name
        @Override
        public void visit(PackageDeclaration node, Void arg) {
            packageName = node.getName().toString();
            add2List(packages, node.getName().toString());
            super.visit(node, arg);
        }

        // 抽取import的包名, java会自动引入java.lang包
        @Override
        public void visit(ImportDeclaration node, Void arg) {
            // 按需导入的包,作为前缀等待补全
            if (node.isAsterisk()) {
                add2List(packages, node.getName().toString());
            } else {
                add2map(completenames, node.getName().toString(), 1L);
            }
            super.visit(node, arg);
        }

        @Override
        public void visit(NameExpr node, Void arg){
            add2map(names,filterSquareBracket(filterAngleBracket(node.getName())), 1L);
            super.visit(node,arg);
        }

        // 抽取所有的类和接口声明
        @Override
        public void visit(ClassOrInterfaceType node, Void arg){
            add2map(names,filterSquareBracket(filterAngleBracket(node.getName())), 1L);
            super.visit(node, arg);
        }
    }
}