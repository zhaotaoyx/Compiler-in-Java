import java.io.FileInputStream;// for io
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.antlr.v4.runtime.*; // for antlr4 
import org.antlr.v4.runtime.tree.*;// for antlr4
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import static java.lang.System.*;

public class Micro {

    public static void main(String[] args) throws FileNotFoundException, IOException, RecognitionException {

	//set input file
	ANTLRInputStream input = new ANTLRInputStream(new FileReader(args[0]));
	MicroLexer lexer = new MicroLexer(input); 	
	CommonTokenStream tokens = new CommonTokenStream(lexer);
	MicroParser parser = new MicroParser(tokens);
	SymbolTable st = new SymbolTable();

	//parse
	parser.program();

	//check the Symbol Table to find out if there are names in conflict
	HashMap<String, String> checkname = new HashMap<String, String>();
	for (int i = 0; i < st.symboltable.size(); i++) {
	    Symbol s = st.symboltable.get(i);
	    String name = s.name;
	    String functionname = s.functionname;
	    String value = functionname + i;
	    if (checkname.get(name) == null) {
		checkname.put(name, value);
	    } else {
		if (functionname.length() >= checkname.get(name).length()) {
		    System.out.println("SHADOW WARNING " + name);
		    checkname.put(name, value);
		} else if (functionname.equals(checkname.get(name).substring(0, functionname.length()))) {
		    System.out.println("DECLARATION ERROR " + name);
		    System.exit(0);
		} else {
		    System.out.println("SHADOW WARNING " + name);
		    checkname.put(name, value);
		}
	    }
	}
	
	System.out.println("Symbol table GLOBAL");
	for (int i = 0; i < st.symboltable.size(); i++) {
	    Symbol s = st.symboltable.get(i);
	    if (s.type.equals("FUNCTION")) {
		System.out.println("");
		System.out.println("Symbol table " + s.functionname);
	    } else if (s.type.equals("BLOCK")) {
		System.out.println("");
		System.out.println("Symbol table BLOCK " + s.name);
	    } else if (s.value == null) {
		System.out.println("name " + s.name + " type " + s.type);
	    } else if (s.value != null) {
		System.out.println("name " + s.name + " type STRING value " + s.value);
	    }
	}
    }
}
