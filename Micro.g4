grammar Micro;

/** Program */
program:
	PROGRAM id BEGIN pgm_body END; 

id:
	IDENTIFIER;

pgm_body:
	decl func_declarations;

decl:
	string_decl decl | var_decl decl | /* empty */;

/** Global String Declaration */
string_decl:
	STRING id IDENTITY str SEMICOLON;

str:
	STRINGLITERAL;

/** Variable Declaration */
var_decl:
	var_type id_list SEMICOLON;

var_type:
	INT | FLOAT;

any_type:
	var_type | VOID;

id_list:
	id id_tail;

id_tail:
	COMMA id id_tail | /* empty */;

id_list_rw:
	id id_tail_rw;

id_tail_rw:
	COMMA id id_tail_rw | /* empty*/;

/** Function Paramater List */
param_decl_list:
	param_decl param_decl_tail | /* empty */;

param_decl:
	var_type id;

param_decl_tail:
	COMMA param_decl param_decl_tail | /* empty */;

/** Function Declarations */
func_declarations:
	func_decl func_declarations | /* empty */;

func_decl:
	FUNCTION any_type id LBRACKET param_decl_list RBRACKET BEGIN func_body END;

func_body:
	decl stmt_list;

/** Statement List */
stmt_list:
	stmt stmt_list | /* empty */;

stmt:
	base_stmt | if_stmt | for_stmt;

base_stmt:
	assign_stmt | rwead_stmt | return_stmt;

/** Basic Statements */
assign_stmt:
	assign_expr SEMICOLON;

assign_expr:
	id IDENTITY expr;

rwead_stmt:
	RW LBRACKET id_list_rw RBRACKET SEMICOLON;

return_stmt:
	RETURN expr SEMICOLON;

/** Expressions */
expr:
	expr_prefix factor;

expr_prefix:
	expr_prefix factor addop | /* empty */;

factor:
	factor_prefix postfix_expr;
	
factor_prefix:
	factor_prefix postfix_expr mulop | /* empty */;	

postfix_expr:
	primary | call_expr;     
  
call_expr:
	id LBRACKET expr_list RBRACKET;

expr_list:
	expr expr_list_tail | /* empty */;

expr_list_tail:
	COMMA expr expr_list_tail | /* empty */;

primary:
	LBRACKET expr RBRACKET | id | INTLITERAL | FLOATLITERAL;

addop:
	ADDOP;

mulop:
	MULOP;

/** Complex Statements and Condition */ 
if_stmt:
	IF LBRACKET cond RBRACKET decl stmt_list else_part FI;

else_part:
	ELSE decl stmt_list | /* empty */;

cond:
	expr compop expr;

compop:
	COMPOP;

init_stmt:
	assign_expr | /* empty */;

incr_stmt:
	assign_expr | /* empty */;


/** ECE 573 students use this version of for_stmt */
for_stmt:
	FOR LBRACKET init_stmt SEMICOLON cond SEMICOLON incr_stmt RBRACKET decl aug_stmt_list ROF; 

/** CONTINUE and BREAK statements. ECE 573 students only */
aug_stmt_list:
	aug_stmt aug_stmt_list | /* empty */;

aug_stmt:
	base_stmt | aug_if_stmt | for_stmt | CB SEMICOLON;

/** Augmented IF statements for ECE 573 students */ 
aug_if_stmt:
	IF LBRACKET cond RBRACKET decl aug_stmt_list aug_else_part FI;

aug_else_part:
	ELSE decl aug_stmt_list | /* empty */;

PROGRAM:
	'PROGRAM';

BEGIN:
	'BEGIN';

END:
	'END';

FUNCTION:
	'FUNCTION';

RW:
	'READ' | 'WRITE';
	
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

CB:
	'CONTINUE' | 'BREAK';

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

COMPOP:
	'<' | '>' | '=' | '!=' | '<=' | '>=';

WS:
	(' ' | '\n' | '\t' | '\r') -> skip;

IDENTITY:
	':=';

COMMA:
	',';

ADDOP:
	'+' | '-';

MULOP:
	'*' | '/';

LBRACKET:
	'(';

RBRACKET:
	')';

SEMICOLON:
	';';




