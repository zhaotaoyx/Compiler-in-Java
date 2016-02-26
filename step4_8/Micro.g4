grammar Micro;

@header
{
	import java.util.*;
	import java.util.HashMap;
	import java.util.LinkedList;
	import java.util.Stack;
	import java.util.Vector;
}

@members
{
	String type = "type_init";
	String scope = "GLOBAL";
	HashMap<String, HashMap<String, Symbol>> st = SymbolTable.symboltable;
	HashMap<String, LinkedList<IRNode>> nl = IRNodesList.irnodeslist;
	Vector<String> pushvar = new Vector<String>();
	Stack<Integer> iflab = new Stack<Integer>();
	Stack<Integer> forlab = new Stack<Integer>();
	int regnum = 0;
	int labnum = 0;
	int parnum = 0;
	int lnum = 0;
	int pnum = 0;
	int index = 0;
	String action = "action_init";
	String op1 = "op1_init";
	String op2 = "op2_init";
	String result = "result_init";
	String operator = "operator_init";
	String postfix_expr = "";
	String factor_prefix = "";
	String factor = "";
	String expr_prefix = "";
	String expr = "";
	String expr_list = "";
	String expr_list_tail = "";
	String primary = "";
	Vector<String> funcalled = new Vector<String>();
}



/** Program */
program:
PROGRAM
{
	HashMap<String, Symbol> newtable = new HashMap<String, Symbol>();
	st.put(scope, newtable);
	LinkedList<IRNode> newlist = new LinkedList<IRNode>();
	nl.put(scope, newlist);
}
id BEGIN pgm_body END; 

id:
IDENTIFIER;

pgm_body:
decl func_declarations;

decl:
string_decl decl | var_decl decl | /* empty */;

/** Global String Declaration */
string_decl:
STRING id IDENTITY str SEMICOLON
{
	Symbol symbol = new Symbol($id.text, "STRING", $str.text);
	st.get(scope).put($id.text, symbol);
}
;

str:
STRINGLITERAL;

/** Variable Declaration */
var_decl:
var_type id_list SEMICOLON;

var_type:
INT 
{
	type = "INT";
}
| FLOAT
{
	type = "FLOAT";
}
;

any_type:
var_type | VOID;

id_list:
id 
{
	String value = scope.equals("GLOBAL") ? "" : "$"+"L"+Integer.toString(++lnum);
	Symbol symbol = new Symbol($id.text, type, value);
	st.get(scope).put($id.text, symbol);
}
id_tail;

id_tail:
COMMA id
{
	String value = scope.equals("GLOBAL") ? "" : "$"+"L"+Integer.toString(++lnum);
	Symbol symbol = new Symbol($id.text, type, value);
	st.get(scope).put($id.text, symbol);
}
id_tail | /* empty */;

id_list_rw:
id
{
	result = $id.text;
	Symbol symbol = st.get(scope).get(result);
	type = symbol.type;
	String curaction;
	switch (type) {
	case "INT" : curaction = action + "I"; break;
	case "FLOAT" : curaction = action + "F"; break;
	case "STRING" : curaction = action + "S"; break;
	default : curaction = action + "WRONG"; 
	}
	IRNode irnode = new IRNode(curaction, result);
	nl.get(scope).add(irnode);
}
id_tail_rw;

id_tail_rw:
COMMA id
{
	result = $id.text;
	Symbol symbol = st.get(scope).get(result);
	type = symbol.type;
	String curaction;
	switch (type) {
	case "INT" : curaction = action + "I"; break;
	case "FLOAT" : curaction = action + "F"; break;
	case "STRING" : curaction = action + "S"; break;
	default : curaction = action + "WRONG"; 
	}
	IRNode irnode = new IRNode(curaction, result);
	nl.get(scope).add(irnode);
}
id_tail_rw | /* empty*/;

/** Function Paramater List */
param_decl_list:
param_decl param_decl_tail | /* empty */;

param_decl:
var_type id
{
	Symbol symbol = new Symbol($id.text, type, "$"+"P"+Integer.toString(++pnum));
	st.get(scope).put($id.text, symbol);
	if (scope.equals("main")) {
		SymbolTable.mainparnum++;
	}
}
;

param_decl_tail:
COMMA param_decl param_decl_tail | /* empty */;

/** Function Declarations */
func_declarations:
func_decl func_declarations | /* empty */;

func_decl:
FUNCTION any_type id
{
	index = 0;
	scope = $id.text;
	HashMap<String, Symbol> newtable = new HashMap<String, Symbol>(st.get("GLOBAL"));
	st.put(scope, newtable);
	LinkedList<IRNode> newlist = new LinkedList<IRNode>();
	nl.put(scope, newlist);
	String curaction = "LABEL";
	String curresult = $id.text;
	IRNode irnode = new IRNode(curaction, curresult);
	nl.get(scope).add(irnode);
	IRNode lnode = new IRNode("LINK");
	nl.get(scope).add(lnode);
	lnum = 0;
	pnum = 0;
	SymbolTable.functype.put($id.text, $any_type.text);
}
LBRACKET param_decl_list RBRACKET BEGIN func_body 
{
	IRNode lastnode = nl.get(scope).getLast();
	if (!lastnode.action.equals("RET")) {
		IRNode retnode = new IRNode("RET");
		nl.get(scope).add(retnode);
	}
	SymbolTable.lnum.put(scope, lnum);
	SymbolTable.pnum.put(scope, pnum);
	scope = "GLOBAL";
}
END;

func_body:
decl stmt_list;

/** Statement List */
stmt_list:
stmt stmt_list | /* empty */;

stmt:
base_stmt | if_stmt | for_stmt;

base_stmt:
assign_stmt | read_stmt | write_stmt | return_stmt;

/** Basic Statements */
assign_stmt:
assign_expr SEMICOLON;

assign_expr:
id IDENTITY expr
{
	result = $id.text;
	Symbol symbol = st.get(scope).get(result);
	type = symbol.type;
	String curaction = type.equals("INT") ? "STOREI" : "STOREF";
	op1 = expr;
	expr = "";
	IRNode irnode = new IRNode(curaction, op1, result);
	nl.get(scope).add(irnode);
}
;

read_stmt:
READ
{
	action = "READ";
}
LBRACKET id_list_rw RBRACKET SEMICOLON;

write_stmt:
WRITE
{
	action = "WRITE";
}
LBRACKET id_list_rw RBRACKET SEMICOLON;

return_stmt:
RETURN expr SEMICOLON
{
	String curaction = type.equals("INT") ? "STOREI" : "STOREF";
	String curop1 = expr;
	expr = "";
	String curresult = "$" + "R" + Integer.toString(pnum+1);
	IRNode snode = new IRNode(curaction, curop1, curresult);
	nl.get(scope).add(snode);
	IRNode rnode = new IRNode("RET");
	nl.get(scope).add(rnode);
}
;

/** Expressions */
expr:
expr_prefix
{
	String curop1 = op1;
	String curaction = action;
	String curoperator = operator;
	String curexpr_prefix = expr_prefix;
	expr_prefix = "";
}
factor
{
	if (!curop1.equals("")) {
		op2 = factor;
		result = curop1 + curoperator + op2;
		curaction += type.equals("INT") ? "I" : "F";
		IRNode irnode = new IRNode(curaction, curop1, op2, result);
		nl.get(scope).add(irnode);
	}
	op1 = "";
	op2 = "";
	expr = curexpr_prefix + factor;
	factor = "";
}
;

expr_prefix:
expr_prefix
{
	String curop1 = op1;
	String curaction = action;
	String curoperator = operator;
	String curexpr_prefix = expr_prefix;
	expr_prefix = "";
}
factor
{
	if (!curop1.equals("")) {
		op2 = factor;
		result = curop1 + curoperator + op2;
		curaction = type.equals("INT") ? curaction + "I" : curaction + "F";
		IRNode irnode = new IRNode(curaction, curop1, op2, result);
		nl.get(scope).add(irnode);
		op1 = curop1 + curoperator + op2;
	}
	else {
		op1 = factor;
	}
}
addop
{
	expr_prefix = curexpr_prefix + factor + $addop.text;
	factor = "";
}
| /* empty */
{
	op1 = "";
	expr_prefix = "";
}
;

factor:
factor_prefix
{
	String curop1 = op1;
	String curaction = action;
	String curoperator = operator;
	String curfactor_prefix = factor_prefix;
	factor_prefix = "";
}
postfix_expr
{
	if (!curop1.equals("")) {
		op2 = postfix_expr;
		result = curop1 + curoperator + op2;
		curaction += type.equals("INT") ? "I" : "F";
		IRNode irnode = new IRNode(curaction, curop1, op2, result);
		nl.get(scope).add(irnode);
		op1 = curop1 + curoperator + postfix_expr;
	}
	else {
		op1 = postfix_expr;
	}
	factor = curfactor_prefix + postfix_expr;
	postfix_expr = "";
}
;
	
factor_prefix:
factor_prefix
{
	String curop1 = op1;
	String curaction = action;
	String curoperator = operator;
	String curfactor_prefix = factor_prefix;
	factor_prefix = "";
}
postfix_expr
{
	if (!curop1.equals("")) {
		op2 = postfix_expr;
		result = curop1 + curoperator + op2;
		curaction += type.equals("INT") ? "I" : "F";
		IRNode irnode = new IRNode(curaction, curop1, op2, result);
		nl.get(scope).add(irnode);
		op1 = curop1 + operator + postfix_expr;
	}
	else {
		op1 = postfix_expr;
	}
}
mulop
{
	factor_prefix = curfactor_prefix + postfix_expr + $mulop.text;
	postfix_expr = "";
}
| /* empty */
{
	op1 = "";
	factor_prefix = "";
}
;	

postfix_expr:
primary
{
	postfix_expr = primary;
	primary = "";
}
| call_expr
{
	postfix_expr = funcalled.lastElement();
}
;
  
call_expr:
id LBRACKET expr_list RBRACKET
{
	String curaction = "PUSH";
	IRNode node1 = new IRNode(curaction);
	nl.get(scope).add(node1);
	for (int i = 0; i < pushvar.size(); i++) {
		String curtype = type.equals("INT") ? "I" : "F";
		String curraction = curaction + curtype;
		IRNode node5 = new IRNode(curraction, pushvar.get(i));
		nl.get(scope).add(node5);
	}
	pushvar.clear();
	IRNode node2 = new IRNode("JSR", $id.text);
	nl.get(scope).add(node2);
	while (parnum > 0) {
		IRNode node3 = new IRNode("POP");
		nl.get(scope).add(node3);
		parnum--;
	}
	String curresult = $id.text + "(" + expr_list + ")";
	while (funcalled.contains(curresult)) {
		curresult += "!";
	}
	funcalled.add(curresult);
	IRNode node4 = new IRNode("POP", curresult);
	nl.get(scope).add(node4);
}
;

expr_list:
expr
{
	pushvar.add(expr);
	parnum++;
	String curexpr = expr;
	expr = "";
}
expr_list_tail
{
	expr_list = curexpr + expr_list_tail;
	expr_list_tail = "";
}
| /* empty */
{
	expr_list = "";
}
;

expr_list_tail:
COMMA expr
{
	pushvar.add(expr);
	parnum++;
	String curexpr = expr;
	expr = "";
}
expr_list_tail
{
	expr_list_tail = "," + curexpr + expr_list_tail;
}
| /* empty */
{
	expr_list_tail = "";
}
;

primary:
LBRACKET expr RBRACKET
{
	primary = "(" + expr + ")";
	expr = "";
}
| id
{
	Symbol symbol = st.get(scope).get($id.text);
	type = symbol.type;
	primary = $id.text;
}
| INTLITERAL
{
	type = "INT";
	primary = $INTLITERAL.text;
}
| FLOATLITERAL
{
	type = "FLOAT";
	primary = $FLOATLITERAL.text;
}
;

addop:
ADDOP
{
	operator = "+";
	action = "ADD";
}
| SUBOP
{
	operator = "-";
	action = "SUB";
}
;

mulop:
MULOP
{
	operator = "*";
	action = "MULT";
}
| DIVOP
{
	operator = "/";
	action = "DIV";
}
;

/** Complex Statements and Condition */ 
if_stmt:
IF
{
	int curlab = ++labnum;
	result = "label" + Integer.toString(curlab);
	iflab.push(curlab);
	++labnum;
}
LBRACKET cond RBRACKET decl stmt_list else_part FI
{
	iflab.pop();
}
;

else_part:
ELSE
{
	int curlab = iflab.peek();
	String curaction = "JUMP";
	String curresult = "label" + Integer.toString(curlab+1);
	IRNode jirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(jirnode);
	curaction = "LABEL";
	curresult = "label" + Integer.toString(curlab);
	IRNode lirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(lirnode);
}
decl stmt_list
{
	curaction = "LABEL";
	curresult = "label" + Integer.toString(curlab+1);
	IRNode irnode = new IRNode(curaction, curresult);
	nl.get(scope).add(irnode);
}
| /* empty */
{
	int curlab = iflab.peek();
	String curaction = "LABEL";
	String curresult = "label" + Integer.toString(curlab);
	IRNode node = new IRNode(curaction, curresult);
	nl.get(scope).add(node);
}
;

cond:
{
	String curresult = result;
}
expr
{
	String curop1 = expr;
	expr = "";
}
compop
{
	String curaction = action;
}
expr
{
	String curop2 = expr;
	expr = "";
	curaction += type.equals("INT") ? "I" : "F";
	IRNode irnode = new IRNode(curaction, curop1, curop2, curresult);
	nl.get(scope).add(irnode);
}
;

compop:
GT
{
	action = "LE";
}
| GE
{
	action = "LT";
}
| LT
{
	action = "GE";
}
| LE
{
	action = "GT";
}
| NE
{
	action = "EQ";
}
| EQ
{
	action = "NE";
}
;

init_stmt:
assign_expr | /* empty */;

incr_stmt:
assign_expr | /* empty */;


/** ECE 573 students use this version of for_stmt */
for_stmt:
FOR LBRACKET init_stmt SEMICOLON
{
	int curlab = ++labnum;
	forlab.push(curlab);
	String fornum = Integer.toString(curlab);
	labnum += 2;
	String curaction = "LABEL";
	String curresult = "label" + fornum;
	IRNode iirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(iirnode);
	result = "label" + Integer.toString(curlab+2);
}
cond SEMICOLON
{
	curaction = "INCR_STMT";
	curresult = "begin" + fornum;
	IRNode inirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(inirnode);
	curaction = "LABEL";
	curresult = "label" + Integer.toString(curlab+1);
	IRNode lirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(lirnode);
}
incr_stmt
{
	curaction = "INCR_STMT";
	curresult = "end" + fornum;
	IRNode incrirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(incrirnode);
}
RBRACKET decl aug_stmt_list
{
	curaction = "INCR_STMT";
	curresult = "here" + fornum;
	IRNode ihnode = new IRNode(curaction, curresult);
	nl.get(scope).add(ihnode);
	curaction = "JUMP";
	curresult = "label" + fornum;
	IRNode irnode = new IRNode(curaction, curresult);
	nl.get(scope).add(irnode);
	curaction = "LABEL";
	curresult = "label" + Integer.toString(curlab+2);
	IRNode node = new IRNode(curaction, curresult);
	nl.get(scope).add(node);
}
ROF
{
	forlab.pop();
}
;

/** CONTINUE and BREAK statements. ECE 573 students only */
aug_stmt_list:
aug_stmt aug_stmt_list | /* empty */;

aug_stmt:
base_stmt | aug_if_stmt | for_stmt | CONTINUE
{
	int curlab = forlab.peek();
	String curaction = "JUMP";
	String curresult = "label" + Integer.toString(curlab+1);
	IRNode irnode = new IRNode(curaction, curresult);
	nl.get(scope).add(irnode);
}
SEMICOLON | BREAK
{
	int curlab = forlab.peek();
	String curaction = "JUMP";
	String curresult = "label" + Integer.toString(curlab+2);
	IRNode node = new IRNode(curaction, curresult);
	nl.get(scope).add(node);
}
SEMICOLON;

/** Augmented IF statements for ECE 573 students */ 
aug_if_stmt:
IF
{
	int curlab = ++labnum;
	result = "label" + Integer.toString(curlab);
	iflab.push(curlab);
	++labnum;
}
LBRACKET cond RBRACKET decl aug_stmt_list aug_else_part FI
{
	iflab.pop();
}
;

aug_else_part:
ELSE
{
	int curlab = iflab.peek();
	String curaction = "JUMP";
	String curresult = "label" + Integer.toString(curlab+1);
	IRNode jirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(jirnode);
	curaction = "LABEL";
	curresult = "label" + Integer.toString(curlab);
	IRNode lirnode = new IRNode(curaction, curresult);
	nl.get(scope).add(lirnode);
}
decl aug_stmt_list
{
	curaction = "LABEL";
	curresult = "label" + Integer.toString(curlab+1);
	IRNode irnode = new IRNode(curaction, curresult);
	nl.get(scope).add(irnode);
}
| /* empty */
{
	int curlab = iflab.peek();
	String curaction = "LABEL";
	String curresult = "label" + Integer.toString(curlab);
	IRNode node = new IRNode(curaction, curresult);
	nl.get(scope).add(node);
}
;

PROGRAM:
'PROGRAM';

BEGIN:
'BEGIN';

END:
'END';

FUNCTION:
'FUNCTION';

READ:
'READ';

WRITE:
'WRITE';
	
IF:
'IF';

ELSE:
'ELSE';

FI:
'FI';

FOR:
'FOR';

ROF:
'ROF';

CONTINUE:
'CONTINUE';

BREAK:
'BREAK';

RETURN:
'RETURN';

INT:
'INT';

FLOAT:
'FLOAT';

VOID:
'VOID';

STRING:
'STRING';

IDENTIFIER:
('a'..'z' | 'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')*;

INTLITERAL:
('0'..'9')+;

FLOATLITERAL:
('0'..'9')+ '.' ('0'..'9')+;

STRINGLITERAL:
('"') (~('"'))* ('"');

COMMENT:
('--' (~('\n')*'\n')) -> skip;

GT:
'>';

GE:
'>=';

LT:
'<';

LE:
'<=';

NE:
'!=';

EQ:
'=';

WS:
(' ' | '\n' | '\t' | '\r') -> skip;

IDENTITY:
':=';

COMMA:
',';

ADDOP:
'+';

SUBOP:
'-';

MULOP:
'*';

DIVOP:
'/';

LBRACKET:
'(';

RBRACKET:
')';

SEMICOLON:
';';




