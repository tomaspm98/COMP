grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

WS : [ \t\n\r\f]+ -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
BOOL: ('true' | 'false');

classDeclaration
    : 'class' className=ID ('extends' superClassName=ID)? '{' (fieldDeclaration)* (methodDeclaration)* '}'
    ;

program
    : (importDeclaration)* classDeclaration <EOF> ;

ifTrue
    :
    statement
    ;

elseBlock
    :
    statement
    ;

  whileBlock
    :
    statement
    ;

methodDeclaration
     : (modifier)* methodSymbol '(' ( argument (',' argument)*)? ')' '{' (varDeclaration)* (methodStatement)* 'return' methodReturnExpression ';' '}' #NonVoid
     | (modifier)* voidMethodSymbol '(' ( argument (',' argument)*)? ')' '{' (varDeclaration)* (methodStatement)* '}' #Void
     ;

methodSymbol
    :
    type name=ID
    ;

voidMethodSymbol
    :
    'void' name=ID
    ;

methodStatement
    :
    statement
    ;

methodReturnExpression
    :
    expression
    ;

statement
    : '{' ( statement )* '}' #Scope
    | 'if' '(' condition ')' ifTrue 'else' elseBlock #Conditional
    | 'while' '(' condition ')' whileBlock #Conditional
    | expression ';' #SimpleStatement
    | classField '=' expression ';' #ClassFieldAssignment
    | varName=ID '=' expression ';' #Assignment
    | varName=ID '[' arrayIndex=intExpression ']' '=' expression ';' #ArrayAssignment
    ;

intExpression
    :
    expression
    ;

condition
    :
    expression
    ;

expression
    :
    classNameExp=expression '.' methodName=ID '('  (expression ( ',' expression)*)? ')' #MethodCall // TODO Check if class name exists and has a 'methodName' method - return type is method's return type
    | array=expression '.' 'length' #ArrayLength // TODO Check if expression is an array - return type is integer
    | array=expression '[' index=expression ']' #ArrayAccess // TODO check if array is an array and if index is an integer - return type is array's type (integer)
    | '(' expression ')' #Parenthesis // TODO return type depends on nested expression
    | '!' bool=expression #UnaryBinaryOp // TODO check if bool is a boolean or BoolOp - return type is Boolean
    | arg1=expression op=('*' | '/' | '%') arg2=expression #ArithmeticBinaryOp // TODO check if arg1 and arg2 are integers - return type is integer
    | arg1=expression op=('+' | '-') arg2=expression #ArithmeticBinaryOp // TODO check if arg1 and arg2 are integers - return type is integer
    | arg1=expression op=('<' | '>' | '<=' | '>=') arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are integers - return type is boolean
    | arg1=expression op=('==' | '!=') arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are of same type - return type is boolean
    | arg1=expression op='&&' arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are booleans - return type is boolean
    | arg1=expression op='||' arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are booleans - return type ias boolean
    | 'new' typeName='int' '[' size=expression ']' #ArrayInstantiation
    | 'new' typeName='boolean' '[' size=expression ']' #ArrayInstantiation
    | 'new' typeName='String' '[' size=expression ']' #ArrayInstantiation
    | 'new' typeName=ID '[' size=expression ']' #ArrayInstantiation // TODO check if size is integer - return type is typeName array
    | 'new' name=ID '(' ')' #Instantiation // TODO check if className is in scope (check imports or main className) - return type is className
    | value=INT #Integer // TODO return type is integer
    | value=BOOL #Boolean // TODO return type is boolean
    | value=ID #Identifier // TODO check if value exists - return type is value's type.
    | classField #ExplicitClassFieldAccess
    | 'this' #ClassAccess // TODO return type is main className
    ;

classField
    : 'this' '.' varName=ID
    ;

importDeclaration
    : 'import' root=modulePathFragment ('.' modulePathFragment)* ';'
    ;

fieldDeclaration
    :
    type name=ID ';'
    ;

varDeclaration
    : type name=ID ';'
    ;

argument
    : type name=ID
    ;

type locals[boolean isArray = false]
    :
    | typeName='int' ('['']' {$isArray=true;})?
    | typeName='boolean' ('['']' {$isArray=true;})?
    | typeName='String' ('['']' {$isArray=true;})?
    | typeName=ID ('['']' {$isArray=true;})? // check if typeName != void
    ;

modulePathFragment
    :
    pathFragment=ID
    ;

modifier
    :
    value='public'
    | value='static'
    | value='private'
    ;

ID : LETTER (LETTER | [0-9])*;
LETTER: [a-zA-Z_$];
INT : ('0' | [1-9][0-9]*);
