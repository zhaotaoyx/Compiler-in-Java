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

	int type_index;
	String type_name_token = null;
	Token token;

	//set input file
	ANTLRInputStream input = new ANTLRInputStream(new FileReader(args[0]));
	MicroLexer lexer = new MicroLexer(input); 	
	CommonTokenStream tokens = new CommonTokenStream(lexer);
	MicroParser parser = new MicroParser(tokens);

	//print input tokens
        for (token = lexer.nextToken(); token.getType() != Token.EOF; token = lexer.nextToken()) {

	    type_index = token.getType();

	    switch(type_index) {
	    case MicroLexer.FLOATLITERAL:type_name_token = "FLOATLITERAL";
		break;
	    case MicroLexer.IDENTIFIER:type_name_token = "IDENTIFIER";
		break;
	    case MicroLexer.INTLITERAL:type_name_token = "INTLITERAL";
		break;
	    case MicroLexer.PROGRAM:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.BEGIN:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.END:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.FUNCTION:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.RW:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.IF:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.ELSE:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.FI:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.FOR:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.ROF:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.CB:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.RETURN:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.INT:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.FLOAT:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.VOID:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.STRING:type_name_token = "KEYWORD";
		break;
	    case MicroLexer.COMPOP:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.IDENTITY:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.COMMA:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.ADDOP:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.MULOP:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.LBRACKET:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.RBRACKET:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.SEMICOLON:type_name_token = "OPERATOR";
		break;
	    case MicroLexer.STRINGLITERAL:type_name_token = "STRINGLITERAL";
		break;     		
	    }
	    System.out.println("Token Type: " + type_name_token);
	    System.out.println("Value: " + token.getText()); 
	}
    }
}
