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
 : aspsection | bksection | testsection
 ;

// (asp | bk ) NEW_LINE* line*) | ( bt | bf | ct | cf )

aspsection
    : asp NEW_LINE* asprule*
    ;

bksection
    : ( bk ) (NEW_LINE | csvline NEW_LINE)*
    ;

testsection
    : ( bt | bf | ct | cf ) (NEW_LINE | csvline NEW_LINE)*
    ;

asp
 : ASP
 ;

bk
 : BK  ;
bt
 : BT  ;
bf
 : BF  ;
ct
 : CT  ;
cf
 : CF ;


csvline
 : value (COMMA value)*
 ;

asprule
 : text NEW_LINE*;


value
 : ID+
 ;


text
 : (ID | OTHER | COMMA)+
 ;

/*----------------
* LEXER RULES
*----------------*/

WS : (' ' | '\t' | '\f')+ -> skip ;
COMMA : ',';
NEW_LINE          : '\r'? '\n';

ASP: SECTION_TAG_START 'ASP' SECTION_TAG_END;
BK:  SECTION_TAG_START 'BK' SECTION_TAG_END;
BT:  SECTION_TAG_START 'BT' SECTION_TAG_END;
BF:  SECTION_TAG_START 'BF' SECTION_TAG_END;
CT:  SECTION_TAG_START 'CT' SECTION_TAG_END;
CF:  SECTION_TAG_START 'CF' SECTION_TAG_END;


ID : LETTER (ALPHANUM)*;

fragment SECTION_TAG_START : '[';
fragment SECTION_TAG_END   : ']';

fragment ALPHANUM :    (DIGIT | LETTER);
fragment LETTER: ('a'..'z' | 'A'..'Z');
fragment DIGIT: ('0'..'9');

OTHER             : . /* any other char: must be the last rule! */;