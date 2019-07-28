package gitlet;

import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class GitletEngine implements Serializable {
    private static boolean isConflicted = false;
    public static final File GITDIR = new File(".gitlet/");
    public static File committedDirctory = new File(GITDIR, "committed");
    public static File stagedDirctory = new File(GITDIR, "staged");
    public static File untrackingDirctory = new File(GITDIR, "untracking");

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

    public static void creatFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writeFIle2File(File src, File dest) {
        Utils.writeContents(dest, Utils.readContents(src));
    }

    public static void writeCommitted2Working(String filename, Tree.Node node) {
        int index = node.fileNames.indexOf(filename);
        String hashId = node.hashed.get(index);
        File src = new File(committedDirctory, hashId);
        File dest = new File(filename);
        Utils.writeContents(dest, Utils.readContents(src));
    }

    public static void init() {
        GITDIR.mkdir();
        committedDirctory.mkdir();
        stagedDirctory.mkdir();
        untrackingDirctory.mkdir();
        Tree metadata = new Tree();
        File mett = new File(GITDIR, "metadata");
        try {
            mett.createNewFile();
        } catch (IOException e) {
            System.out.print("didn't make fie");
        }
        writeObjectToFile(metadata);
    }

    public static void add(String file) {
        //fixme
        Tree metadata = loadTree();
        Tree.Node currNode = metadata.currBranch.headNode;
        File fileWorking = new File(file);
        if (!fileWorking.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        String fileID = Utils.sha1(Utils.readContents(fileWorking), fileWorking.getName());
        File fileStaged = new File(stagedDirctory, file);
        File fileUntracking = new File(untrackingDirctory, file);
        if (!(currNode.hashed.contains(fileID)) || fileUntracking.exists()) {
            if (fileUntracking.exists()) {
                fileUntracking.delete();
                return;
            }
            creatFile(fileStaged);
            writeFIle2File(fileWorking, fileStaged);
        }
    }

    public static void commit(String message) {
        Tree metadata = loadTree();
        Tree.Node currNode = metadata.currBranch.headNode;
        ArrayList<String> hashedNames = currNode.hashed;
        ArrayList<String> realNames = currNode.fileNames;
        List<String> untracked = Utils.plainFilenamesIn(untrackingDirctory);
        List<String> stagedFiles = Utils.plainFilenamesIn(stagedDirctory);
        if (Utils.fileisEmpty(untrackingDirctory) && Utils.fileisEmpty(stagedDirctory)) {
            System.out.println("No changes added to the commit.");
            return;
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        if (!Utils.fileisEmpty(untrackingDirctory)) {
            for (String name : untracked) {
                if (realNames.contains(name)) {
                    int index = realNames.indexOf(name);
                    String id = hashedNames.get(index);
                    realNames.remove(id);
                    hashedNames.remove(id);
                }
            }
            for (String string : stagedFiles) {
                realNames.add(string);
                File fileStaged = new File(stagedDirctory, string);
                String fileID = Utils.sha1(Utils.readContents(fileStaged), fileStaged.getName());
                File fileCommitted = new File(committedDirctory, fileID);
                creatFile(fileCommitted);
                writeFIle2File(fileStaged, fileCommitted);
                hashedNames.add(fileID);
            }
            metadata.add(message, hashedNames, realNames);
        }
        clearStageUntracking();
        writeObjectToFile(metadata);
    }
//fixme
    public static void deleteDr(File dir) {
        if (dir.exists()) {
            for (String fileName: Utils.plainFilenamesIn(dir)) {
                File currFile = new File(dir, fileName);
                currFile.delete();
            }
        }
    }

    public static void clearStageUntracking() {
        deleteDr(untrackingDirctory);
        deleteDr(stagedDirctory);
    }

    public static void rm(String filename) {
        Tree metadata = loadTree();
        Tree.Node currNode = metadata.currBranch.headNode;
        ArrayList tracked = currNode.fileNames;
        //fixme

        File toDelete = new File(filename);
        File toDeleteStage = new File(stagedDirctory, filename);
        File fileUntracked = new File(untrackingDirctory, filename);
        if (!isTracked(currNode, filename) && !toDeleteStage.exists()) {
            System.out.println("No reason to remove the file");
            return;
        }
        if (tracked.contains(filename)) {
            creatFile(fileUntracked);
            Utils.restrictedDelete(filename);
        }
        if (toDeleteStage.exists()) {
            toDeleteStage.delete();
        }
    }

    public static void rmBranch(String branchName) {
        Tree metadata = loadTree();
        if (metadata.currBranch.name.equals(branchName)) {
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
        Tree.Node p = metadata.currBranch.headNode;
        while (p != null) {
            System.out.println(p);
            p = p.child;
        }
    }

    public static void globalLog() {
        Tree metadata = loadTree();
        ArrayList<Tree.Node> allNodes = metadata.allNodes;
        for (Tree.Node node : allNodes) {
            System.out.println(node);
        }
    }

    public static void status() {
        Tree metadata = loadTree();
        System.out.println("=== Branches ===");
        System.out.println("*" + metadata.currBranch.name);
        ArrayList<String> branchNames = new ArrayList<>();
        for (Tree.Branch branch : metadata.branches) {
            if (!branch.name.equals(metadata.currBranch.name)) {
                branchNames.add(branch.name);
            }
        }
        Collections.sort(branchNames);
        for (String name : branchNames) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        List<String> staging = Utils.plainFilenamesIn(stagedDirctory);
        if (staging != null) {
            for (String file : staging) {
                System.out.println(file);
            }
            System.out.println();
        } else {
            System.out.println();
        }

        System.out.println("=== Removed Files ===");
        List<String> untracking = Utils.plainFilenamesIn(untrackingDirctory);
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
    }

    public static void find(String message) {
        int count = 0;
        Tree metadata = loadTree();
        for (Tree.Node node : metadata.allNodes) {
            if (node.logMessage.equals(message)) {
                System.out.println(node.shaId);
                count += 1;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static void reset(String commitId) {
        Tree.Node newNode = null;
        Tree metadata = loadTree();
        ArrayList<Tree.Node> allNodes = metadata.allNodes;
        Tree.Node currNode = metadata.currBranch.headNode;
        if (commitId.length() == 6) {
            for (Tree.Node node : allNodes) {
                //fixme
                if (node.shaId.startsWith(commitId)) {
                    newNode = node;
                    break;
                }
            }
        } else {
            for (Tree.Node node : allNodes) {
                if (node.shaId.equals(commitId)) {
                    newNode = node;
                    break;
                }
            }
        }

        if (newNode == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        for (String name : newNode.fileNames) {
            File file = new File(name);
            if (file.exists() && !isTracked(currNode, name)) {
                System.out.println("There is an untracked file in the way; "
                        +
                        "delete it or add it first.");
                return;
            }
        }
        for (String fileName : currNode.fileNames) {
            if (!newNode.fileNames.contains(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        for (String fileName : newNode.fileNames) {
            writeCommitted2Working(fileName, newNode);
        }
        metadata.currBranch.headNode = newNode;
        clearStageUntracking();
        writeObjectToFile(metadata);
    }

    public static boolean isTracked(Tree.Node curr, String file) {
        File untracking = new File(untrackingDirctory, file);
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
        Tree.Node currNode = metadata.currBranch.headNode;
        Tree.Branch newBranch = null;
        for (Tree.Branch branch : metadata.branches) {
            if (branch.name.equals(branchName)) {
                newBranch = branch;
                break;
            }
        }
        if (newBranch == null) {
            System.out.println("No such branch exists.");
            return;
        } else if (metadata.currBranch.name.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Tree.Node node = newBranch.headNode;
        if (untrackCheck(node, currNode)) {
            return;
        }

        for (String filename : currNode.fileNames) {
            if (!node.fileNames.contains(filename)) {
                Utils.restrictedDelete(filename);
            }
        }
        int len = node.fileNames.size();
        for (String filename : node.fileNames) {
            writeCommitted2Working(filename, node);
        }
        metadata.currBranch = newBranch;
        clearStageUntracking();
        writeObjectToFile(metadata);
    }

    public static void checkoutFile(String fileName) {
        Tree.Node curr = loadTree().currBranch.headNode;
        if (!curr.fileNames.contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        writeCommitted2Working(fileName, curr);
    }

    public static void checkoutCommitFile(String commitId, String fileName) {
        Tree.Node newNode = null;
        Tree metadata = loadTree();
        ArrayList<Tree.Node> allNodes = metadata.allNodes;
        if (commitId.length() == 8) {
            for (Tree.Node node : allNodes) {
                if (node.shaId.startsWith(commitId)) {
                    newNode = node;
                    break;
                }
            }
        } else {
            for (Tree.Node node : allNodes) {
                if (node.shaId.equals(commitId)) {
                    newNode = node;
                    break;
                }
            }
        }
        if (newNode == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        if (!newNode.fileNames.contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        writeCommitted2Working(fileName, newNode);
    }

    public static Tree.Node splitP(Tree.Branch b1, Tree.Branch b2) {
        if (b1 == null || b2 == null) {
            return null;
        }
        Tree.Node n1 = b1.headNode;
        Tree.Node n2 = b2.headNode;
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
        Tree metadata = loadTree();
        Tree.Branch newBranch = null;
        Tree.Branch currBranch = metadata.currBranch;
        Tree.Node currNode = currBranch.headNode;

        for (Tree.Branch branch : metadata.branches) {
            if (branch.name.equals(branchName)) {
                newBranch = branch;
                break;
            }
        }
        if (newBranch == null) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currBranch.name.equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (!Utils.fileisEmpty(stagedDirctory) || !Utils.fileisEmpty(untrackingDirctory)) {
            System.out.println("You have uncommitted changes.");
            return;
        }

        Tree.Node newNode = newBranch.headNode;
        Tree.Node splitNode = splitP(currBranch, newBranch);
        File parentFolder = new File(".").getAbsoluteFile();
        for (File file : parentFolder.listFiles()) {
            String filename = file.getName();
            if (!file.isFile()) {
                continue;
            }
            if (!isTracked(currNode, filename)) {
                if (newNode.fileNames.contains(filename)) {
                    String currId = currNode.hashed.get(currNode.fileNames.indexOf(filename));
                    String workingID = Utils.sha1(Utils.readContents(file), file.getName());
                    if (!currId.equals(workingID)) {
                        System.out.println("There is an untracked file in "
                                +
                                "the way; delete it or add it first.");
                        return;
                    }
                }
                if (!newNode.fileNames.contains(filename)
                        && splitNode.fileNames.contains(filename)) {
                    System.out.println("There is an untracked file in "
                            +
                            "the way; delete it or add it first.");
                    return;
                }
            }
        }
        if (splitNode.shaId.equals(newNode.shaId)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (splitNode.shaId.equals(currNode.shaId)) {
            String newCommitID = newNode.shaId;
            metadata.currBranch.headNode = newNode;
            reset(newCommitID);
            System.out.println("Current branch fast-forwarded..");
            return;
        }
        split2curr(splitNode, currNode, newNode, newBranch);
        curr2split(splitNode, currNode, newNode, newBranch);
        new2split(splitNode, currNode, newNode, newBranch);
        if (!isConflicted) {
            commit("Merged " + currBranch.name + " with " + branchName
                    + ".");
        }
        writeObjectToFile(metadata);
    }

    public static void conflict(String filename, Tree.Branch b, Tree.Node currNode) {
        if (!isConflicted) {
            isConflicted = true;
            System.out.println("Encountered a merge conflict.");
        }
        File file = new File(filename);
        String ret = "<<<<<<< HEAD\n";
        if (!file.exists()) {
            Utils.writeContents(file, new byte[]{});
        }
        int index = currNode.fileNames.indexOf(filename);
        String fileId = currNode.hashed.get(index);
        File thisFile = new File(committedDirctory, fileId);
        byte[] contents = Utils.readContents(thisFile);
        if (currNode.fileNames.contains(filename)) {
            ret = ret + new String(contents);
        }
        ret += ("=======\n");
        Tree.Node bNode = b.headNode;
        int bIndex = bNode.fileNames.indexOf(filename);
        String bFileId = bNode.hashed.get(bIndex);
        File bFile = new File(committedDirctory, bFileId);
        byte[] bContents = Utils.readContents(bFile);
        if (bNode.fileNames.contains(filename)) {
            ret = ret + new String(bContents);
        }
        ret += (">>>>>>>\n");
        Utils.writeContents(file, ret.getBytes());
    }

    public static void split2curr(Tree.Node split, Tree.Node currNode,
                                  Tree.Node newNode, Tree.Branch newBranch) {
        for (String name : split.fileNames) {
            int indexCurr = currNode.fileNames.indexOf(name);
            int indexSplit = split.fileNames.indexOf(name);
            int indexNew = newNode.fileNames.indexOf(name);
            if (currNode.fileNames.contains(name)) {
                if (split.hashed.get(indexSplit).equals(currNode.hashed.get(indexCurr))) {
                    if (!newNode.fileNames.contains(name)) {
                        rm(name);
                    } else if (!split.hashed.get(indexSplit)
                            .equals(newNode.hashed.get(indexNew))) {
                        writeCommitted2Working(name, newNode);
                        add(name);
                    }
                } else {
                    if (newNode.fileNames.contains(name)
                            && newNode.hashed.get(indexNew).equals(split.hashed.get(indexSplit))) {
                        continue;
                    }
                    conflict(name, newBranch, currNode);
                }
            } else {
                if (newNode.fileNames.contains(name)
                        && newNode.hashed.get(indexNew).equals(split.hashed.get(indexSplit))) {
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
                    writeCommitted2Working(name, newNode);
                    add(name);
                }
            }
        }
    }
}
