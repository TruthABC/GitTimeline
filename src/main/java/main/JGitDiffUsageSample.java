package main;

import jgit.JGitExample;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import toolkit.FileTool;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试，试运行JGitExample的getDiffList功能
 */
public class JGitDiffUsageSample {

    public static void main(String[] args) {

        String projectUrl = "https://github.com/TruthABC/FucanBackend";
        String projectPath = "FucanBackend";
//        String projectUrl = "https://github.com/MrDoomy/Torch";
//        String projectPath = "Torch";

        File projectDir = new File(projectPath);

        if (!projectDir.exists()) {
            JGitExample.clone(projectUrl, projectPath);
        }

        JGitExample jGit = new JGitExample();
        jGit.initGitRoot(projectPath + "/.git");

        List<RevCommit> commitList = jGit.getGitCommitList();

        jGit.checkout(commitList.get(0).getName());

        List<File> allFiles = new ArrayList<>();
        FileTool.listFiles(allFiles, projectDir, "");

        System.out.println("Commit[0] " + commitList.get(0).getShortMessage());
        for (int i = 1; i < allFiles.size(); i++) {
            System.out.println("\t" + allFiles.get(i).getPath());
        }

        for (int i = 1; i < commitList.size(); i++) {
            List<DiffEntry> diffList = jGit.getDiffList(commitList.get(i-1).getName(), commitList.get(i).getName());
            System.out.println("Commit Diff["+ i +"] " + commitList.get(i).getShortMessage());

            for (DiffEntry entry: diffList) {
                System.out.println("\t" + entry.getOldPath() + " -> " + entry.getNewPath());
            }
        }

        jGit.checkout(commitList.get(commitList.size() - 1).getName());
        System.out.println("Total Commit Count: " + commitList.size());
        System.out.println("Total Diff Count: " + (commitList.size() - 1));
        jGit.close();

    }

}
