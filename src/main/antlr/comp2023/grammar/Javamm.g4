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
     : ('public')? type methodName=ID '(' ( paramDeclaration (',' paramDeclaration)*)? ')' '{' (varDeclaration)* ( statement )* 'return' expression ';' '}'
     | ('public')? 'static' 'void' methodName='main' '(' paramType=type '[' ']' paramName=ID ')' '{' (varDeclaration)* (statement)* '}'
     ;

program
    : (importDeclaration)* declaration=classDeclaration <EOF> ;

statement
    : '{' ( statement )* '}' #Scope
    | 'if' '(' condition=expression ')' statement 'else' statement #If
    | 'while' '(' condition=expression ')' statement #WhileLoop
    | expression ';' #SimpleStatement
    | varName=ID '=' assigned=expression ';' #SimpleAssignment // TODO CHECK IF EXISTS
    | varName=ID '[' expression ']' '=' assigned=expression ';' #ArrayAssignment // TODO CHECK IF EXISTS
    ;

expression
    :
    classNameExp=expression '.' methodName=ID '('  (expression ( ',' expression)*)? ')' #MethodCall // TODO Check if class name exists and has a 'methodName' method - return type is method's return type
    | array=expression '.' 'length' #ArrayLength // TODO Check if expression is an array - return type is integer
    | array=expression '[' index=expression ']' #ArrayAccess // TODO check if array is an array and if index is an integer - return type is array's type (integer)
    | '(' expression ')' #Parenthesis // TODO return type depends on nested expression
    | '!' bool=expression #Negation // TODO check if bool is a boolean or BoolOp - return type is Boolean
    | arg1=expression op=('*' | '/' | '%') arg2=expression #ArithmeticBinaryOp // TODO check if arg1 and arg2 are integers - return type is integer
    | arg1=expression op=('+' | '-') arg2=expression #ArithmeticBinaryOp // TODO check if arg1 and arg2 are integers - return type is integer
    | arg1=expression op=('<' | '>' | '<=' | '>=') arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are integers - return type is boolean
    | arg1=expression op=('==' | '!=') arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are of same type - return type is boolean
    | arg1=expression op='&&' arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are booleans - return type is boolean
    | arg1=expression op='||' arg2=expression #BoolBinaryOp // TODO check if arg1 and arg2 are booleans - return type ias boolean
    | 'new' 'int' '[' index=expression ']' #IntArrayInstantiation // TODO check if index is integer - return type is integer array
    | 'new' className=ID '(' ')' #Instantiation // TODO check if className is in scope (check imports or main className) - return type is className
    | value=INT #Integer // TODO return type is integer
    | value=BOOL #Boolean // TODO return type is boolean
    | value=ID #Identifier // TODO check if value exists - return type is value's type.
    | 'this' #ClassAccess // TODO return type is main className
    ;

importDeclaration
    : 'import' root=modulePathFragment ('.' last=modulePathFragment)* ';'
    ;

varDeclaration
    : type name=ID ';'
    ;

paramDeclaration
    : type paramName=ID
    ;

type
    : 'int''['']' #IntArray
    | 'boolean' #Bool
    | 'int' #Int
    | typeName=ID #CustomType
    ;

modulePathFragment
    :
    pathFragment=ID
    ;

INT : ('0' | [1-9][0-9]*);
ID : LETTER (LETTER | [0-9])*;
LETTER: [a-zA-Z_$];