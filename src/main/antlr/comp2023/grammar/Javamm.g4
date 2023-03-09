grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

WS : [ \t\n\r\f]+ -> skip ;
COMMENT: ('/*' [^*/]* '*/') | ('//' [^\n]* '\n');
BOOL: ('true' | 'false');

classDeclaration
    : 'class' className=ID ('extends' superClassName=ID)? '{' (varDeclaration)* (methodDeclaration)* '}'
    ;

methodDeclaration
     : ('public')? type methodName=ID '(' ( type ID (',' type ID)*)? ')' '{' (varDeclaration)* ( statement )* 'return' expression ';' '}'
     | ('public')? 'static' 'void' 'main' '(' type '[' ']' ID ')' '{' (varDeclaration)* (statement)* '}'
     ;

program
    : (importDeclaration)* classDeclaration <EOF> ;

statement
    : '{' ( statement )* '}' #Scope
    | 'if' '(' expression ')' statement 'else' statement #If
    | 'while' '(' expression ')' statement #WhileLoop
    | expression ';' #SimpleStatement
    | varName=ID '=' expression ';' #SimpleAssignment
    | varName=ID '[' expression ']' '=' expression ';' #ArrayAssignment
    ;

expression
    :
    expression '.' methodName=ID '('  (expression ( ',' expression)*)? ')' #MethodCall
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
    | 'new' className=ID '(' ')' #Instantiation
    | value=INT #Integer
    | value=BOOL #Boolean
    | value=ID #Identifier
    | 'this' #ClassAccess
    ;

importDeclaration
    : 'import' ID ('.' ID)* ';'
    ;

varDeclaration
    : type ID ';'
    ;
type
    : 'int' '['']' #IntArray
    | 'boolean' #Bool
    | 'int' #Int
    | typeName=ID #CustomType
    ;

INT : ('0' | [1-9][0-9]*);
ID : LETTER (LETTER | [0-9])*;
LETTER: [a-zA-Z_$];