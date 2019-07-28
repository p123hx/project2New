package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tree implements Serializable {
    public ArrayList<Branch> branches; //does branches need a size?
    public ArrayList<Node> allNodes;
    public Head head;

    public class Branch implements Serializable {
        public Node node;
        public String name;
        public Branch(Node node, String name) {
            this.node = node;
            this.name = name;
        }
    }
    public class Head implements Serializable {
        public Branch branch;
        public Head(Branch branch) {
            this.branch = branch;
        }
    }

    public Tree() {
        Node firstCommit = new Node("initial commit");
        Branch master = new Branch(firstCommit, "master");
        Head heed = new Head(master);
        this.head = heed;
        this.branches = new ArrayList<Branch>();
        this.branches.add(master);
        this.allNodes = new ArrayList<>();
        this.allNodes.add(firstCommit);
    }
    public void add(String message, ArrayList<String> hashedNames,
                    ArrayList<String> realNames, List untracked) {
        Node old = this.head.branch.node;
        Node adding = new Node(message, hashedNames, realNames);
        adding.child = this.head.branch.node;
        this.head.branch.node = adding;
        old.parents.add(adding);
        /*for (String hash : old.hashed) {
            if (!this.head.branch.node.hashed.contains(hash))
             {//contains uses objects? need .equals()?
                this.head.branch.node.hashed.add(hash);
            }
        }*/
        for (String file : old.fileNames) {
            if (!this.head.branch.node.fileNames.contains(file) && !untracked.contains(file)) {
                this.head.branch.node.fileNames.add(file);
                int index = old.fileNames.indexOf(file);
                this.head.branch.node.hashed.add(old.hashed.get(index));
            }
        }
        allNodes.add(adding);
    }

    public void makeBranch(String branchName) {
        Tree.Branch newBranch = new Tree.Branch(this.head.branch.node, branchName);
        this.branches.add(newBranch);
    }


    public class Node implements Serializable {
        public Node child;
        public int numNode = 0;
        public ArrayList<Node> parents;
        public String logMessage;
        public String commitTime;
        public String shaId;
        //Map<String, String> fileNamesHashed
        public ArrayList<String> fileNames;
        public ArrayList<String> hashed;
        public Node(String msg) {
            numNode += 1;
            this.child = null;
            this.parents = new ArrayList<Node>();
            this.logMessage = msg;
            this.fileNames = new ArrayList<>();
            this.hashed = new ArrayList<>();
            this.parents = new ArrayList<Node>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            this.commitTime = dtf.format(now);
            ArrayList toSha = new ArrayList();
            toSha.add(this.logMessage);
            toSha.add(this.commitTime.toString());
            this.shaId = Utils.sha1(toSha); //does this need all the variables, not just "this"?
        }
        public Node(String msg, ArrayList<String> hashedFileNames, ArrayList<String> realNames) {
            this.logMessage = msg;
            this.fileNames = realNames;
            this.hashed = hashedFileNames;
            this.parents = new ArrayList<Node>();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime now = LocalDateTime.now();
            this.commitTime = dtf.format(now);
            ArrayList toSha = new ArrayList();
            if (!fileNames.isEmpty()) {
                for (int i = 0; i < fileNames.size(); i += 1) {
                    toSha.add(fileNames.get(i));
                    toSha.add(hashed.get(i));
                }
            }
            toSha.add(this.logMessage);
            toSha.add(this.commitTime.toString());
            this.shaId = Utils.sha1(toSha); //does this need all the variables, not just "this"?
        }

        @Override
        public String toString() {
            String output = "===" + "\n" + "Commit "
                    + this.shaId + "\n" + this.commitTime
                    + "\n" + this.logMessage
                    + "\n";
            return output;
        }

    }
}
