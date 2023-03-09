grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

COMMENT: ('/*' [^*/]* '*/') | ('//' [^\n]* '\n');
INT : ('0' | [1-9][0-9]*);
LETTER: [a-zA-Z_$];
ID : LETTER (LETTER | [0-9])*;
BOOL: ('true' | 'false');

WS : [ \t\n\r\f]+ -> skip ;

program
    : (importDeclaration)* classDeclaration <EOF> ;

importDeclaration
    : 'import' ID ('.' ID)* ';'
    ;

classDeclaration
    : 'class' ID ('extends' ID)? '{' (varDeclaration)* (methodDeclaration)* '}'
    ;

varDeclaration
    : type ID ';'
    ;


methodDeclaration
     : ('public')? type ID '(' ( type ID (',' type ID)*)? ')' '{' (varDeclaration)* ( statement )* 'return' expression ';' '}'
     | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' ID ')' '{' (varDeclaration)* (statement)* '}'
     ;

type
    : 'int' '['']' #IntArray
    | 'boolean' #Bool
    | 'int' #Int
    | ID #CustomType
    ;

statement
    : '{' ( statement )* '}' #Scope
    | 'if' '(' expression ')' statement 'else' statement #If
    | 'while' '(' expression ')' statement #WhileLoop
    | expression ';' #SimpleStatement
    | ID '=' expression ';' #SimpleAssignment
    | ID '[' expression ']' '=' expression ';' #ArrayAssignment
    ;

expression
    :
    expression '.' ID '('  (expression ( ',' expression)*)? ')' #MethodCall
    | expression '.' 'length' #ArrayLength
    | expression '[' expression ']' #ArrayAccess
    | '(' expression ')' #Parenthesis
    | '!' expression #Negation
    | expression op=('*' | '/' | '%') expression #BinaryOp
    | expression op=('+' | '-') expression #BinaryOp
    | expression op=('<' | '>' | '<=' | '>=') expression #BinaryOp
    | expression op=('==' | '!=') expression #BinaryOp
    | expression op='&&' expression #BinaryOp
    | expression op='||' expression #BinaryOp
    | 'new' 'int' '[' expression ']' #IntArrayInstantiation
    | 'new' ID '(' ')' #Instantiation
    | value=INT #Integer
    | value=BOOL #Boolean
    | value=ID #Identifier
    | 'this' #ClassAccess
    | ID  #NormalIdentifier
    ;






















