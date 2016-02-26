import java.util.LinkedList;
import java.util.HashMap;
import java.util.Vector;
import java.util.*;

public class IRNodesList {
    public static HashMap<String, LinkedList<IRNode>> irnodeslist = new HashMap<String, LinkedList<IRNode>>();
    public static HashMap<String, LinkedList<IRNode>> irnl = new HashMap<String, LinkedList<IRNode>>();

    public static void printirnodeslist () {
	System.out.println(";IR code");
	Vector<String> v = new Vector<String>(irnodeslist.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    if (s.equals("GLOBAL")) {
		continue;
	    }
	    LinkedList<IRNode> ll = irnodeslist.get(s);
	    for (int i = 0; i < ll.size(); i++) {
		System.out.println(";" + ll.get(i).ircode);
	    }
	    System.out.println(" ");
	}
    }

    public static void printirnl () {
	System.out.println(";IR code");
	Vector<String> v = new Vector<String>(irnl.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    if (s.equals("GLOBAL")) {
		continue;
	    }
	    LinkedList<IRNode> ll = irnl.get(s);
	    for (int i = 0; i < ll.size(); i++) {
		System.out.println(";" + ll.get(i).ircode);
	    }
	    System.out.println(" ");
	}
    }
}
