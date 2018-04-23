package toolkit;

import java.io.File;
import java.util.List;

/**
 * 常见的文件操作
 */
public class FileTool {

    /**
     * 填充一个文件列表files，用dir目录下的所有扩展名为expansionName的文件
     * @param files
     * @param dir
     * @param expansionName
     */
    public static void listFiles(List<File> files, File dir, String expansionName){
        File[] listFiles = dir.listFiles();
        if (listFiles == null)
            return;
        for(File f: listFiles){
            if(f.isFile()){
                if(f.getName().endsWith(expansionName)) {
                    files.add(f);
                }
            }else if(f.isDirectory()){
                listFiles(files, f, expansionName);
            }
        }
    }

    /**
     * 删除一个文件或文件夹
     * @param dir
     */
    public static void deleteDir(File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file: files) {
                    deleteDir(file);
                }
            }
        }
        if (!dir.delete()) {
            System.out.println("deleteDir(): Error Deleting file - " + dir.getPath());
            System.exit(1);
        }
    }

}