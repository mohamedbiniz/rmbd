grammar AnswerSet;

@header {
	/* package at.ainf.asp.antlr; */
}

@parser::members {

}

/*---------------------------
* Author: Melanie Frühstück
*----------------------------*/

/*----------------
* PARSER RULES
*----------------*/

/**  */
output : begin (answerSet | unsatisfiable | satisfiable | unknown)+ end ;

begin : symbols solving? ;

end : (symbols)+ ;

/**  */
unsatisfiable : UNSATISFIABLE ;

/**  */
satisfiable : SATISFIABLE ;

/**  */
unknown : UNKNOWN ;

answerSet : answer (literal)+ optimization optimumFound? ;

literal : constant parenthL? terms? parenthR? ;

terms : (constant | DIGIT | literal | comma)+ ;

/**  */
symbols : (DIGIT | SYMBOLS | WHITESPACE | PREDICATE | DOT | CONDITION | PARENTHL | PARENTHR)+ ;

answer : ANSWER DIGIT ;

optimization : OPTIMIZATION ' ' DIGIT ;

optimumFound : OPTIMUMFOUND ;

whitespace : WHITESPACE ;

constant : whitespace? (PREDICATE | STRING) ;

parenthL : PARENTHL ;

parenthR : PARENTHR ;

comma : COMMA ;

solving : SOLVING ;


/*----------------
* LEXER RULES
*----------------*/

WHITESPACE : ' ' ;
WS : ('\t' | '\n' | '\r' | '\f')+ -> skip ;
UNSATISFIABLE : 'UNSATISFIABLE' ;
SATISFIABLE : 'SATISFIABLE' ;
UNKNOWN : 'UNKNOWN' ;
OPTIMUMFOUND : 'OPTIMUM FOUND' ;
SOLVING : 'Solving...' ;
CONDITION : ':' ;
DOT : '.' ;
COMMA : ',' ;
PARENTHL : '(' ;
PARENTHR : ')' ;
STRING : '"' (~('"'))* '"' ;
ANSWER : 'Answer: ' ;
OPTIMIZATION : 'Optimization:' ;
DIGIT : (INT)+ ;
fragment INT : '0'..'9' ;
PREDICATE : 'a'..'z' ('a'..'z' | 'A'..'Z' | '0'..'9' | '_')* ;
SYMBOLS : ('a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '-' | '+' | '/' )+ ;