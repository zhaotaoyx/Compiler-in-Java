import java.util.*;
import java.util.Vector;
import java.util.HashMap;
import java.util.LinkedList;

public class Predecessor {

    public static HashMap<String, LinkedList<IRNode>> nl = IRNodesList.irnl;
    public static HashMap<String, HashMap<Integer, Vector<Integer>>> pt = new HashMap<String, HashMap<Integer, Vector<Integer>>>();  //predecessor table

    public static void setpredecessor () {
	Vector<String> v = new Vector<String>(nl.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String functionname = (String)it.next();
	    LinkedList<IRNode> ll = nl.get(functionname);
	    HashMap<Integer, Vector<Integer>> newpt = new HashMap<Integer, Vector<Integer>>();
	    for (int i = 0; i < ll.size(); i++) {
		IRNode node = ll.get(i);
		String curaction = node.action;
		if (i == 0) {
		    newpt.put(i, null);
		} else if (curaction.startsWith("LABEL")) {
		    String curresult = node.result;
		    Vector<Integer> p = new Vector<Integer>();
		    if (!ll.get(i-1).action.equals("JUMP")) {
			p.add(i-1);
		    }
		    for (int j = 0; j < ll.size(); j++) {
			if (!ll.get(j).action.equals("LABEL") && ll.get(j).result != null && ll.get(j).result.equals(curresult)) {
			    p.add(j);
			}
		    }
		    newpt.put(i, p);
		} else {
		    IRNode lastnode = ll.get(i-1);
		    Vector<Integer> p = new Vector<Integer>();
		    p.add(i-1);
		    newpt.put(i, p);
		}
	    }
	    
	    pt.put(functionname, newpt);
	}
    }

    public static Vector<Integer> getpredecessor (String functionname, int i) {
	return pt.get(functionname).get(i);
    }
}
