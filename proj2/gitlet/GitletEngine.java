package gitlet;
import java.io.Serializable;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class GitletEngine implements Serializable {
    private static boolean isConflicted;
    public static String committedDirctory = "committed";
    public static File currentDirectory = new File(System.getProperty("user.dir"));
    public static final File GITDIR = new File(currentDirectory, ".gitlet");
    public static boolean checkInit() {
        return GITDIR.exists() && GITDIR.isDirectory();
    }

    public static void writeObjectToFile(Object myMetadata) {
        File outfile = new File(GITDIR, "metadata");
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(outfile));
            out.writeObject(myMetadata);
            out.close();
        } catch (IOException excp) {
            throw new Error("IO Error");
        }
    }

    public static Tree loadTree() {
        Tree myMetadata = new Tree();
        File inFile = new File(GITDIR, "metadata");
        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            myMetadata = (Tree) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            myMetadata = null;
            throw new Error("IO Error");
        }
        return myMetadata;
    }


    public static void init() {
        //File pwd = new File(System.getProperty("user.dir"));
        GITDIR.mkdir();
        //File cmt = new File(System.getProperty("user.dir/.gitlet"));
        File committed = Utils.join(GITDIR, committedDirctory);
        committed.mkdir();

        //File stg = new File(System.getProperty("user.dir/.gitlet"));
        File staged = new File(GITDIR, "staged");
        staged.mkdir();

        //File rmd = new File(System.getProperty("user.dir/.gitlet"));
        File removed = new File(GITDIR, "untracking");
        removed.mkdir();

        Tree metadata = new Tree();
        //File pwdd = new File(System.getProperty("user.dir/.gitlet"));
        File mett = new File(GITDIR, "metadata");
        try {
            mett.createNewFile();
        } catch (IOException e) {
            System.out.print("didn't make fie");
        }
        writeObjectToFile(metadata); //"user.dir/.gitlet/metadata"
    }

    public static void add(String file) {
        File thefile = new File(file);
        if (!thefile.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Tree metadata = loadTree();
        ArrayList hashed = metadata.head.branch.node.hashed;
        File path = Utils.join(GITDIR, "untracking");
        File[] untracked = path.listFiles();
        if (untracked != null && untracked.length != 0) {
            for (File f : untracked) {
                if (f.getName().equals(file)) {
                    f.delete();
                }
            }
        }
        //citation-stack-overflow
        File dir = new File(System.getProperty("user.dir"));
        File[] files = dir.listFiles(); //what if this is empty?
        //if (files == null) System.out.println("File does not exist.");
        List filess = Utils.plainFilenamesIn(dir);
        if (filess != null) {
            for (File f : files) {
                if (!f.isFile()) {
                    continue;
                }
                String toRead = Utils.sha1(Utils.readContents(f));
                if (f.getName().equals(file) && !hashed.contains(toRead)) {
                    File absOld = Utils.join(dir, f.getName());
                    File absNew = Utils.join(dir, ".gitlet", "staged", f.getName());
                    try {
                        if (!(absNew.exists() && absNew.isFile())) {
                            absNew.createNewFile();
                        }
                        byte[] inFile = Utils.readContents(absOld);
                        Utils.writeContents(absNew, inFile);
                        //Files.copy(absOld.toPath(), absNew.toPath());
                    } catch (IOException e) {
                        System.out.println("didn't work");
                    }
                    //absOld.renameTo(absNew);

                } else {
                    continue;
                }
            }
        }
    }

    public static void commit(String message) {
        File staged = Utils.join(GITDIR, "staged");
        List<String> stagedFiless = Utils.plainFilenamesIn(staged);
        File ped = Utils.join(GITDIR, "untracking");
        List<String> untracked = Arrays.asList(ped.list());

        if (stagedFiless.isEmpty() && untracked.isEmpty()) {
            System.out.println("No changes added to the commit.");
            return;
        }

        Tree metadata = loadTree();
        File[] stagedFiles = staged.listFiles();
        ArrayList<String> hashedNames = metadata.head.branch.node.hashed;
        ArrayList<String> realNames = metadata.head.branch.node.fileNames;
        ArrayList<String> toBeRemoved = new ArrayList<String>();
        if (stagedFiless != null) {
            for (File file : stagedFiles) {
                String filename = file.getName();
                if (untracked.contains(file.getName())) {
                    toBeRemoved.add(filename);
//                    int index = realNames.indexOf(filename);
//                    realNames.remove(index);
//                    hashedNames.remove(index);
//                    continue;
                }
                if (!file.isFile()) {
                    continue;
                } else {
                    byte[] toSha = Utils.readContents(file);
                    String sHaa = Utils.sha1(toSha);
                    File absOld = Utils.join(GITDIR, "staged", file.getName());
                    File absNew = Utils.join(GITDIR, "committed", sHaa);
                    try {
                        absNew.createNewFile();
                        byte[] inFile = Utils.readContents(absOld);
                        Utils.writeContents(absNew, inFile);
                        //Files.copy(absOld.toPath(), absNew.toPath());
                    } catch (IOException e) {
                        System.out.println("didn't work");
                    }
                    file.delete();
                    if (untracked.contains(file.getName())) {
                        continue;
                    }
                    if (realNames.contains(file.getName())) {
                        int index = realNames.indexOf(filename);
                        hashedNames.set(index, sHaa);
                        continue;
                    } else {
                        realNames.add(file.getName());
                        hashedNames.add(sHaa);
                    }
                }
            }
        }
        metadata.add(message, hashedNames, realNames, untracked);
        metadata.head.branch.node.numNode++;
        deleteDr(ped);
        deleteDr(staged);
        writeObjectToFile(metadata);
    }

    public static void deleteDr(File dir) {
        if (dir.exists()) {
            for (String fileName: Utils.plainFilenamesIn(dir)) {
                File currFile = new File(dir, fileName);
                currFile.delete();
            }
            return;
        } else {
            return;
        }
    }

    public static void clearStageUntracking() {
        File staged = Utils.join(GITDIR, "staged");
        File ped = Utils.join(GITDIR, "untracking");
        deleteDr(ped);
        deleteDr(staged);
    }
    public static void rmHelper(String filename) {
        File absNew = Utils.join(GITDIR, "untracking", filename);
        try {
            if (!absNew.exists()) {
                absNew.createNewFile();
            }
            //Files.copy(absOld.toPath(), absNew.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //file.delete();
        /*Path sd = Paths.get("user.dir/.gitlet/" + filename);
        Path od = Paths.get("user.dir/.gitlet/untracking/" + filename);
        }*/
    }

    public static void rm(String filename) {
        Tree metadata = loadTree();
        File staged = Utils.join(GITDIR, "staged");
        List<String> staging = Utils.plainFilenamesIn(staged);
        ArrayList tracked = metadata.head.branch.node.fileNames;
        //if (staging == null && tracked.contains(filename) == false)
        // System.out.println("No reason to remove the file");
        File toDelete = new File(filename);
        //File toDelete = new File(System.getProperty("user.dir"), filename);
        //File toDelete = Utils.join(gitdir, filename);
        /*if (!toDelete.exists()) {
            System.out.println("No reason to remove the file");
            return;
        }*/
        File toDeleteStage = Utils.join(GITDIR, "staged", filename);
        if (tracked.contains(filename)) {
            rmHelper(filename);
            if (toDelete.exists()) {
                toDelete.delete();
            }
            if (staging != null || toDeleteStage.exists()) {
                toDeleteStage.delete();
            }
        } else if (!tracked.contains(filename) && toDeleteStage.exists()) {
            toDeleteStage.delete();
        } else {
            System.out.println("No reason to remove the file");
        }
    }

    public static void rmBranch(String branchName) {
        Tree metadata = loadTree();
        if (metadata.head.branch.name.equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        for (int i = 0; i < metadata.branches.size(); i += 1) {
            if (metadata.branches.get(i).name.equals(branchName)) {
                metadata.branches.remove(i);
                writeObjectToFile(metadata);
                return;
            }
        }
        System.out.println("A branch with that name does not exist.");
    }

    public static void log() {
        Tree metadata = loadTree();
        Tree.Node p = metadata.head.branch.node;
        while (p != null) {
            System.out.println("===");
            System.out.println("Commit " + p.shaId);
            System.out.println(p.commitTime);
            System.out.println(p.logMessage);
            System.out.println();
            p = p.child;
        }
    }

    public static void globalLogHelper(Tree.Node commit, List<Tree.Node> used) {
        if (commit.parents == null || commit.parents.isEmpty()) {
/*
            if (!used.contains(commit)) {
                System.out.println("Commit " + commit.shaId);
                System.out.println(commit.commitTime);
                System.out.println(commit.logMessage);
                System.out.println();
                used.add(commit);
            }
 */
            return;
        }
        for (Tree.Node parent : commit.parents) {
            if (!used.contains(parent)) {
                System.out.println("Commit " + parent.shaId);
                System.out.println(parent.commitTime);
                System.out.println(parent.logMessage);
                System.out.println();
                used.add(parent);
                globalLogHelper(parent, used);
            }
        }
    }

    public static void globalLog() {
        ArrayList<Tree.Node> used = new ArrayList<Tree.Node>();
        Tree metadata = loadTree();
        Tree.Node p = metadata.head.branch.node;
        while (p.child != null) {
            System.out.println("===");
            System.out.println("Commit " + p.shaId);
            System.out.println(p.commitTime);
            System.out.println(p.logMessage);
            System.out.println();
            used.add(p);
            //Tree.Node[] newUsed = new Tree.Node[used.length + 1];
            //System.arraycopy(used, 0, newUsed, 0, used.length);
            //used = newUsed;
            p = p.child;
        }
        System.out.println("===");
        System.out.println("Commit " + p.shaId);
        System.out.println(p.commitTime);
        System.out.println(p.logMessage);
        System.out.println();
        globalLogHelper(p, used);
    }

    public static void status() {
        Tree metadata = loadTree();
        File dir = new File(System.getProperty("user.dir"));
        System.out.println("=== Branches ===");
        System.out.println("*" + metadata.head.branch.name);
        ArrayList<String> branchNames = new ArrayList<>();
        for (Tree.Branch branch : metadata.branches) {
            if (!branch.name.equals(metadata.head.branch.name)) {
                branchNames.add(branch.name);
            }
        }
        Collections.sort(branchNames);
        for (String name : branchNames) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        File staged = Utils.join(dir, ".gitlet", "staged");
        //File[] stagedFiles = staged.listFiles();
        List<String> staging = Utils.plainFilenamesIn(staged);
        if (staging != null) {
            for (String file : staging) {
                System.out.println(file);
            }
            System.out.println();
        } else {
            System.out.println();
        }

        System.out.println("=== Removed Files ===");
        File rmd = Utils.join(GITDIR, "untracking");
        List<String> untracking = Utils.plainFilenamesIn(rmd);
        if (untracking != null) {
            for (String file : untracking) {
                System.out.println(file);
            }
            System.out.println();
        } else {
            System.out.println();
        }
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public static void branch(String branchName) {
        Tree metadata = loadTree();
        ArrayList<Tree.Branch> listBranch = metadata.branches;
        for (Tree.Branch b : listBranch) {
            if (b.name.equals(branchName)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        metadata.makeBranch(branchName);
        writeObjectToFile(metadata);
        /*
        for (Tree.Branch branch : metadata.branches) {
            if (branch.name.equals(branchName)) {
                System.out.println("A branch with that name already exists.");
                return;
            } else {
                metadata.makeBranch(branchName);
                writeObjectToFile(metadata);
            }
        }
         */
    }

    public static int findHelper(
            Tree.Node commit,
            ArrayList<Tree.Node> used, String message) {
        if (commit.parents == null || commit.parents.isEmpty()) {
/*
            if (!(used.contains(message)) &&
                    commit.logMessage.equals(message)) {
                return 1;
            }
 */
            return 0;
        }
        int tmp = 0;
        for (Tree.Node parent : commit.parents) {
            if (!(used.contains(parent))) {
                //System.out.println("===");
                if (parent.logMessage.equals(message)) {
                    System.out.println(parent.shaId);
                    tmp += 1;
                }
                tmp += findHelper(parent, used, message);
            }
        }
        return tmp;
    }

    public static void find(String message) {
        int num = 0;
        ArrayList<Tree.Node> used = new ArrayList<Tree.Node>();
        Tree metadata = loadTree();
        Tree.Node p = metadata.head.branch.node;
        while (p.child != null) {
            if (p.logMessage.equals(message)) {
                System.out.println(p.shaId);
                num += 1;
            }
            used.add(p);
            p = p.child;
        }
        if (p.logMessage.equals(message)) {
            System.out.println(p.shaId);
            num += 1;
            //System.out.println();
        }
        num += findHelper(p, used, message);
        if (num == 0) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }

    public static Tree.Node resetHelper(Tree.Node commit, ArrayList used, String commitId) {
        Tree.Node toReturn = commit;
        if (commit.parents.isEmpty()) {
/*
            if (!used.contains(commit) && commit.shaId.equals(commitId)) {
                return commit;
            }
 */
            return null;
        }
        for (Tree.Node parent : commit.parents) {
            if (!used.contains(parent)) {
                if (parent.shaId.equals(commitId)) {
                    return parent;
                }
                used.add(parent);
                Tree.Node dum = resetHelper(parent, used, commitId);
                if (dum != null) {
                    return dum;
                }
            }
        }
        return null;
    }

    public static byte[] byteContent(Tree.Node node, String filename) {
        int index = node.fileNames.indexOf(filename);
        String id = node.hashed.get(index);
        File target = Utils.join(GITDIR, "committed", id);
        return Utils.readContents(target);
    }

    public static boolean committed(Tree.Node currNode, String filename) {
        return currNode.fileNames.contains(filename);
    }

    public static void reset(String commitId) {
        Tree metadata = loadTree();
        boolean idTrue = false;
        ArrayList used = new ArrayList();
        Tree.Node p = metadata.head.branch.node;
        Tree.Node curr = metadata.head.branch.node;
        while (p.child != null) {
            if (p.shaId.equals(commitId)) {
                idTrue = true;
                break;
            }
            used.add(p);
            p = p.child;
        }
        if (!idTrue) {
            p = resetHelper(p, used, commitId);
            if (p == null) {
                System.out.println("No commit with that id exists.");
                return;
            } else {
                for (String filename : p.fileNames) {
                    File f = new File(filename);
                    if (f.exists() && !committed(curr, filename)) {
                        System.out.println("There is an untracked file in the way; "
                                +
                                "delete it or add it first.");
                        return;
                    }
                }
            }
            for (String n : curr.fileNames) {
                if (!p.fileNames.contains(n)) {
                    Utils.restrictedDelete(n);
                }
            }
            for (String n : p.fileNames) {
                Utils.writeContents(new File(n), byteContent(p, n));
            }

        }
        metadata.head.branch.node = p;
        File staged = Utils.join(GITDIR, "staged");
        File ped = Utils.join(GITDIR, "untracking");
        deleteDr(ped);
        deleteDr(staged);
        writeObjectToFile(metadata);
    }

    public static boolean isTracked(Tree.Node curr, String file) {
        File untracking = Utils.join(GITDIR + "untracking" + file);
        return (curr.fileNames.contains(file) && !untracking.exists());
    }

    public static boolean untrackCheck(Tree.Node newNode, Tree.Node currNode) {
        for (String filename : newNode.fileNames) {
            File file = new File(filename);
            if (file.exists() && !isTracked(currNode, filename)) {
                System.out.println("There "
                        + "is an untracked file in the way; delete it or add it first.");
                return true;
            }
        }
        return false;
    }

    public static void checkoutBranch(String branchName) {
        Tree metadata = loadTree();
        Tree.Node currNode = metadata.head.branch.node;
        boolean flag = false;
        Tree.Branch newBranch = null;
        for (Tree.Branch branch : metadata.branches) {
            if (branch.name.equals(branchName)) {
                flag = true;
                newBranch = branch;
                break;
            }
        }
        if (!flag) {
            System.out.println("No such branch exists.");
            return;
        } else if (metadata.head.branch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Tree.Node node = newBranch.node;
        if (untrackCheck(node, currNode)) {
            return;
        }

        for (String filename : currNode.fileNames) {
            if (!node.fileNames.contains(filename)) {
                Utils.restrictedDelete(filename);
            }
        }
        int len = node.fileNames.size();
        for (int i = 0; i < len; i += 1) {
            String fileID = node.hashed.get(i);
            File file = Utils.join(GITDIR, committedDirctory + fileID);
            Utils.writeContents(new File(node.fileNames.get(i)), Utils.readContents(file));
        }
        metadata.head.branch = newBranch;
        clearStageUntracking();
        writeObjectToFile(metadata);
    }

    public static void checkoutFile(String fileName) {
        Tree.Node curr = loadTree().head.branch.node;
        int index = curr.fileNames.indexOf(fileName);
        String fileID = curr.hashed.get(index);
        File file = Utils.join(GITDIR, committedDirctory, fileID);
        if (!file.exists()) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Utils.writeContents(new File(fileName), Utils.readContents(file));
    }

    public static boolean checkoutFileHelper(Tree metadata,
                                             Tree.Node commit,
                                             List<Tree.Node> used,
                                             String commitId, String fileName) {
        boolean flag = false;

        if (commit.parents == null || commit.parents.isEmpty()) {
            if (commit.shaId.equals(commitId)) {
                checkFileExist(commit, fileName);
                return true;
            }
            return false;
        }
        for (Tree.Node parent : commit.parents) {
            if (!used.contains(parent)) {
                if (parent.shaId.equals(commitId)) {
                    checkFileExist(parent, fileName);
                    return true;
                }
                used.add(parent);
                flag = checkoutFileHelper(metadata, parent, used, commitId, fileName);
                if (flag) {
                    break;
                }
            }
        }
        return flag;
    }

    public static boolean checkoutFileHelperShort(Tree metadata,
                                                  Tree.Node commit,
                                                  List<Tree.Node> used,
                                                  String commitId, String fileName) {
        boolean flag = false;

        if (commit.parents == null || commit.parents.isEmpty()) {
            if (commit.shaId.startsWith(commitId)) {
                checkFileExist(commit, fileName);
                return true;
            }
            return false;
        }
        for (Tree.Node parent : commit.parents) {
            if (!used.contains(parent)) {
                if (parent.shaId.startsWith(commitId)) {
                    checkFileExist(parent, fileName);
                    return true;
                }
                used.add(parent);
                flag = checkoutFileHelper(metadata, parent, used, commitId, fileName);
                if (flag) {
                    break;
                }
            }
        }
        return flag;
    }


    public static void checkFileExist(Tree.Node node, String filename) {
        int len = node.fileNames.size();
        for (int i = 0; i < len; i += 1) {
            if (node.fileNames.get(i).equals(filename)) {
                String id = node.hashed.get(i);
                File src = Utils.join(GITDIR, committedDirctory, id);
                Utils.writeContents(new File(node.fileNames.get(i)),
                        Utils.readContents(src));
                return;
            }
        }
        System.out.println("File does not exist in that commit.");
        return;
    }

    public static void checkoutCommitFile(String commitId, String fileName) {
        if (commitId.length() == 8) {
            checkoutCommitFileShortID(commitId, fileName);
            return;
        }
        checkoutCommitFileFullID(commitId, fileName);
        return;
    }

    public static void checkoutCommitFileFullID(String commitId, String fileName) {
        Tree metadata = loadTree();
        ArrayList<Tree.Node> used = new ArrayList<Tree.Node>();
        Tree.Node p = metadata.head.branch.node;
        while (p.child != null) {
            if (p.shaId.equals(commitId)) {
                checkFileExist(p, fileName);
                return;
            }
            used.add(p);
            p = p.child;
        }
        checkFileExist(p, fileName);
        if (!checkoutFileHelper(metadata, p, used, commitId, fileName)) {
            System.out.println("No commit with that id exists. ");
            return;
        }
    }

    public static void checkoutCommitFileShortID(String commitId, String fileName) {
        Tree metadata = loadTree();
        ArrayList<Tree.Node> used = new ArrayList<Tree.Node>();
        Tree.Node p = metadata.head.branch.node;

        while (p.child != null) {
            if (p.shaId.startsWith(commitId)) {
                checkFileExist(p, fileName);
                checkFileExist(p, fileName);
                return;
            }
            used.add(p);
            p = p.child;
        }
        checkFileExist(p, fileName);
        if (!checkoutFileHelperShort(metadata, p, used, commitId, fileName)) {
            System.out.println("No commit with that id exists. ");
            return;
        }
    }

    public static Tree.Node splitP(Tree.Branch b1, Tree.Branch b2) {
        if (b1 == null || b2 == null) {
            return null;
        }
        Tree.Node n1 = b1.node;
        Tree.Node n2 = b2.node;
        while (n1.numNode > n2.numNode) {
            n1 = n1.child;
        }
        while (n2.numNode > n1.numNode) {
            n2 = n2.child;
        }
        while (!n1.shaId.equals(n2.shaId)) {
            n1 = n1.child;
            n2 = n2.child;
        }
        return n1;
    }

    public static void merge(String branchName) {
        isConflicted = false;
        File staged = Utils.join(GITDIR, "staged");
        File untracked = Utils.join(GITDIR, "untracking");
        if (!Utils.fileisEmpty(staged) || !Utils.fileisEmpty(untracked)) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        Tree metadata = loadTree();
        Tree.Branch newBranch = null;
        Tree.Branch currBranch = metadata.head.branch;

        //if (untrackCheck(metadata)) {
        //            return;
        //        }
        boolean flag = false;
        Tree.Node node = metadata.head.branch.node;
        for (Tree.Branch branch : metadata.branches) {
            if (branch.name.equals(branchName)) {
                flag = true;
                newBranch = branch;
            }
        }
        if (!flag) {
            System.out.println("No such branch exists.");
            return;
        }
        if (metadata.head.branch.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        //fixme
        /*
        if (untrackCheck(metadata)) {
            return;
        }
         */
        Tree.Node newNode = newBranch.node;
        Tree.Node currNode = currBranch.node;
        Tree.Node split = splitP(newBranch, currBranch);
        if (split.shaId.equals(newNode.shaId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (split.shaId.equals(currNode.shaId)) {
            String currentCommit = newNode.shaId;
            currBranch.node = split;
            metadata.head.branch = currBranch;
            reset(currentCommit);
            System.out.println("Current branch fast-forwarded..");
            return;
        }
        split2curr(split, currNode, newNode, newBranch);
        curr2split(split, currNode, newNode, newBranch);
        new2split(split, currNode, newNode, newBranch);
        if (!isConflicted) {
            commit("Merged " + currBranch.name + " with " + branchName
                    + ".");
        }
        writeObjectToFile(metadata);
    }

    public static void writefile(String filename, Tree.Node node) {
        int index = node.fileNames.indexOf(filename);
        String hashId = node.hashed.get(index);
        File src = Utils.join(GITDIR, "committed", hashId);
        File dest = new File(filename);
        dest.delete();
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);
            // buffer size 1K
            byte[] buf = new byte[1024];

            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        } catch (IOException e) {
            System.out.println("Didn't work");
        }
    }

    public static void conflict(String filename, Tree.Branch b, Tree.Node currNode) {
        if (!isConflicted) {
            isConflicted = !isConflicted;
            System.out.println("Encountered a merge conflict.");
        }
        File file = new File(filename);
        String ret = "<<<<<<< HEAD\n";
        if (!file.exists()) {
            Utils.writeContents(file, new byte[]{});
        }
        int index = currNode.fileNames.indexOf(filename);
        String fileId = currNode.hashed.get(index);
        File thisFile = Utils.join(GITDIR, "committed", fileId);
        byte[] contents = Utils.readContents(thisFile);
        if (currNode.fileNames.contains(filename)) {
            ret = ret + new String(contents);
        }
        ret += ("=======\n");
        Tree.Node bNode = b.node;
        int bIndex = bNode.fileNames.indexOf(filename);
        String bFileId = bNode.hashed.get(bIndex);
        File bFile = Utils.join(GITDIR, "committed", bFileId);
        byte[] bContents = Utils.readContents(thisFile);
        if (bNode.fileNames.contains(filename)) {
            ret = ret + new String(bContents);
        }
        ret += (">>>>>>>\n");
        Utils.writeContents(file, ret.getBytes());
    }

    public static void split2curr(Tree.Node split, Tree.Node currNode,
                                  Tree.Node newNode, Tree.Branch newBranch) {
        for (String name : split.fileNames) {
            int index = currNode.fileNames.indexOf(name);
            int index2 = split.fileNames.indexOf(name);
            int index3 = newNode.fileNames.indexOf(name);
            if (currNode.fileNames.contains(name)) {
                if (split.hashed.get(index2).equals(currNode.hashed.get(index))) {
                    if (!newNode.fileNames.contains(name)) {
                        rm(name);
                    } else if (split.hashed.get(index2)
                            .equals(newNode.hashed.get(index3))) {
                        writefile(name, newNode);
                        add(name);
                    }
                } else {
                    if (newNode.fileNames.contains(name)
                            && newNode.hashed.get(index3).equals(split.hashed.get(index2))) {
                        continue;
                    }
                    conflict(name, newBranch, currNode);
                }
            } else {
                if (newNode.fileNames.contains(name)
                        && newNode.hashed.get(index3).equals(split.hashed.get(index2))) {
                    continue;
                }
                conflict(name, newBranch, currNode);
            }
        }
    }

    public static void curr2split(Tree.Node split, Tree.Node currNode,
                                  Tree.Node newNode, Tree.Branch newOne) {
        for (String name : currNode.fileNames) {
            if (!split.fileNames.contains(name)) {
                if (!newNode.fileNames.contains(name)) {
                    continue;
                } else {
                    int newI = newNode.fileNames.indexOf(name),
                            currI = currNode.fileNames.indexOf(name);
                    if (!newNode.hashed.get(newI)
                            .equals(currNode.hashed.get(currI))) {
                        conflict(name, newOne, currNode);
                    }
                }
            }
        }
    }


    public static void new2split(Tree.Node split, Tree.Node currNode,
                                 Tree.Node newNode, Tree.Branch newOne) {
        for (String name : newNode.fileNames) {
            if (!split.fileNames.contains(name)) {
                if (!currNode.fileNames.contains(name)) {
                    File file = new File(name);

                    int index = newNode.fileNames.indexOf(name);
                    String fileId = newNode.hashed.get(index);
                    File thisFile = Utils.join(GITDIR, "committed", fileId);
                    byte[] contents = Utils.readContents(thisFile);
                    Utils.writeContents(file, contents);
                    add(name);
                }
            }
        }
    }
}
