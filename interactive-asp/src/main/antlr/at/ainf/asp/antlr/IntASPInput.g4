/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

grammar IntASPInput;

parse
 : section* EOF
 ;

section
 : aspsection | bksection | testsection  | comment
 ;

aspsection
    : asp NEW_LINE* asprule*
    ;

asprule
 : (idfact | fact | otherrules | comment) NEW_LINE*;

comment:
 COMMENT+ bodytext* NEW_LINE+;

idfact
 : (HEAD | BODYN | BODYP) LBR ruleid COMMA WS* atomid RBR DOT;

ruleid:
 ID+;

atomid:
 ID+;

fact: headtext DOT;

otherrules:
 headtext IMP bodytext DOT;

headtext
 : (ID | OTHER | COMMA | NEW_LINE | WS | LBR | RBR)+
 ;

bodytext
 : (ID | OTHER | COMMA | NEW_LINE | WS | LBR | RBR | HEAD | BODYN | BODYP )+
 ;

bksection
    : ( bk ) (NEW_LINE | csvline NEW_LINE*)*
    ;

testsection
    : ( bt | bf | ct | cf ) (WS|NEW_LINE)* ((csvline | comment) NEW_LINE*)*
    ;

csvline
 : value (WS|NEW_LINE)* (COMMA (WS|NEW_LINE)* value (WS|NEW_LINE)*)*
 ;

value
 : ID+
 ;


asp : ASP ;
bk  : BK  ;
bt  : BT  ;
bf  : BF  ;
ct  : CT  ;
cf  : CF ;

/*----------------
* LEXER RULES
*----------------*/

HEAD: 'head';
BODYN: 'bodyN';
BODYP: 'bodyP';

IMP: ':-';
DOT:     '.';
COMMENT: '%';
WS : (' ' | '\t' | '\f')+ ;
COMMA : ',';
NEW_LINE          : '\r'? '\n';

ASP: SECTION_TAG_START 'ASP' SECTION_TAG_END;
BK:  SECTION_TAG_START 'BK' SECTION_TAG_END;
BT:  SECTION_TAG_START 'BT' SECTION_TAG_END;
BF:  SECTION_TAG_START 'BF' SECTION_TAG_END;
CT:  SECTION_TAG_START 'CT' SECTION_TAG_END;
CF:  SECTION_TAG_START 'CF' SECTION_TAG_END;

NUMBER: DIGIT+;
ID : LETTER (ALPHANUM)*;

LBR: '(';
RBR: ')';

fragment SECTION_TAG_START : '[';
fragment SECTION_TAG_END   : ']';

fragment ALPHANUM :    (DIGIT | LETTER);
fragment LETTER: ('a'..'z' | 'A'..'Z');
fragment DIGIT: ('0'..'9');

OTHER             : . /* any other char: must be the last rule! */;