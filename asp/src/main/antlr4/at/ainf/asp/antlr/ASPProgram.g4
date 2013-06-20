                                                                     
                                                                     
                                                                     
                                             
grammar ASPProgram;

/* @header {
	package antlr;
}  */

@parser::members { 

}


/*----------------
* PARSER RULES
*----------------*/

prog	: (asprule | aspfact)+ ;

aspfact	: head DOT ;

asprule	: hbrule | constraint ;

hbrule : head ENTAILS body DOT ;

constraint : ENTAILS body DOT ;

head : literal | choiceRule ;

body : (literal | hornConstraint | maxConstraint | builtIns | comma)+ ;

hornConstraint : bound? bracketL (literal (equal weight)? | condition | comma)+ bracketR ;

maxConstraint : bound? max bracketL (literal (equal weight)? | comma)+ bracketR ;

choiceRule : bound? choiceBracL (literal | condition | comma)+ choiceBracR bound? ;

literal : constants | predicate ;

predicate : constants parenthL terms parenthR ;

builtIns : varAss | inequality | equality ;

inequality : (variable | DIGIT | constants) nequal (variable | DIGIT | constants) ;

varAss : variable equal DIGIT ;

equality : (variable | DIGIT | constants) equal equal? (variable | DIGIT | constants) ;

terms : (variable | constants | DIGIT | rangeDigit | comma)+ ;

rangeDigit : DIGIT range DIGIT ;

range : RANGE ;

weight : DIGIT ;

equal : EQUAL ;

nequal : NEQUAL ;

bound : DIGIT | VARIABLE ;

variable : VARIABLE ;

constants : PREDICATE ;

parenthL : PARENTHL ;

parenthR : PARENTHR ;

choiceBracL : CURLBRL ;

choiceBracR : CURLBRR ;

bracketL : CURLBRL | SQURBRL ;

bracketR : CURLBRR | SQURBRR ;

condition : CONDITION ;

max : MAX ;

comma : COMMA ;


/*----------------
* LEXER RULES
*----------------*/

/** SYMBOLS : (' ' | 'a'..'z' | 'A'..'Z' | '0'..'9' | '_')+ ; */

WS : (' ' | '\t' | '\n' | '\r' | '\f')+ -> skip ;
COMMENT : ( '%' ~[\r\n]* '\r'? '\n' | '%*' .*? '*%' ) -> skip ;
ENTAILS : ':-' ;
DOT : '.' ;
COMMA : ',' ;
CONDITION : ':' ;
NAF : 'not' ;
MAX : '#max' ;
PARENTHL : '(' ;
PARENTHR : ')' ;
CURLBRL : '{' ;
CURLBRR : '}' ;
SQURBRL : '[' ;
SQURBRR : ']' ;
EQUAL : '=' ;
NEQUAL : '!=' ;
RANGE : '..' ;
DIGIT : (INT)+ ;
fragment INT : '0'..'9' ;
VARIABLE : 'A'..'Z' ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')* ;
PREDICATE : 'a'..'z' ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')* ;
