import java.io.FileInputStream;// for io
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ClassCastException;

import org.antlr.v4.runtime.*; // for antlr4 
import org.antlr.v4.runtime.tree.*;// for antlr4
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.*;
import java.util.LinkedList;
import static java.lang.System.*;

public class Micro {

    public static void main(String[] args) throws FileNotFoundException, IOException, RecognitionException, ClassCastException {

	//set input file
	ANTLRInputStream input = new ANTLRInputStream(new FileReader(args[0]));
	MicroLexer lexer = new MicroLexer(input); 	
	CommonTokenStream tokens = new CommonTokenStream(lexer);
	MicroParser parser = new MicroParser(tokens);

	//parse the input code
	parser.program();

	//SymbolTable.printsymboltable();
	IRNodesList.printirnodeslist();
	
	Revise.revise();
	//Reduce.reduce();
	

	int number_of_registers = 1000;
	RegisterAllocation.registerallocation(number_of_registers);
	IRNodesList.printirnl();
	Translator.translator(number_of_registers);
    }
}
