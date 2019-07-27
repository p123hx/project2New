package gitlet;

public class Ideas {
    /*public void reset(String commitID) {
        Tree metadata = loadTree();
        Node currNode = metadata.currBranch.node;
        while (!currNode.id.equals(commitID)) {
            currNode = currNode.child[0];
            if (currNode.logMessage.equals("initial commit")) {
                System.out.println("No commit with that id exists");
            }
        }
        metadata.currBranch.node = currNode;
        metadata.head.node = currNode //do we have redundancy in our code?
        for (fileID : commitStoredFiles) {
            checkout(fileID);
        }
        for (file : stagingArea) {rm} //i need to look up how to do this
        //insert "There is an untracked file in the way; delete it or add it first"
    }*/

    /*Tree obj;
        File inFile = new File("user.dir/.gitlet/metadata");
        try {
            ObjectOutputStream inp = new ObjectOutputStream(new FileInputStream(inFile));
            obj = (Tree) inp.();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            obj = null;
        }
        return Tree tree = (Tree) inp.readObject();*/

     /*public boolean checkFile(String name) {
        Tree obj;
        File inFile = new File("./gitlet/metadata");
        boolean isEqual = false;

        try {
            ObjectInputStream inp = new ObjectInputStream(new FileInputStream(inFile));
            obj = (Tree) inp.readObject();
            Map<String,File> map = obj.currBranch.node.map;
            File dst = map.get(name);
            File sr = new File(name);
            FileReader fr = new FileReader(dst);
            FileReader fr2 = new FileReader(sr);
            isEqual =fr.equals(fr2);
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            obj = null;
        }
        return isEqual;
    }*/

    /*try {
                            Files.move(Paths.get(absOld), Paths.get(absNew));
                        } catch (IOException e) { System.out.println("didn't work"); }*/

    //Path sd = Paths.get("user.dir/" + file);
    //Path od = Paths.get("user.dir/.gitlet/staged/" + file);
    //try {
    //   Files.copy(sd, od);
    //} catch (Exception e) {
    //   e.printStackTrace(); //what even is this?
    //}

    //File pwd = new File(System.getProperty("user.dir"));
    //List<String> filesNames = Utils.plainFilenamesIn(pwd); //what if this is empty?
    //if (filesNames == null) System.out.println("File does not exist.");

    /*try {
                    Path temp = Files.move(Paths.get(absOld), Paths.get(absNew));
                    file.delete();
                } catch (IOException e) { System.out.println("didn't work"); }*/

    //File newFile = Utils.join(System.getProperty("user.dir"),
    // ".gitlet", "committed", file.getName());
    //File dummyFile = Utils.join(System.getProperty("user.dir"),
    // "gitlet", "committed", Utils.sha1(file));
    //newFile.renameTo(dummyFile);
    //dummyFile.delete();
    //file.delete();

    /*Path sd = Paths.get("user.dir/gitlet" + file.getName());
                Path od = Paths.get("user.dir/.gitlet/committed/" + file.getName());
                try {
                    Files.copy(sd, od);
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

    //File old = new File(absOld);
    //File nu = new File(absNew);
    //absOld.renameTo(absNew);

    /*for (String untr : untracked) {
            if (this.head.branch.node.fileNames.contains(untr)) {
                int index = this.head.branch.node.fileNames.indexOf(untr);
                this.head.branch.node.fileNames.remove(index);
                this.head.branch.node.hashed.remove(index);
            }
        }*/

    //USED TO BE CHECKOUT BRAHCN
    /*
        File dir = new File(System.getProperty("user.dir"));
        File[] files = dir.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
        int len = node.fileNames.size();
        for (int i = 0; i < len; i++) {
            String filename = node.fileNames.get(i);
            String hashId = node.hashed.get(i);
            File src = Utils.join(GITDIR, "committed", hashId);
            File dest = new File(filename);
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
                System.out.println("didn't work");
            }
            metadata.head.branch = newBranch;
        }
        File ped = Utils.join(dir, ".gitlet", "untracking");
        File[] untrachfiles = ped.listFiles();
        for (File file : untrachfiles) {
            file.delete();
        }
        File stag = Utils.join(dir, ".gitlet", "staged");
        File[] stags = stag.listFiles();
        for (File file : stags) {
            file.delete();
        }
         */

    //TREE LOAD TREE

    /*
Tree metadata = loadTree();
        Tree.Branch newBranch = null;
        boolean flag = false;
        Tree.Node node = metadata.head.branch.node;
        int len = node.fileNames.size();
        for (int i = 0; i < len; i++) {
            String filename = node.fileNames.get(i);
            if (filename.equals(fileName)) {
                String hashId = node.hashed.get(i);
                File src = Utils.join(GITDIR, "committed", hashId);
                File dest = new File(filename);
                dest.delete(); //error?
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = new FileInputStream(src);
                    os = new FileOutputStream(dest);//error?
                    // buffer size 1K
                    byte[] buf = new byte[1024];

                    int bytesRead;
                    while ((bytesRead = is.read(buf)) > 0) {
                        os.write(buf, 0, bytesRead);
                    }
                } catch (IOException e) {
                    System.out.println("Didn't work");
                }
                flag = true;
                File dele = Utils.join(GITDIR, "untracking", fileName);
                dele.delete();
                break;
            }
        }
        if (!flag) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        writeObjectToFile(metadata);*/
}
