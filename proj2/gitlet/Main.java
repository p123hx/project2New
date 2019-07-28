package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;


/* Driver class for Gitlet, the tiny stupid version-control system.
   @author Hongxi
*/
public class Main implements Serializable {
    /* Usage: java gitlet.Main ARGS, where ARGS contains
       <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        ArrayList<String> input = new ArrayList<>(Arrays.asList(args));
        String cmd = args[0];
        if (input.isEmpty()) {
            System.out.println("Please enter a command.");
            return;
        }
        switch (cmd) {
            case "init" :
                Main.init();
                break;
            case "add" :
                Main.add(args);
                break;
            case "commit" :
                Main.commit(args);
                break;
            case "rm" :
                Main.rm(args);
                break;
            case "log" :
                Main.log();
                break;
            case "global-log" :
                Main.globalLog(args);
                break;
            case "find" :
                Main.find(args);
                break;
            case "status" :
                Main.status();
                break;
            case "checkout" :
                Main.checkout(input, args);
                break;
            case "branch" :
                Main.branch(args);
                break;
            case "reset" :
                Main.reset(args);
                break;
            case "rm-branch" :
                Main.rmBranch(args);
                break;
            case "merge" :
                Main.merge(args);
                break;
            default :
                System.out.println("No command with that name exists.");
                break;
        }
    }

    public static void init() {
        if (GitletEngine.checkInit()) {
            System.out.println(("A gitlet version-control system "
                    + "already exists in the current directory."));
            return;
        }
        //if (args[1] != null) {
        //  System.out.println("Incorrect operands.");
        //  break;
        //}*/
        GitletEngine.init();
        return;
    }

    public static void add(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        GitletEngine.add(args[1]);
        return;
    }

    public static void commit(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        GitletEngine.commit(args[1]);
        return;
    }

    public static void rm(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        if (args.length == 1) {
            System.out.println("Please enter a filename");
        }
        GitletEngine.rm(args[1]);
        return;
    }

    public static void log() {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        GitletEngine.log();
        return;
    }

    public static void globalLog(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        if (args.length != 1) {
            System.out.println("Incorrect operands.");
            return;
        }
        GitletEngine.globalLog();
        return;
    }

    public static void find(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        if (args.length == 1) {
            System.out.println("Please enter a commit message.");
            return;
        }
        GitletEngine.find(args[1]);
        return;
    }

    public static void status() {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        GitletEngine.status();
        return;
    }

    public static void branch(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        if (args.length == 1) {
            System.out.println("Please enter a branch.");
            return;
        }
        GitletEngine.branch(args[1]);
        return;
    }

    public static void reset(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        if (args.length == 1) {
            System.out.println("Please enter a commit id");
            return;
        }
        GitletEngine.reset(args[1]);
        return;
    }

    public static void rmBranch(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        if (args.length == 1) {
            System.out.println("Please enter a commit id");
            return;
        }
        GitletEngine.rmBranch(args[1]); //[2]
        return;
    }

    public static void merge(String[] args) {
        if (!GitletEngine.checkInit()) {
            System.out.println(("Not in an initialized gitlet directory."));
            return;
        }
        if (args.length == 1) {
            System.out.println("Please enter a branch name.");
            return;
        }
        GitletEngine.merge(args[1]);
        return;
    }

    public static void checkout(ArrayList<String> input, String[] args) {
        try {
            input.remove(0);
            String one = input.remove(0);
            if (one.equals("--")) {
                String filename = input.remove(0);
                if (!input.isEmpty()) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                if (!GitletEngine.checkInit()) {
                    System.out.println("Not in an initialized Gitlet directory.");
                    return;
                }
                GitletEngine.checkoutFile(filename);
                return;
            } else {
                if (input.isEmpty()) {
                    if (!input.isEmpty()) {
                        System.out.println("Incorrect operands.");
                        return;
                    }
                    if (!GitletEngine.checkInit()) {
                        System.out.println("Not in an initialized Gitlet directory.");
                        return;
                    }
                    GitletEngine.checkoutBranch(args[0]);
                    return;
                } else {
                    String two = input.remove(0);
                    if (two.equals("--")) {
                        String filename = input.remove(0);
                        if (!input.isEmpty()) {
                            System.out.println("Incorrect operands.");
                            return;
                        }
                        if (!GitletEngine.checkInit()) {
                            System.out.println("Not in an initialized Gitlet directory.");
                            return;
                        }
                        GitletEngine.checkoutCommitFile(one, filename);
                        return;
                    }
                }
            }
        } catch (IndexOutOfBoundsException indexException) {
            System.out.println("Incorrect operands.");
        }
    }
}
