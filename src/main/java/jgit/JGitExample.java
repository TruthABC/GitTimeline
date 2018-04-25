package jgit;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 面向本Project，封装了jGit的一些操作
 */
public class JGitExample {

    private Git git = null;

    public JGitExample() {}

    public JGitExample(String gitRoot){
        initGitRoot(gitRoot);
    }

    public static boolean clone(String url, String targetPath) {
        if (!new File(targetPath).mkdirs()){
            System.out.println("clone(): Error Making Dirs - " + targetPath);
            System.exit(1);
        }
        try {
            CloneCommand clone = Git.cloneRepository();
            clone.setURI(url);
            clone.setDirectory(new File(targetPath));
            clone.call().close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("clone(): Error Happened");
            return false;
        }
        return true;
    }

    public void initGitRoot(String gitRoot){
        if (git != null) {
            git.close();
        }
        try {
            git = Git.open(new File(gitRoot));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("initGitRoot() Error Happened");
        }
    }

    public List<RevCommit> getGitCommitList(){
        List<RevCommit> resList = new ArrayList<RevCommit>();
        if (git == null) {
            return resList;
        }
        Repository repository = git.getRepository();
        try( RevWalk revWalk = new RevWalk( repository ) ) {
            ObjectId commitId = repository.resolve( "refs/heads/master" );
            revWalk.markStart( revWalk.parseCommit( commitId ) );
            for( RevCommit commit : revWalk ) {
                resList.add(commit);
            }
            //保证时间顺序从小到大，从早到晚，从远到近
            resList.sort((o1, o2) -> {
                if (o1.getCommitTime() > o2.getCommitTime()) {
                    return 1;//前大于后则“做”交换(1)
                } else {
                    return -1;//前不大于后“不”交换(-1)
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getGitCommitList() Error Happened");
        }
        return resList;
    }

    public boolean checkout(String commitName) {
        if (git == null) {
            return false;
        }
        try {
            CleanCommand clean = git.clean();
            clean.setForce(true);
            clean.call();
            ResetCommand reset = git.reset();
            reset.setMode(ResetCommand.ResetType.HARD);
            reset.call();
            CheckoutCommand checkout = git.checkout();
            checkout.setName(commitName);
            checkout.call();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("checkout() Error Happened");
            return false;
        }
        return true;
    }

    public List<DiffEntry> getDiffList(String oldCommitName, String newCommitName) {

        List<DiffEntry> diffList = new ArrayList<DiffEntry>();
        if (git == null) {
            return diffList;
        }

        Repository repository = git.getRepository();
        ObjectReader reader = repository.newObjectReader();

        try {
            ObjectId old = repository.resolve(oldCommitName + "^{tree}");
            ObjectId head = repository.resolve(newCommitName + "^{tree}");

            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, old);

            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);

            diffList = git.diff()
                    .setNewTree(newTreeIter)
                    .setOldTree(oldTreeIter)
                    .call();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("getDiffList() Error Happened");
        }

        return diffList;
    }


    public void close() {
        if (git != null) {
            git.close();
            git = null;
        }
    }

}
