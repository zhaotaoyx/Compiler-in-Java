import java.util.LinkedList;
import java.util.Vector;
import java.util.HashMap;
import java.util.*;

public class Translator {
    public static Map<String,String> regMap = new HashMap<String,String>();
    public static HashMap<String, Integer> lnum = SymbolTable.lnum;
    public static HashMap<String, Integer> pnum = SymbolTable.pnum;
    public static String scope = "GLOBAL";
    public static int regnum = 0;
    public static Vector<String> regused = new Vector<String>();


    public static void translator (int totalregnum) {
	regnum = totalregnum;

	print(";tiny code");
	HashMap<String, HashMap<String, Symbol>> st = SymbolTable.symboltable;
	variable(st.get("GLOBAL"));
	
	print("push");
	for (int i = 1; i <= SymbolTable.mainparnum; i++) {
	    print("push $-" + i);
	}
	for (int i = 0; i < 4; i++) {
	    print("push r" + i);
	}
	print("jsr main");
	print("sys halt");
	
	HashMap<String, LinkedList<IRNode>> nl = IRNodesList.irnl;
	Vector<String> v = new Vector<String>(nl.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    translate(nl.get(s));
	    regMap.clear();
	    regused.clear();
	}
	print("end");
    }

    public static void print (String code) {
	System.out.println(code);
    }

    public static String getReg () {
	for (int i = 0; i < regnum; i++) {
	    String newReg = "r" + Integer.toString(i);
	    if (!regused.contains(newReg)) {
		return newReg;
	    }
	}
	return "wrong";
    }

    public static void variable (HashMap<String, Symbol> st) {
	Vector v = new Vector(st.keySet());
	Iterator it = v.iterator();
	while(it.hasNext()){
	    String s = (String)it.next();
	    if (s.startsWith("$T")) {
	    	continue;
	    }
	    Symbol symbol = st.get(s);
	    if (symbol.type.equals("INT") || symbol.type.equals("FLOAT")) {
		print("var " + s);
	    } else {
		print("str " + s + " " + symbol.value);
	    }
	}
    }

    public static void translate (LinkedList<IRNode> irList) {
	for (IRNode node: irList) {
	    switch (node.action) {
	    case "ADDI" : op("addi", node); break;
	    case "ADDF" : op("addr", node); break;
	    case "SUBI" : op("subi", node); break;
	    case "SUBF" : op("subr", node); break;
	    case "MULTI" : op("muli", node); break;
	    case "MULTF" : op("mulr", node); break;
	    case "DIVI" : op("divi", node); break;
	    case "DIVF" : op("divr", node); break;
	    case "STOREI" : store("move", node); break;
	    case "STOREF" : store("move", node); break;
	    case "READI" : rwjl("sys readi", node); break;
	    case "READF" : rwjl("sys readr", node); break;
	    case "WRITEI" : rwjl("sys writei", node); break;
	    case "WRITEF" : rwjl("sys writer", node); break;
	    case "WRITES" : rwjl("sys writes", node); break;
	    case "JUMP" : rwjl("jmp", node); break;
	    case "LABEL" : rwjl("label", node); break;
	    case "GTI" : compare("jgt", node, "i"); break;
	    case "GTF" : compare("jgt", node, "r"); break;
	    case "GEI" : compare("jge", node, "i"); break;
	    case "GEF" : compare("jge", node, "r"); break;
	    case "LTI" : compare("jlt", node, "i"); break;
	    case "LTF" : compare("jlt", node, "r"); break;
	    case "LEI" : compare("jle", node, "i"); break;
	    case "LEF" : compare("jle", node, "r"); break;
	    case "NEI" : compare("jne", node, "i"); break;
	    case "NEF" : compare("jne", node, "r"); break;
	    case "EQI" : compare("jeq", node, "i"); break;
	    case "EQF" : compare("jeq", node, "r"); break;
	    case "RET" : ret(node); break;
	    case "JSR" : jsr(node); break;
	    case "LINK" : link(node); break;
	    case "PUSH" : pupo("push", node); break;
	    case "PUSHI" : pupo("push", node); break;
	    case "PUSHF" : pupo("push", node); break;
	    case "POP" : pupo("pop", node); break;
	    }


	    /*
	    System.out.println(node.ircode);
	    System.out.println(regMap);
	    System.out.println(regused);
	    */

	}
    }
    
    public static void op(String cmd, IRNode node) {
	String curop1 = node.op1;
	String curop2 = node.op2;
	String curresult = node.result;

	curop1 = regMap.get(curop1);
	curop2 = regMap.get(curop2);
	regused.remove(regMap.get(node.result));
	regMap.remove(node.op1);
	regMap.put(curresult, curop1);
	if (!regused.contains(curop1)) {
	    regused.add(curop1);
	}

	print(cmd + " " + curop2 + " " + curop1);
    }

    public static void store(String cmd, IRNode node) {
	String curop1 = node.op1;
	String curresult = node.result;
	curop1 = check(curop1);
	curresult = check(curresult);
	print(cmd + " " + curop1 +" " + curresult);
    }

    public static void rwjl(String cmd, IRNode node) {         //read, write, jump, label
	String curresult = node.result;
	curresult = check(curresult);
	print(cmd + " " + curresult);
	if (node.action.equals("LABEL") && !curresult.startsWith("label")) {
	    scope = curresult;
	}
    }

    public static void compare(String cmd, IRNode node, String type) {
	String curop1 = node.op1;
	String curop2 = node.op2;
	curop1 = check(curop1);
	curop2 = check(curop2);
	print("cmp" + type + " " + curop1 + " " + curop2);
	print(cmd + " " + node.result);
    }

    public static void ret (IRNode node) {
	print("unlnk");
	print("ret");
    }

    public static void pupo (String cmd, IRNode node) {
	if (node.result == null) {
	    print(cmd);
	} else {
	    String curresult = node.result;
	    curresult = check(curresult);
	    print(cmd + " " + curresult);
	}
    }
    /*
    public static String checkop1 (String curop1) {
	if (curop1.startsWith("$T")) {
	    if (regMap.containsKey(curop1)) {
		curop1 = regMap.get(curop1);
	    }
	    else {
		String newReg = getReg();
		regMap.put(curop1, newReg);
		regused.add(newReg);
		curop1 = newReg;
	    }
	}
	else if (curop1.startsWith("$L")) {
	    curop1 = curop1.replace('L', '-');
	}
	else if (curop1.startsWith("$P")) {
	    String curparnum = curop1.substring(2, curop1.length());
	    int cpnum = Integer.parseInt(curparnum) +5;
	    curop1 = "$" + Integer.toString(cpnum);
	}
	return curop1;
    }

    public static String checkop2 (String curop2) {
	if (curop2.startsWith("$T")) {
	    if (regMap.containsKey(curop2)) {
		curop2 = regMap.get(curop2);
	    }
	    else {
		String newReg = getReg();
		regMap.put(curop2, newReg);
		regused.add(newReg);
		curop2 = newReg;
	    }
	}
	else if (curop2.startsWith("$L")) {
	    curop2 = curop2.replace('L', '-');
	}
	else if (curop2.startsWith("$P")) {
	    String curparnum = curop2.substring(2, curop2.length());
	    int cpnum = Integer.parseInt(curparnum) +5;
	    curop2 = "$" + Integer.toString(cpnum);
	}
	return curop2;
    }
    */
    public static String check (String curresult) {
	if (curresult.startsWith("$T")) {
	    if (regMap.containsKey(curresult)) {
		curresult = regMap.get(curresult);
	    } else {
		String newReg = getReg();
		regMap.put(curresult, newReg);
		regused.add(newReg);
		curresult = newReg;
	    }
	} else if (curresult.startsWith("$R")) {
	    String currnum = curresult.substring(2, curresult.length());
	    int crnum = Integer.parseInt(currnum) +5;
	    curresult = "$" + Integer.toString(crnum);
	} else if (curresult.startsWith("$L")) {
	    curresult = curresult.replace('L', '-');
	} else if (curresult.startsWith("$P")) {
	    int curtotalpnum = pnum.get(scope);
	    String curparnum = curresult.substring(2, curresult.length());
	    int cpnum = 6+curtotalpnum - Integer.parseInt(curparnum);
	    curresult = "$" + Integer.toString(cpnum);
	}
	return curresult;
    }

    public static void jsr (IRNode node) {
	for (int i = 0; i < 4; i++) {
	    print("push r" + i);
	}
	print("jsr " + node.result);
	for (int i = 3; i >= 0; i--) {
	    print("pop r" + i);
	}
    }

    public static void link (IRNode node) {
	int i = lnum.get(scope);
	print("link " + Integer.toString(i));
    }
}
