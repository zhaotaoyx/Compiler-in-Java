import java.util.HashMap;
import java.util.Vector;
import java.util.*;

public class SymbolTable {
    public static HashMap<String, HashMap<String, Symbol>> symboltable = new HashMap<String, HashMap<String, Symbol>>();
    public static int mainparnum = 0; //record how many p does main have
    public static HashMap<String, Integer> lnum = new HashMap<String, Integer>(); //record how many local variables in a function
    public static HashMap<String, Integer> pnum = new HashMap<String, Integer>(); //record how many input parameters of a function
    public static HashMap<String, HashMap<String, String>> regtable = new HashMap<String, HashMap<String, String>>(); // register table
    public static HashMap<String, String> functype = new HashMap<String, String>();

    public static void printsymboltable () {
	Vector<String> v = new Vector<String>(symboltable.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    System.out.println(s);
	    printhashmap(symboltable.get(s));
	    System.out.println(" ");
	}
    }

    public static void printhashmap (HashMap<String, Symbol> hm) {
	Vector<String> v = new Vector<String>(hm.keySet());
	Iterator it = v.iterator();
	while (it.hasNext()) {
	    String s = (String)it.next();
	    System.out.print(s + " ");
	    Symbol symbol = hm.get(s);
	    System.out.println(symbol.type + " " + symbol.value);
	}
    }
}
