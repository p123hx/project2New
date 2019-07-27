package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Tree implements Serializable {
    public ArrayList<Branch> branches; //does branches need a size?
    public ArrayList<Node> allNodes;
    public Branch currBranch;

    public class Branch implements Serializable {
        public Node headNode;
        public String name;
        public Branch(Node node, String name) {
            this.headNode = node;
            this.name = name;
        }
    }

    public Tree() {
        Node firstCommit = new Node("initial commit");
        this.currBranch = new Branch(firstCommit, "master");
        this.branches = new ArrayList<>();
        this.branches.add(currBranch);
        this.allNodes = new ArrayList<>();
        this.allNodes.add(firstCommit);
    }
    public void add(String message, ArrayList<String> hashedNames,
                    ArrayList<String> realNames) {
        Node old = this.currBranch.headNode;
        Node adding = new Node(message, hashedNames, realNames);
        adding.child = old;
        this.currBranch.headNode = adding;
        allNodes.add(adding);
    }

    public void makeBranch(String branchName) {
        Tree.Branch newBranch = new Tree.Branch(this.currBranch.headNode, branchName);
        this.branches.add(newBranch);
    }


    public class Node implements Serializable {
        public Node child;
        public int numNode = 0;
        public String logMessage;
        public String commitTime;
        public String shaId;
        public ArrayList<String> fileNames;
        public ArrayList<String> hashed;
        public Node(String msg) {
            numNode += 1;
            this.child = null;
            this.logMessage = msg;
            this.fileNames = new ArrayList<>();
            this.hashed = new ArrayList<>();
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
            this.numNode += 1;
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
