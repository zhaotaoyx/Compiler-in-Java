import java.util.LinkedList;
import java.util.HashMap;
import java.util.Vector;
import java.util.*;

public class RegisterAllocation {
    public static HashMap<String, LinkedList<IRNode>> nl = IRNodesList.irnl;
    public static HashMap<String, HashMap<String, Symbol>> st = SymbolTable.symboltable;
    public static HashMap<String, Symbol> globalst = st.get("GLOBAL");
    public static HashMap<String, HashMap<String, String>> allregtable = SymbolTable.regtable;
    public static HashMap<String, String> regtable = new HashMap<String, String>();
    public static Vector<String> willuse = new Vector<String>();
    public static Vector<String> cannotchange = new Vector<String>();


    public static void registerallocation (int regnum) {

	//set gen kill in out in each node
	Successor.setsuccessor();
	Predecessor.setpredecessor();
	Vector<String> v1 = new Vector<String>(nl.keySet());
	Iterator it1 = v1.iterator();
	while (it1.hasNext()) {
	    String functionname = (String)it1.next();
	    LinkedList<IRNode> ll = nl.get(functionname);
	    Stack<Integer> worklist = new Stack<Integer>();
	    for (int i = 0; i < ll.size(); i++) {
		worklist.push(i);
		IRNode node = ll.get(i);
		String curaction = node.action;
		if (curaction.equals("JSR")) {
		    node.gen.addAll(globalst.keySet());
		} else if (curaction.startsWith("NE") || curaction.startsWith("EQ")
			 || curaction.startsWith("GE") || curaction.startsWith("GT")
			 || curaction.startsWith("LE") || curaction.startsWith("LT")
			 || curaction.startsWith("JUMP") || curaction.startsWith("LINK")
			 || curaction.startsWith("LABEL")) {
		    if (node.op1 != null) {
			node.gen.add(node.op1);
		    }
		    if (node.op2 != null) {
			node.gen.add(node.op2);
		    }
		    continue;
		} else if (curaction.startsWith("WRITE") || curaction.startsWith("PUSH")) {
		    if (node.result != null) {
			node.gen.add(node.result);
		    }
		} else {
		    if (node.op1 != null) {
			node.gen.add(node.op1);
		    }
		    if (node.op2 != null) {
			node.gen.add(node.op2);
		    }
		    if (node.result != null) {
			node.kill.add(node.result);
		    }
		}
	    }
	    while (!worklist.empty()) {
		int i = worklist.pop();
		IRNode node = ll.get(i);
		// set out
		if (!node.action.equals("RET")) {
		    Successor successor = Successor.getsuccessor(functionname, i);
		    int s1 = successor.successor1;
		    IRNode sr1 = ll.get(s1);
		    node.out.addAll(sr1.in);
		    if (successor.successor2 != -1) {
			int s2 = successor.successor2;
			IRNode sr2 = ll.get(s2);
			Vector<String> curout = sr2.in;
			Iterator curit = curout.iterator();
			while (curit.hasNext()) {
			    String curs = (String)curit.next();
			    if (!node.out.contains(curs)) {
				node.out.add(curs);
			    }
			}
		    }
		}
		// set in
		Vector<String> oldin = node.in;
		node.in.addAll(node.out);
		node.in.removeAll(node.kill);
		Iterator genit = node.gen.iterator();
		while (genit.hasNext()) {
		    String gens = (String)genit.next();
		    if (!node.in.contains(gens)) {
			node.in.add(gens);
		    }
		}
		if (!node.in.equals(oldin)) {
		    Vector<Integer> predecessor = Predecessor.getpredecessor(functionname, i);
		    Iterator preit = predecessor.iterator();
		    while (preit.hasNext()) {
			int prei = (Integer)preit.next();
			if (!worklist.contains(prei)) {
			    worklist.add(prei);
			}
		    }
		}
	    }


	    /*
	    //check
	    for (int x = 0; x < ll.size(); x++) {
		IRNode node = ll.get(x);
		System.out.println(node.in);
		System.out.println(Predecessor.getpredecessor(functionname, x));
		System.out.println(node.ircode);
		//Successor xxx = Successor.getsuccessor(functionname, x);
		//System.out.println(Integer.toString(xxx.successor1));
		System.out.println(node.out);
	    }
	
	    System.out.println("");
	    */

	}


	//register allocation
	Vector<String> v2 = new Vector<String>(nl.keySet());
	Iterator it2 = v2.iterator();
	while (it2.hasNext()) {
	    String functionname = (String)it2.next();
	    regtable = allregtable.get(functionname);
	    LinkedList<IRNode> ll = nl.get(functionname);
	    for (int i = 0; i < ll.size(); i++) {
		IRNode node = ll.get(i);
		if (node.result != null && node.result.startsWith("$T")) {
		    int curregnum = Integer.parseInt(node.result.substring(2, node.result.length()));
		    if (curregnum > regnum) {
			String curreg = "";

			//find a reg which is not alive
			Vector<String> curout = node.out;
			for (int j = 1; j <= regnum; j++) {
			    if (!curout.contains("$T" + Integer.toString(j))) {
				curreg = "$T" + Integer.toString(j);
				break;
			    }
			}
			
			//find a reg which will not gonna be used
			if (curreg.equals("")) {
			    for (int j = 1; j <= regnum; j++) {
				if (!willuse.contains("$T" + Integer.toString(j))) {
				    curreg = "$T" + Integer.toString(j);
				    break;
				}
			    }
			}
			

			/*
			//find a reg which will be used latest
			if (curreg.equals("")) {
			    Iterator tempit = curout.iterator();
			    while (tempit.hasNext()) {
				String temps = (String)tempit.next();
				if (temps.startsWith("$T")) {
				    String tempreg = temps.substring(2, temps.length());
				    if (Integer.parseInt(tempreg) <= regnum) {
					curreg = temps;
					break;
				    }
				}
			    }
			    //store back to memory
			    String memvar = regtable.get(curreg);
			    if (!memvar.contains("+") && !memvar.contains("-")
				&& !memvar.contains("*") && !memvar.contains("/")) {
				HashMap<String, Symbol> curst = st.get(functionname);
				String curtype = curst.get(memvar).type;
				String curaction = curtype.equals("INT") ? "STOREI" : "STOREF";
				IRNode newnode = new IRNode(curaction, curreg, memvar);
				ll.add(i++, newnode);
			    }
			}
			*/


			ll = replace(node.result, curreg, ll, i);
		    }

		    //mark cannotchange
		    node = ll.get(i);
		    if (node.action.startsWith("ADD") || node.action.startsWith("SUB")
			|| node.action.startsWith("MULT") || node.action.startsWith("DIV")
			|| node.action.equals("POP")) {
			if (node.op1 != null) {
			    willuse.remove(node.op1);
			}
			if (node.op2 != null) {
			    willuse.remove(node.op2);
			}
			if (node.result != null) {
			    willuse.add(node.result);
			}
		    }

		    //mark willuse
		    node = ll.get(i);
		    if (node.action.startsWith("STORE")) {
			willuse.add(node.result);
			//System.out.println(node.out);
		    }
		}
		else {
		    if (node.action.startsWith("STORE")) {
			willuse.remove(node.op1);
		    } else if (node.action.startsWith("PUSH") && node.result != null) {
			willuse.remove(node.result);
		    }
		}
		
		//write back before jump

		/*
		if (node.action.startsWith("NE") || node.action.startsWith("EQ")
		    || node.action.startsWith("GT") || node.action.startsWith("GE")
		    || node.action.startsWith("LT") || node.action.startsWith("LE")
		    || node.action.startsWith("JUMP")) {

		    for (int k = 1; k <= regnum; k++) {
			String curreg = "$T" + Integer.toString(k);
			String memvar = regtable.get(curreg);

			if (!memvar.contains("+") && !memvar.contains("-")
			    && !memvar.contains("*") && !memvar.contains("/")
			    && dirty.contains(curreg) && node.out.contains(curreg)) {
			    if ((memvar.charAt(0) < '0' || memvar.charAt(0) > '9') && !memvar.startsWith("$R")) {
				String curtype = st.get(functionname).get(memvar).type;
				String curaction = curtype.equals("INT") ? "STOREI" : "STOREF";
				IRNode newnode1 = new IRNode(curaction, curreg, memvar);
				ll.add(i++, newnode1);
			    }
			}
		    }
		}

		//write back before call function or return

		else if (node.action.startsWith("PUSH") || node.action.startsWith("RET")) {

		    for (int k = 1; k <= regnum; k++) {
			String curreg = "$T" + Integer.toString(k);
			String memvar = regtable.get(curreg);
			if (memvar == null) {
			    continue;
			}

			if (!memvar.contains("+") && !memvar.contains("-")
			    && !memvar.contains("*") && !memvar.contains("/")
			    && dirty.contains(curreg) && globalst.containsKey(memvar)) {
			    String curtype = st.get(functionname).get(memvar).type;
			    String curaction = curtype.equals("INT") ? "STOREI" : "STOREF";
			    IRNode newnode1 = new IRNode(curaction, curreg, memvar);
			    ll.add(i++, newnode1);
			}
		    }
		}
		*/



		/*
		//check dirty
		if (node.equals(new IRNode("ADD", "$T2", "$T1", "$T1"))) {
		    Iterator itit = dirty.iterator();
		    while (itit.hasNext()) {
			System.out.println((String)itit.next());
		    }
		}
		*/


		/*
		//check liveness
		if (node.equals(new IRNode("ADD", "$T2", "$T1", "$T1"))) {
		    Iterator itit = node.out.iterator();
		    while (itit.hasNext()) {
			System.out.println((String)itit.next());
		    }
		}
		*/


	    }
	}
    }

    public static LinkedList<IRNode> replace (String oldreg, String newreg, LinkedList<IRNode> ll, int i) {
	IRNode inode = ll.get(i);
	if (inode.op2 != null) {
	    IRNode irnode = new IRNode(inode.action, inode.op1, inode.op2, newreg);
	    irnode.out = inode.out;
	    ll.add(i, irnode);
	    ll.remove(i+1);
	} else if (inode.op1 != null) {
	    IRNode irnode = new IRNode(inode.action, inode.op1, newreg);
	    irnode.out = inode.out;
	    ll.add(i, irnode);
	    ll.remove(i+1);
	} else {
	    IRNode irnode = new IRNode(inode.action, newreg);
	    irnode.out = inode.out;
	    ll.add(i, irnode);
	    ll.remove(i+1);
	}

	//change out for ll.get(i)
	IRNode node = ll.get(i);
	if (node.out.contains(oldreg) && !node.out.contains(newreg)) {
	    node.out.add(newreg);
	    node.out.remove(oldreg);
	} else if (node.out.contains(newreg) && !node.out.contains(oldreg)) {
	    node.out.remove(newreg);
	} else if (node.out.contains(oldreg)) {
	    node.out.remove(oldreg);
	}


	for (int j = i+1; j < ll.size(); j++) {
	    node = ll.get(j);
	    if (node.op1 != null && node.op2 != null) {
		if (node.op1.equals(oldreg)) {
		    String curresult = node.result;
		    if (node.result.equals(node.op1)) {
			curresult = newreg;
		    }
		    IRNode newnode = new IRNode(node.action, newreg, node.op2, curresult);
		    newnode.out = node.out;
		    ll.add(j, newnode);
		    ll.remove(j+1);
		}
		node = ll.get(j);
		if (node.op2.equals(oldreg)) {
		    IRNode newnode2 = new IRNode(node.action, node.op1, newreg, node.result);
		    newnode2.out = node.out;
		    ll.add(j, newnode2);
		    ll.remove(j+1);
		}
	    } else if (node.op1 != null) {
		if (node.op1.equals(oldreg)) {
		    IRNode newnode3 = new IRNode(node.action, newreg, node.result);
		    newnode3.out = node.out;
		    ll.add(j, newnode3);
		    ll.remove(j+1);
		}
	    } else if ((node.action.equals("PUSH") || node.action.startsWith("WRITE")) && node.result != null) {
		if (node.result.equals(oldreg)) {
		    IRNode newnode4 = new IRNode(node.action, newreg);
		    newnode4.out = node.out;
		    ll.add(j, newnode4);
		    ll.remove(j+1);
		}
	    }

	    //change out
	    node = ll.get(j);
	    if (node.out.contains(oldreg) && !node.out.contains(newreg)) {
		node.out.add(newreg);
		node.out.remove(oldreg);
	    } else if (node.out.contains(newreg) && !node.out.contains(oldreg)) {
		node.out.remove(newreg);
	    } else if (node.out.contains(oldreg)) {
		node.out.remove(oldreg);
	    }
	}

	//change regtable
	regtable.put(newreg, regtable.get(oldreg));
	regtable.remove(oldreg);

	return ll;
    }

    /*
    public static LinkedList<IRNode> addliveness (LinkedList<IRNode> ll, int i) {
	String needtoadd = ll.get(i).op1;
	String oldreg = ll.get(i).result;
	for (int j = i; j < ll.size(); j++) {
	    if (ll.get(j).out.contains(oldreg)) {
		ll.get(j).out.add(needtoadd);
	    }
	}

	return ll;
    }
    */
}
