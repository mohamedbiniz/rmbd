grammar ASPProgram;

@header {
	/* package at.ainf.asp.antlr; */
	import at.ainf.asp.model.exceptions.IllegalParserElementException;
}

/*---------------------------
* Author: Melanie Frühstück
*----------------------------*/

/*----------------
* PARSER RULES
*----------------*/

prog	: (asprule | aspfact)+ ;
		catch[RecognitionException e] {
			try {
				throw new IllegalParserElementException("Probably the parser cannot deal with some program elements.");
			} catch (IllegalParserElementException e1) {
				e1.printStackTrace();
			}
			System.exit(1);
		}
		
		
aspfact	: head DOT | constantSymbol constant equal DIGIT DOT ;

asprule	: hbrule | constraint ;

hbrule : head ENTAILS body DOT ;

constraint : ENTAILS body DOT ;

head : literal | choiceRule ;

body : (literal | hornConstraint | maxConstraint | builtIns | arithmetic | upperBoundCardinalityNOT | defaultNegationNOT | otherConstraints | comma)+ ;



/** Cardinality constraints that are monotone. */
hornConstraint : bound? bracketL ((literal | builtIns | defaultNegatedLiteralsNOT) (equal weight)? | condition | comma)+ bracketR ;

maxConstraint : bound? max bracketL ((literal | builtIns | defaultNegatedLiteralsNOT) (equal weight)? | condition | comma)+ bracketR ;

choiceRule : bound? choiceBracL (literal | builtIns | condition | comma)+ choiceBracR bound? ;



/** Defining literals, can be constants, built ins, arithmetics, or predicate literals. */
literal : constant | predicate ;

predicate : constant parenthL terms parenthR ;

terms : (variable | constant | DIGIT | rangeDigit | arithmetic | semicolon | predicate | comma)+ ;



/** Defining comparing built ins. */
builtIns : (rangeDigit | variable | DIGIT | constant | arithmetic) builtInSign (rangeDigit | variable | DIGIT | constant | arithmetic) ;

builtInSign : lowerequal | lower | greaterequal | greater | equal equal? | notequal ;



/** Defining arithmetics. */
arithmetic : (variable | DIGIT | constant) (arithmeticSign (variable | DIGIT | constant))+ ;

arithmeticSign : plus | minus | mult | div ;



/* NOT ALLOWED CONSTRUCTS:
* Cardinality constraints with upper bounds in the body are NOT ALLOWED. */
upperBoundCardinalityNOT : bound? bracketL (literal (equal weight)? | condition | comma)+ bracketR bound ;

/** #even, #odd and #min constraints are NOT ALLOWED. */
otherConstraints : evenConstraint | oddConstraint | minConstraint ;

evenConstraint : even choiceBracL (literal | builtIns | condition | comma)+ choiceBracR ;

oddConstraint : odd choiceBracL (literal | builtIns | condition | comma)+ choiceBracR ;

minConstraint : bound? min bracketL ((literal | builtIns | defaultNegatedLiteralsNOT) (equal weight)? | condition | comma)+ bracketR ;

/** There is NO default negation allowed in monotone programs. */
defaultNegationNOT : defaultNegatedLiteralsNOT | defaultNegatedAggregateNOT ;

defaultNegatedLiteralsNOT : NOT (literal | builtIns | arithmetic) ;

defaultNegatedAggregateNOT : NOT (maxConstraint | hornConstraint) ;



/** Defining other program elements. */
rangeDigit : (DIGIT | VARIABLE | constant) range (DIGIT | VARIABLE | constant) ;

range : RANGE ;

weight : DIGIT | variable ;

equal : EQUAL ;

notequal : NOTEQUAL ;

lower : LOWER ;

lowerequal : LOWEREQUAL ;

greater : GREATER ;

greaterequal : GREATEREQUAL ;

plus : PLUS ;

minus : MINUS ;

div : DIV ;

mult : MULT ;

bound : DIGIT | VARIABLE | arithmetic | constant ;

variable : VARIABLE ;

constant : PREDICATE | STRING ;

parenthL : PARENTHL ;

parenthR : PARENTHR ;

choiceBracL : CURLBRL ;

choiceBracR : CURLBRR ;

bracketL : CURLBRL | SQURBRL ;

bracketR : CURLBRR | SQURBRR ;

condition : CONDITION ;

max : MAX ;

min : MIN ;

odd : ODD ;

even : EVEN ;

constantSymbol : CONST ;

semicolon : SEMICOLON ;

comma : COMMA ;


/*----------------
* LEXER RULES
*----------------*/

/** SYMBOLS : (' ' | 'a'..'z' | 'A'..'Z' | '0'..'9' | '_')+ ; */

WS : (' ' | '\t' | '\n' | '\r' | '\f')+ -> skip ;
COMMENT : ( '%' ~[\r\n]* '\r'? '\n' | '%*' .*? '*%' ) -> skip ;
HIDESHOW : ('#hide' | '#show') .*? '\n' -> skip ;
ENTAILS : ':-' ;
DOT : '.' ;
COMMA : ',' ;
CONDITION : ':' ;
NAF : 'not' ;
MAX : '#max' ;
EVEN : '#even' ;
ODD : '#odd' ;
MIN : '#min' ;
CONST : '#const ' ;
PARENTHL : '(' ;
PARENTHR : ')' ;
CURLBRL : '{' ;
CURLBRR : '}' ;
SQURBRL : '[' ;
SQURBRR : ']' ;
EQUAL : '=' ;
NOTEQUAL : '!=' ;
LOWER : '<' ;
LOWEREQUAL : '<=' ;
GREATER : '>' ;
GREATEREQUAL : '>=' ;
PLUS : '+' ;
MINUS :	'-' ;
DIV : '/' ;
MULT : '*' ;
RANGE : '..' ;
SEMICOLON : ';' ;
DIGIT : (INT)+ ;
fragment INT : '0'..'9' ;
NOT : 'not ' ;
STRING : '"' (~('"'))* '"' ;
VARIABLE : 'A'..'Z' ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')* ;
PREDICATE : 'a'..'z' ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')* ;