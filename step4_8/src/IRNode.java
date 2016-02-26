import java.util.Vector;
import java.util.*;

public class IRNode {
    String action;
    String op1;
    String op2;
    String result;
    String ircode;
    Vector<String> gen = new Vector<String>();
    Vector<String> kill = new Vector<String>();
    Vector<String> in = new Vector<String>();
    Vector<String> out = new Vector<String>();

    IRNode (String action, String op1, String op2, String result) {
	this.action = action;
	this.op1 = op1;
	this.op2 = op2;
	this.result = result;
	this.ircode = action + " " + op1 +" " + op2 + " " + result;
    }
    IRNode (String action, String op1, String result) {
	this.action = action;
	this.op1 = op1;
	this.result = result;
	this.ircode = action + " " + op1 + " " + result;
    }
    IRNode (String action, String result) {
	this.action = action;
	this.result = result;
	this.ircode = action + " " + result;
    }
    IRNode (String action) {
	this.action = action;
	this.ircode = action;
	if (action.equals("RET")) {
	    HashMap<String, Symbol> globalst = SymbolTable.symboltable.get("GLOBAL");
	    this.out.addAll(globalst.keySet());
	}
    }

    public boolean equals (IRNode node) {
	String curaction = this.action;
	curaction = curaction.substring(0, curaction.length()-1);
	if (!curaction.equals(node.action)) {
	    return false;
	}
	if (this.op1 == null && node.op1 != null) {
	    return false;
	}
	else if (this.op1 != null && node.op1 == null) {
	    return false;
	}
	else if (this.op1 != null && node.op1 != null && !this.op1.equals(node.op1)) {
	    return false;
	}
	if (this.op2 == null && node.op2 != null) {
	    return false;
	}
	else if (this.op2 != null && node.op2 == null) {
	    return false;
	}
	else if (this.op2 != null && node.op2 != null && !this.op2.equals(node.op2)) {
	    return false;
	}
	if (this.result == null && node.result != null) {
	    return false;
	}
	else if (this.result != null && node.result == null) {
	    return false;
	}
	else if (this.result != null && node.result != null && !this.result.equals(node.result)) {
	    return false;
	}
	return true;
    }
}
