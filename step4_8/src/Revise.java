import java.util.LinkedList;
import java.util.HashMap;
import java.util.Vector;
import java.util.*;

public class Revise {
    public static HashMap<String, LinkedList<IRNode>> irnodeslist = IRNodesList.irnodeslist;
    public static HashMap<String, LinkedList<IRNode>> irnl = IRNodesList.irnl;
    public static HashMap<String, HashMap<String, Symbol>> st = SymbolTable.symboltable;
    public static HashMap<String, String> reg = new HashMap<String, String>();
    public static HashMap<String, String> newregtable = new HashMap<String, String>();
    public static String scope = "GLOBAL";
    public static int regnum = 0;
    public static int locnum = 0;
    public static int parnum = 0;

    public static void revise () {
	Vector<String> v = new Vector<String>(irnodeslist.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    LinkedList<IRNode> ll = irnodeslist.get(s);
	    newregtable.clear();
	    irnladd(s, ll);
	    SymbolTable.regtable.put(s, new HashMap<String, String>(newregtable));
	}	
    }

    public static void irnladd (String functionname, LinkedList<IRNode> ll) {
	if (ll.size() == 0) {
	    return;
	}
	LinkedList<IRNode> ll1 = new LinkedList<IRNode>();
	scope = functionname;
	for (int i = 0; i < ll.size(); i++) {
	    IRNode irnode = ll.get(i);
	    switch (irnode.action) {
	    case "LABEL" : ll1.add(irnode); reg.clear(); break;
	    case "LINK" : ll1.add(irnode); break;
	    case "JUMP" : ll1.add(irnode); break;
	    case "RET" : ll1.add(irnode); break;
	    case "JSR" : ll1.add(irnode); break;
	    case "POP" : ll1.add(pop(irnode)); break;
	    case "PUSH" : push(irnode, ll1); break;
	    case "PUSHI" : push(irnode, ll1); break;
	    case "PUSHF" : push(irnode, ll1); break;
	    case "READI" : ll1.add(read(irnode)); break;
	    case "READF" : ll1.add(read(irnode)); break;
	    case "WRITEI" : ll1.add(read(irnode)); break;
	    case "WRITEF" : ll1.add(read(irnode)); break;
	    case "WRITES" : ll1.add(irnode); break;
	    case "INCR_STMT" : incr(ll, i); break;
	    default : tac(irnode, ll1); break;
	    }
	}
	irnl.put(functionname, ll1);
	regnum = 0;
	locnum = 0;
	parnum = 0;
	reg.clear();
    }

    public static void incr (LinkedList<IRNode> ll, int i) {
	if (ll.get(i).result.startsWith("begin")) {
	    LinkedList<IRNode> temp = new LinkedList<IRNode>();
	    int j;
	    String curresult = ll.get(i).result;
	    String fornum = curresult.substring(5, curresult.length());
	    for (j = i+1; !ll.get(j).result.equals("end" + fornum); j++) {
		temp.add(ll.get(j));
		ll.remove(j);
		j--;
	    }
	    j++;
	    while (ll.get(j).result == null || !ll.get(j).result.equals("here" + fornum)) {
		j++;
		//let ll.get(j) == INCR_STMT here#
	    }
	    ll.remove(j);
	    ll.addAll(j, temp);
	    return;
	}
    }

    //read and write
    public static IRNode read (IRNode irnode) {
	String curvalue;
	String curresult = irnode.result;
	if (st.get(scope).containsKey(curresult)) {
	    curvalue = st.get(scope).get(irnode.result).value;
	    curresult = curvalue.equals("") ? irnode.result : curvalue;
	}
	IRNode node = new IRNode(irnode.action, curresult);
	if (irnode.action.startsWith("READ")) {
	    valuechanged(irnode.result);
	}
	return node;
    }

    //push
    public static void push (IRNode irnode, LinkedList<IRNode> ll1) {
	if (irnode.result == null) {
	    ll1.add(irnode);
	    return;
	}
	String curresult = irnode.result;
	while (curresult.charAt(0) == '(' && curresult.charAt(curresult.length()-1) == ')' && !reg.containsKey(curresult)) {
	    curresult.substring(1, curresult.length()-1);
	}
	if (curresult.contains("+") || curresult.contains("-")
	    || curresult.contains("*") || curresult.contains("/")
	    || curresult.contains("(")) {
	    curresult = reg.get(curresult);
	} else {
	    if (st.get(scope).containsKey(curresult)) {
		String curvalue = st.get(scope).get(curresult).value;
		curresult = curvalue.equals("") ? curresult : curvalue;
	    }
	    String curaction = irnode.action;
	    curaction = curaction.replace("PUSH", "STORE");
	    String newreg = "$T" + Integer.toString(++regnum);
	    ll1.add(new IRNode(curaction, curresult, newreg));
	    curresult = newreg;
	}
	ll1.add(new IRNode("PUSH", curresult));
    }

    //pop
    public static IRNode pop (IRNode irnode) {
	if (irnode.result == null) {
	    return irnode;
	} else {
	    String newreg = "$T" + Integer.toString(++regnum);
	    reg.put(irnode.result, newreg);
	    return new IRNode(irnode.action, newreg);
	}
    }

    //change it to three address code
    public static void tac (IRNode irnode, LinkedList<IRNode> ll1) {
	String curaction = irnode.action;
	String curop1 = irnode.op1;
	String curresult = irnode.result;
	String curvalue;

	while (curop1.charAt(0) == '(' && curop1.charAt(curop1.length()-1) == ')' && !reg.containsKey(curop1)) {
	    curop1 = curop1.substring(1, curop1.length()-1);
	}

	if (curaction.startsWith("STORE")) {
	    if (st.get(scope).containsKey(curresult)) {
		curvalue = st.get(scope).get(curresult).value;
		curresult = curvalue.equals("") ? curresult : curvalue;
	    }
	    if (reg.containsKey(curop1)) {
		IRNode node = new IRNode(curaction, reg.get(curop1), curresult);
		ll1.add(node);
		reg.put(irnode.result, reg.get(curop1));
		newregtable.put(reg.get(curop1), curresult);
	    } else {
		String newreg = "$T" + Integer.toString(++regnum);
		newregtable.put(newreg, curresult);
		reg.put(curop1, newreg);
		if (st.get(scope).containsKey(curop1)) {
		    curvalue = st.get(scope).get(curop1).value;
		    curop1 = curvalue.equals("") ? curop1 : curvalue;
		}
		IRNode node1 = new IRNode(curaction, curop1, newreg);
		ll1.add(node1);
		IRNode node2 = new IRNode(curaction, newreg, curresult);
		ll1.add(node2);
		reg.put(irnode.result, newreg);
	    }
	    //valuechanged(irnode.result);
	    //notusecsl();
	    return;
	}

	else {
	    String curop2 = irnode.op2;
	    String newreg;
	    String tempaction = "STORE" + String.valueOf(curaction.charAt(curaction.length()-1));
	    String op1rec = curop1;

	    //op1
	    if (reg.containsKey(curop1)
		&& (curop1.contains("+") || curop1.contains("-")
		    || curop1.contains("*") || curop1.contains("/")
		    || curop1.contains("("))) {
		String t = curop1;
		curop1 = reg.get(curop1);
		reg.remove(t);
	    }
	    else {
		newreg = "$T" + Integer.toString(++regnum);
		if (st.get(scope).containsKey(curop1)) {
		    curvalue = st.get(scope).get(curop1).value;
		    curop1 = curvalue.equals("") ? curop1 : curvalue;
		}
		newregtable.put(newreg, curop1);
		IRNode node1 = new IRNode(tempaction, curop1, newreg);
		ll1.add(node1);
		op1rec = curop1;
		curop1 = newreg;
	    }

	    //op2
	    while (curop2.charAt(0) == '(' && curop2.charAt(curop2.length()-1) == ')' && !reg.containsKey(curop2)) {
		curop2 = curop2.substring(1, curop2.length()-1);
	    }
	    if (reg.containsKey(curop2)
		&& (curop2.contains("+") || curop2.contains("-")
		    || curop2.contains("*") || curop2.contains("/")
		    || curop2.contains("("))) {
		String t = curop2;
		curop2 = reg.get(curop2);
		if (t.contains("+") || t.contains("-") || t.contains("*") || t.contains("/") || t.contains("(")) {
		    reg.remove(t);
		}
	    } else if (curop2.equals(op1rec)) {
		curop2 = curop1;
	    } else {
		newreg = "$T" + Integer.toString(++regnum);
		reg.put(curop2, newreg);
		if (st.get(scope).containsKey(curop2)) {
		    curvalue = st.get(scope).get(curop2).value;
		    curop2 = curvalue.equals("") ? curop2 : curvalue;
		}
		newregtable.put(newreg, curop2);
		IRNode node2 = new IRNode(tempaction, curop2, newreg);
		ll1.add(node2);
		curop2 = newreg;
	    }


	    //result
	    if (curaction.startsWith("ADD") || curaction.startsWith("SUB") || curaction.startsWith("MULT") || curaction.startsWith("DIV")) {
		newregtable.put(curop1, curresult);
		reg.put(curresult, curop1);
		curresult = curop1;
	    }

	    IRNode node = new IRNode(curaction, curop1, curop2, curresult);
	    ll1.add(node);
	}
    }

    public static void valuechanged (String changed) {
	Vector<String> v = new Vector<String>(reg.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    if (s.equals(changed)) {
		continue;
	    } else if (s.contains(changed)) {
		String[] parts = s.split(changed);
		for (int i = 0; i < parts.length-1; i++) {
		    if (parts[i].length() != 0 && parts[i+1].length() != 0) {
			char p1 = parts[i].charAt(parts[i].length()-1);
			char p2 = parts[i+1].charAt(0);
			if ((p1>'9'||p1<'0') && (p1>'z'||p1<'a') && (p1>'Z'||p1<'A') && (p2>'9'||p2<'0') && (p2>'z'||p2<'a') && (p2>'Z'||p2<'A')) {
			    reg.remove(s);
			    break;
			}
		    } else if (parts[i].length() != 0) {
			char p1 = parts[i].charAt(parts[i].length()-1);
			if ((p1>'9'||p1<'0') && (p1>'z'||p1<'a') && (p1>'Z'||p1<'A')) {
			    reg.remove(s);
			    break;
			}
		    } else if (parts[i+1].length() != 0) {
			char p2 = parts[i+1].charAt(0);
			if ((p2>'9'||p2<'0') && (p2>'z'||p2<'a') && (p2>'Z'||p2<'A')) {
			    reg.remove(s);
			    break;
			}
		    }
		}
	    }
	}
    }

    public static void notusecsl () {
	Vector<String> cslv = new Vector<String>(reg.keySet());
	Iterator cslit = cslv.iterator();
	while (cslit.hasNext()) {
	    String curvar = (String)cslit.next();
	    if (curvar.contains("+") || curvar.contains("-") || curvar.contains("*") || curvar.contains("/") || curvar.contains("(")) {
		reg.remove(curvar);
	    }
	}
    }
}

