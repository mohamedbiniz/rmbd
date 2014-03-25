/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

grammar IntASPOutput;

parse
 : line* (EOF?)
 ;

line:
(answerset | text) NEW_LINE*
;

answerset
    : interpretation WS diagnosis
    ;

interpretation:
    intliteral (WS intliteral)*
;

intliteral:
    NOT? intatom
    ;

intatom: INT LBR ID RBR;

diagnosis:
    diagatom (WS diagatom)*
    ;

diagatom:
 ((UNSAT | VIOL | UNSUP | ULOOP)  LBR ID RBR)
;

text
 : (ID | OTHER | COMMA | WS | NUMBER | DOT | NOT | RBR | LBR)+
 ;

/*----------------
* LEXER RULES
*----------------*/

INT: 'int';
UNSAT: 'unsatisfied';
VIOL: 'violated';
ULOOP: 'ufLoop';
UNSUP: 'unsupported';
LBR: '(';
RBR: ')';
NOT:   '-';
DOT:     '.';
COMMENT: '%';
WS : (' ' | '\t' | '\f')+ ;
COMMA : ',';
NEW_LINE          : '\r'? '\n';

ID : LETTER (ALPHANUM)*;
NUMBER: DIGIT+;

fragment ALPHANUM :    (DIGIT | LETTER);
fragment LETTER: ('a'..'z' | 'A'..'Z');
fragment DIGIT: ('0'..'9');

OTHER             : . /* any other char: must be the last rule! */;