grammar ASPOutput;

/* @header {
	package antlr;
} */

/*----------------
* PARSER RULES
*----------------*/

/**  */
output : ( ( (unsatisfiable)+ | (satisfiable)+ ) | other)+ ;

/**  */
unsatisfiable : UNSATISFIABLE ;

/**  */
satisfiable : SATISFIABLE ;

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
CONDITION : ':' ;
DOT : '.' ;
PARENTHL : '(' ;
PARENTHR : ')' ;
SYMBOLS : (' ' | 'a'..'z' | 'A'..'Z' | '0'..'9' | '_' )+ ;