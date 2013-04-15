grammar Constraint;
options 
{
	output=AST;
	ASTLabelType=CommonTree;	
}

tokens
{
	NEGATE;
}

@header {package at.ainf.choco2.parser;
import java.util.LinkedList;
import java.util.List;
import java.lang.String;
import choco.kernel.model.constraints.Constraint;
}
@lexer::header {package at.ainf.choco2.parser;}

@members{
	private IParserHelper parser;
	
	public void setParserHelper(IParserHelper prs)
	{
	    this.parser = prs;
	}
	
	public IParserHelper getParserHelper()
	{
	    return this.parser;
	}
	
	public void reportError(RecognitionException e) {
		super.reportError(e);
		getParserHelper().addError(e);
	} 
}


expression
@init{this.parser.getErrors().clear();}: 		    
	    (varDef | constraint)+ EOF;
	
NEWLINE	: ( '\r'? '\n');

varDef: name=IDENT ':' typeDef [name.getText()] ';' NEWLINE?;

typeDef [String name]: intDef [name] | realDef [name] | boolDef  [name]| stringDef[name];

intDef[String name]: 'int' (intBound[name] | intEnum[name]);
intBound [String name]
@after{this.parser.addIntVar(name, up, low);}
	: '[' up = INTEGER ',' low = INTEGER']';
	
intEnum [String name]
@init{List<Token> list = new LinkedList<Token>();}
@after{this.parser.addIntVar(name, list);}
	: '(' v = INTEGER {list.add(v);} (',' v = INTEGER {list.add(v);})*')';


realDef [String name]: 'real' (realBound [name]);
realBound [String name ]@after{this.parser.addReadVar(name, up, low);}
	: '[' up = FLOAT ',' low = FLOAT']';

boolDef[String name]@after{this.parser.addBoolVar(name);}
	: 'boolean';
stringDef[String name]
@init{List<String> list = new LinkedList<String>();}
@after{this.parser.addStringIntVar(name, list);}
: 'string' '(' v = STRING {list.add(v.getText().replaceAll("\'", ""));} (',' v = STRING {list.add(v.getText().replaceAll("\'", ""));})* ')';


// -------------------------------------

constraint 
@after{
this.parser.addConstraint(name.getText(), type.getText().charAt(0), l.con);}
	: '[' name = IDENT ',' type = TYPE (l = implicationExpression) ';' NEWLINE?
	;
	

implicationExpression returns [Constraint con]
	:	 l = logicalOrExpression (IMPLIES^ r = logicalOrExpression)*
	{if (r != null) 
		$con = this.parser.implies(l.con,r.con); 
	else $con = l.con;}
	;
IMPLIES	:	'->';
	
logicalOrExpression returns [Constraint con]
@init{List<Constraint> list = new LinkedList<Constraint>();}
@after{$con =  this.parser.or(list);} // toConstraintArray
	:	l = logicalAndExpression {list.add(l.con);} (OR^ l = logicalAndExpression {list.add(l.con);})* 
	;
OR 	: 	'||' | 'or';
  
logicalAndExpression returns [Constraint con]
@init{List<Constraint> list = new LinkedList<Constraint>();}
@after{$con =  this.parser.and(list);} // toConstraintArray
	:	l = unaryExpression {list.add(l.con);} (AND^ l = unaryExpression {list.add(l.con);})* 
	;
AND : 	'&&' | 'and';

unaryExpression returns [Constraint con]
	:	r = constant {$con = r.con;}
	|	v = relationalExpression {$con = v.con;}
    	|	NOT^ v = relationalExpression {$con =  this.parser.negate(v.con);}
    	|	MINUS relationalExpression -> ^(NEGATE relationalExpression)
   	;
constant returns [Constraint con] @after{$con = this.parser.createFixConstraint(l.getText());}: l = BOOLEAN;
  
NOT	:	'!' | 'not';
MINUS	:	'-';
 
relationalExpression returns [Constraint con]
	:	
	l =  value ( e = EQUALS| e = NOTEQUALS| e = LT| e = LTEQ| e = GT| e = GTEQ)^ r = value 
		{$con =  this.parser.createConstraint(input, e.getType(), l.obj, r.obj);}	
	| ('('^) log = implicationExpression (')'^) {$con = log.con;}
	;
	
EQUALS	:	'=' | '==';
NOTEQUALS 
	:	'!=' | '<>';
LT	:	'<';
LTEQ	:	'<=';
GT	:	'>';
GTEQ	:	'>=';

value returns [IValue obj] 
	: i = INTEGER {$obj = this.parser.createIntValue(i);}
	| f = FLOAT {$obj = this.parser.createRealValue(f);}	
	| b = BOOLEAN {$obj = this.parser.createBooleanValue(b);}
	| s = STRING {$obj = this.parser.createStringValue(s);}
	| id = IDENT {$obj=this.parser.createIdentValue(id);}
	;

BOOLEAN: 'true' | 'false';
	
INTEGER: ('0'..'9')+;

FLOAT 
	:	('0'..'9')* '.' ('0'..'9')+
	;

STRING
    	:  	'\'' ( EscapeSequence | ~('\\'|'\'') )* '\''
    	;
	
IDENT
	:	('a'..'z' | 'A'..'Z' | '_') ('a'..'z' | 'A'..'Z' | '_' | '0'..'9')*
	;


TYPE 	:	('u' | 'c')']';

/* Character escape */
fragment
EscapeSequence
    	:   	'\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    	;

fragment
HexDigit 
	: 	('0'..'9'|'a'..'f'|'A'..'F') ;

fragment
UnicodeEscape
    	:   	'\\' 'u' HexDigit HexDigit HexDigit HexDigit
    	;

/* Ignore white spaces */	
WS: (' '|'\n'|'\r'|'\t'|'\u000C')+ {skip();} ;

MULTI_COMMENT
    :   '/*' ( options {greedy=false;} : . )* '*/' {skip();}
    ;

LINE_COMMENT
    : '//' ~('\n'|'\r')* NEWLINE {skip();}
    ;
