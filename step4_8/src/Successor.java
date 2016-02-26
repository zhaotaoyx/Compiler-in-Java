import java.util.*;
import java.util.Vector;
import java.util.HashMap;
import java.util.LinkedList;

public class Successor {

    int successor1 = -1;
    int successor2 = -1;

    Successor (int s1) {
	this.successor1 = s1;
    }

    Successor (int s1, int s2) {
	this.successor1 = s1;
	this.successor2 = s2;
    }

    public static HashMap<String, LinkedList<IRNode>> nl = IRNodesList.irnl;
    public static HashMap<String, HashMap<Integer, Successor>> st = new HashMap<String, HashMap<Integer, Successor>>();  //successor table

    public static void setsuccessor () {
	Vector<String> v = new Vector<String>(nl.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String functionname = (String)it.next();
	    LinkedList<IRNode> ll = nl.get(functionname);
	    HashMap<Integer, Successor> newst = new HashMap<Integer, Successor>();
	    for (int i = 0; i < ll.size(); i++) {
		IRNode node = ll.get(i);
		String curaction = node.action;
		if (curaction.startsWith("RET")) {
		    newst.put(i, null);
		} else if (curaction.startsWith("JUMP")) {
		    String curresult = node.result;
		    for (int j = 0; j < ll.size(); j++) {
			if (ll.get(j).action.equals("LABEL") && ll.get(j).result.equals(curresult)) {
			    Successor s = new Successor(j);
			    newst.put(i, s);
			    break;
			}
		    }
		} else if (curaction.startsWith("EQ") || curaction.startsWith("NE")
			 || curaction.startsWith("GT") || curaction.startsWith("GE")
			 || curaction.startsWith("LT") || curaction.startsWith("LE")) {
		    String curresult = node.result;
		    for (int j = 0; j < ll.size(); j++) {
			if (ll.get(j).action.equals("LABEL") && ll.get(j).result.equals(curresult)) {
			    Successor s = new Successor(i+1, j);
			    newst.put(i, s);
			    break;
			}
		    }
		} else {
		    Successor s = new Successor(i+1);
		    newst.put(i, s);
		}
	    }
	    
	    st.put(functionname, newst);
	}
    }

    public static Successor getsuccessor (String functionname, int i) {
	return st.get(functionname).get(i);
    }
}
