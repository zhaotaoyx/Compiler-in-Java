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
	boolean acceptornot = true;
	//set input file
	ANTLRInputStream input = new ANTLRInputStream(new FileReader(args[0]));
	MicroLexer lexer = new MicroLexer(input); 	
	CommonTokenStream tokens = new CommonTokenStream(lexer);
	MicroParser parser = new MicroParser(tokens);

	//set new error strategy
	parser.setErrorHandler(new NewErrorStrategy());
	NewErrorStrategy nes = new NewErrorStrategy();

	//parse the input code
	parser.program();

	acceptornot = nes.getAcceptornot();

	if (acceptornot) {
	    System.out.println("Accepted");
	} else {
	    System.out.println("Not accepted");
	}
    }
}
