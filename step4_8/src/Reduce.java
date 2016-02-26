import java.util.LinkedList;
import java.util.HashMap;
import java.util.Stack;
import java.util.*;

public class Reduce {
    public static HashMap<String, LinkedList<IRNode>> nl = IRNodesList.irnl;
    public static HashMap<String, HashMap<String, Symbol>> st = SymbolTable.symboltable;
    public static HashMap<String, Symbol> globalst = st.get("GLOBAL");
    public static HashMap<String, Integer> live = new HashMap<String, Integer>();
    public static HashMap<String, Integer> changed = new HashMap<String, Integer>();
    public static HashMap<Integer, Integer> labels = new HashMap<Integer, Integer>();
    public static Vector<String> canignorefunction = new Vector<String>();
    public static int notinloop = 0;

    public static void reduce () {
	HashMap<String, Integer> init_live = new HashMap<String, Integer>();
	Vector<String> ilv = new Vector<String>(globalst.keySet());
	Iterator ilit = ilv.iterator();
	while (ilit.hasNext()) {
	    String ils = (String)ilit.next();
	    init_live.put(ils, 0);
	}

	live = init_live;

	Vector<String> v = new Vector<String>(nl.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    LinkedList<IRNode> ll = nl.get(s);
	    for (int i = 2; i < ll.size(); i++) {
		if (ll.get(i).action.equals("LABEL")) {
		    recordlabels(ll.get(i), i);
		}
	    }

	    for (int i = ll.size()-1; i > 1; i--) {
		IRNode node = ll.get(i);
		switch (node.action) {
		case "ADDI" :  if (ignore(node, i)) ll.remove(i); break;
		case "ADDF" :  if (ignore(node, i)) ll.remove(i); break;
		case "SUBI" :  if (ignore(node, i)) ll.remove(i); break;
		case "SUBF" :  if (ignore(node, i)) ll.remove(i); break;
		case "MULTI" :  if (ignore(node, i)) ll.remove(i); break;
		case "MULTF" :  if (ignore(node, i)) ll.remove(i); break;
		case "DIVI" :  if (ignore(node, i)) ll.remove(i); break;
		case "DIVF" :  if (ignore(node, i)) ll.remove(i); break;
		case "JSR" : if (!ll.get(i+1).action.equals("POP")) ll.remove(i); break;
		case "RET" : ret(ll.get(i-1), i); break;
		case "LABEL" : changed.clear(); label(node); break;
		case "POP" : ll = pop(ll, i); break;
		case "PUSH" : if (node.result != null) live.put(node.result, i); break;
		case "READI" : if (store(node, i)) ll.remove(i); break;
		case "READF" : if (store(node, i)) ll.remove(i); break;
		case "WRITEI" : live.put(node.result, i); break;
		case "WRITEF" : live.put(node.result, i); break;
		case "WRITES" : live.put(node.result, i); break;
		case "STOREI" : if (store(node, i)) ll.remove(i); break;
		case "STOREF" : if (store(node, i)) ll.remove(i); break;
		default : addlives(ll.get(i), i); checklabel(ll.get(i), i); break;
		}
	    }

	    labels.clear();
	    changed.clear();
	    live = init_live;
	}
    }

    public static void label (IRNode irnode) {
	String curresult = irnode.result;
	String curlabelnumber = curresult.substring(5, curresult.length());
	int curlabnum = labels.get(Integer.parseInt(curlabelnumber));
	if (curlabnum == notinloop) {
	    notinloop = 0;
	}
    }

    public static void ret (IRNode irnode, int i) {
	if (irnode.action.startsWith("STORE")) {
	    live.put(irnode.result, i-1);
	}
    }

    public static void recordlabels (IRNode irnode, int i) {
	String curresult = irnode.result;
	String curlabnum = curresult.substring(5, curresult.length());
	labels.put(Integer.parseInt(curlabnum), i);
    }

    public static void checklabel (IRNode irnode, int i) {
	String curresult = irnode.result;
	String curlabelnumber = curresult.substring(5, curresult.length());
	int curlabnum = labels.get(Integer.parseInt(curlabelnumber));
        if (curlabnum < i) {
	    notinloop = curlabnum;
	}
    }

    //ignore variables that will not be used anymore
    public static boolean ignore (IRNode irnode, int i) {
	String curresult = irnode.result;
	if (!live.containsKey(curresult) && notinloop == 0) {
	    return true;
	}
	addlives(irnode, i);
	return false;
    }

    public static void addlives (IRNode irnode, int i) {
	if (irnode.op1 == null) {
	    return;
	}
	live.put(irnode.op1, i);

	if (irnode.op2 == null) {
	    return;
	}
	live.put(irnode.op2, i);
    }

    public static boolean ignore2 (IRNode irnode, int i) {            //ignore without addlives
	String curresult = irnode.result;
	if (!live.containsKey(curresult) && notinloop == 0) {
	    return true;
	}
	return false;
    }

    public static boolean store (IRNode irnode, int i) {
	String curresult = irnode.result;
	if (ignore2(irnode, i)) {
	    return true;
	} else if (changed.containsKey(curresult) && (changed.get(curresult) < live.get(curresult))) {
	    return true;
	}
	addlives(irnode, i);
	changed.put(curresult, i);
	return false;
    }

    public static LinkedList<IRNode> pop (LinkedList<IRNode> ll, int i) {
	if (ll.get(i).result == null) {
	    return ll;
	} else {
	    int j;
	    for (j = i; ll.get(j).action.equals("POP"); j--) {
		//get jsr node
	    }
	    IRNode jsrnode = ll.get(j);
	    String jsrresult = jsrnode.result;
	    if (ignore(ll.get(i), i) && !doesglobalvarchanged(nl.get(jsrresult), jsrresult)) {
		while (ll.get(i).action.equals("POP")) {
		    ll.remove(i);
		    i--;
		}
		if (ll.get(i).action.equals("JSR")) {
		    ll.remove(i);
		    i--;
		}
		while (ll.get(i).action.equals("PUSH")) {
		    ll.remove(i);
		    i--;
		}
	    }
	    return ll;
	}
    }

    public static boolean doesglobalvarchanged (LinkedList<IRNode> ll, String functionname) {
	if (canignorefunction.contains(functionname)) {
	    return false;
	}

	for (int i = 0; i < ll.size(); i++) {
	    IRNode node = ll.get(i);
	    if (node.action.startsWith("WRITE")) {
		return true;
	    } else if (node.action.startsWith("STORE") || node.action.startsWith("READ")) {
		if (globalst.containsKey(node.result)) {
		    return true;
		}
	    } else if (node.action.equals("JSR")) {
		if (doesglobalvarchanged(nl.get(node.result), node.result)) {
		    return true;
		}
	    }
	}

	canignorefunction.add(functionname);
	return false;
    }
}
