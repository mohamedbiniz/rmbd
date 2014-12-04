grammar Program;

@header {
	/* package at.ainf.asp.antlr; */
}

/*---------------------------
* Author: Melanie Frühstück
*----------------------------*/

/*----------------
* PARSER RULES
*----------------*/

prog	: (asprule | aspfact)+ ;		
		
aspfact	: head DOT | constantSymbol constant equal DIGIT DOT ;

asprule	: hbrule | constraint ;

hbrule : head ENTAILS body DOT ;

constraint : ENTAILS body DOT ;

head : literal | choiceRule | conditionalLiteral ;

body : (literal | builtIns | arithmetic | cardinalityConstraint | defaultNegation | comma | conditionalLiteral)+ ;



/** Cardinality constraints */
cardinalityConstraint: variable? equal? operator? bound? bracketL (conditionalLiteral (equal weight)?)+ bracketR bound? ;

choiceRule : bound? choiceBracL (conditionalLiteral)+ choiceBracR bound? ;



/** Defining literals, can be constants, built ins, arithmetics, or predicate literals. */
literal : constant | predicate ;

predicate : constant parenthL terms parenthR ;

terms : (variable | constant | DIGIT | rangeDigit | arithmetic | semicolon | predicate | comma)+ ;

conditionalLiteral : (literal | comma | condition | builtIns | semicolon | defaultNegatedLiterals)+ ;



/** Defining comparing built ins. */
builtIns : (rangeDigit | variable | DIGIT | constant | arithmetic) builtInSign (rangeDigit | variable | DIGIT | constant | arithmetic) ;

builtInSign : lowerequal | lower | greaterequal | greater | equal equal? | notequal ;



/** Defining arithmetics. */
arithmetic : (variable | DIGIT | constant) (arithmeticSign (variable | DIGIT | constant))+ ;

arithmeticSign : plus | minus | mult | div ;
operator : min | max | odd | even | count | sum ;



/** There is NO default negation allowed in monotone programs. */
defaultNegation : defaultNegatedLiterals | defaultNegatedAggregate ;

defaultNegatedLiterals : NOT (literal | builtIns | arithmetic) ;

defaultNegatedAggregate : NOT cardinalityConstraint ;



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

count : COUNT ;

sum : SUM ;

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
COUNT : '#count' ;
SUM : '#sum' ;
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