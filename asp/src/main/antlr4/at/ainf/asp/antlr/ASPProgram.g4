grammar ASPProgram;

/* @header {
	package at.ainf.asp.antlr;
}  */


/*----------------
* PARSER RULES
*----------------*/

/** The overall program consist of facts and rules. */
prog : (aspfact | asprule)+ ;

/** A rule can be a normal rule or a constraint. */
asprule : normrule | constraint ;

/** A fact is only the head followed by a dot. */
aspfact : head DOT ;

/** A normal rule has a head and body. */
normrule : head ENTAILS body DOT ;

/** A constraint has NO head, only a body. */
constraint : ENTAILS body DOT ;

/** An aggregate can have bounds, curly or squared brackets and conditional literals. */
aggregate : bound? equal? ( CURLBRL | SQURBRL ) (condliteral | COMMA)+ ( CURLBRR | SQURBRR ) bound? ;

/** The head consists of a literal or an aggregate. */
head : literal | aggregate ;

/** The body consists of one or more literals or aggregates separated with comma. */
body : (literal | aggregate | varassignment | notequal | COMMA)+ ;

/** A literal consists of symbols (no predicates) or symbols and terms (predicate literal). */
literal : SYMBOLS | ( SYMBOLS terms ) ;

/** A conditional literal is a literal with conditions. */
condliteral : (literal | CONDITION)+ ;

/** Variable assignments in body. */
varassignment : (SYMBOLS equal (INT | range) | (INT | range) equal SYMBOLS) ;

/** */
notequal : SYMBOLS nequal SYMBOLS ;

/** Ranges of interger values. */
range : INT RANGE INT ;

/** Terms consist of parenthesis and symbols (separated by comma). */
terms : PARENTHL (SYMBOLS | INT | COMMA)+ PARENTHR ;

/** Bounds of aggregates can be symbols. */
bound : SYMBOLS ;

/** Equality. */
equal : EQUAL ;

/** Non equality. */
nequal : NEQUAL ;



/*----------------
* LEXER RULES
*----------------*/

WS : (' ' | '\t' | '\n' | '\r' | '\f')+ -> skip ;
ENTAILS : ':-' ;
DOT : '.' ;
COMMA : ',' ;
CONDITION : ':' ;
PARENTHL : '(' ;
PARENTHR : ')' ;
CURLBRL : '{' ;
CURLBRR : '}' ;
SQURBRL : '[' ;
SQURBRR : ']' ;
EQUAL : '=' ;
NEQUAL : '!=' ;
RANGE : '..' ;
INT : ('0'..'9')+ ;
SYMBOLS : (' ' | 'a'..'z' | 'A'..'Z' | '0'..'9' | '_')+ ;
