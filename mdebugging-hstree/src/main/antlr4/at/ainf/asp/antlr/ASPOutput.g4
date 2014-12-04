grammar ASPOutput;

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
output : ( (unsatisfiable | satisfiable | unknown)+ | other)+ ;

/**  */
unsatisfiable : UNSATISFIABLE ;

/**  */
satisfiable : SATISFIABLE ;

/**  */
unknown : UNKNOWN ;

/**  */
other : symbols ;

/**  */
symbols : ( SYMBOLS | DOT | CONDITION | PARENTHL | PARENTHR )+ ;


/*----------------
* LEXER RULES
*----------------*/

WS : (' ' | '\t' | '\n' | '\r' | '\f')+ -> skip ;
UNSATISFIABLE : 'UNSATISFIABLE' ;
SATISFIABLE : 'SATISFIABLE' ;
UNKNOWN : 'UNKNOWN' ;
CONDITION : ':' ;
DOT : '.' ;
PARENTHL : '(' ;
PARENTHR : ')' ;
SYMBOLS : (' ' | 'a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '+' | '/' )+ ;