grammar Arduinoml;


/******************
 ** Parser rules **
 ******************/

root            :   declaration bricks states EOF;

declaration     :   'application' name=IDENTIFIER;

bricks          :   (sensor|actuator)+;
    sensor      :   'sensor'   location ;
    actuator    :   'actuator' location ;
    location    :   id=IDENTIFIER ':' port=PORT_NUMBER;

states          :   state+;
    state       :   initial? name=IDENTIFIER '{'  action* transition+ '}';
    action      :   receiver=IDENTIFIER '<=' value=SIGNAL;
    transition  :   (expr)? '=>' next=IDENTIFIER;
    initial     :   '->';


/*****************
 ** Expressions **
 *****************/

expr            :   orExpr;
orExpr          :   andExpr ( OR andExpr )*;
andExpr         :   atom ( AND atom )*;
atom            :   IDENTIFIER 'is' SIGNAL;


/*****************
 ** Lexer rules **
 *****************/

PORT_NUMBER     :   [1-9] | '11' | '12';
SIGNAL          :   'HIGH' | 'LOW';
AND             :   'and';
OR              :   'or';
IDENTIFIER      :   [a-zA-Z_] [a-zA-Z0-9_]*;

/*************
 ** Helpers **
 *************/

fragment LOWERCASE  : [a-z];                                 // abstract rule, does not really exists
fragment UPPERCASE  : [A-Z];
NEWLINE             : ('\r'? '\n' | '\r')+      -> skip;
WS                  : ((' ' | '\t')+)           -> skip;     // who cares about whitespaces?
COMMENT             : '#' ~( '\r' | '\n' )*     -> skip;     // Single line comments, starting with a #
