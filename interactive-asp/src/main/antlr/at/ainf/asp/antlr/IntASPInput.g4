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
 : (asp | bk | bt | bf | ct | cf) NEW_LINE* line*
 ;

asp
 : SECTION_TAG_START ASP SECTION_TAG_END
 ;

bk
 : SECTION_TAG_START BK SECTION_TAG_END
 ;
bt
 : SECTION_TAG_START BT SECTION_TAG_END
 ;
bf
 : SECTION_TAG_START BF SECTION_TAG_END
 ;
ct
 : SECTION_TAG_START CT SECTION_TAG_END
 ;
cf
 : SECTION_TAG_START CF SECTION_TAG_END
 ;

line
 : value=text (NEW_LINE | EOF) ;

text
 : OTHER+
 ;

/*----------------
* LEXER RULES
*----------------*/

WS : (' ' | '\t' | '\f')+ -> skip ;
COMMA : ',';
SECTION_TAG_START : '[';
SECTION_TAG_END   : ']';
NEW_LINE          : '\r'? '\n';

ASP: 'ASP';
BK: 'BK';
BT: 'BT';
BF: 'BF';
CT: 'CT';
CF: 'CF';

OTHER             : . /* any other char: must be the last rule! */;