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
    classNameExp=expression '.' methodName=ID '('  (expression ( ',' expression)*)? ')' #MethodCall
    | 'this' '.' methodName=ID '('  (expression ( ',' expression)*)? ')' #ThisMethodCall
    | array=expression '.' 'length' #ArrayLength
    | array=expression '[' index=expression ']' #ArrayAccess
    | '(' expression ')' #Parenthesis
    | '!' bool=expression #UnaryBinaryOp
    | arg1=expression op=('*' | '/' | '%') arg2=expression #ArithmeticBinaryOp
    | arg1=expression op=('+' | '-') arg2=expression #ArithmeticBinaryOp
    | arg1=expression op=('<' | '>' | '<=' | '>=') arg2=expression #BoolBinaryOp
    | arg1=expression op=('==' | '!=') arg2=expression #BoolBinaryOp
    | arg1=expression op='&&' arg2=expression #BoolBinaryOp
    | arg1=expression op='||' arg2=expression #BoolBinaryOp
    | 'new' typeName='int' '[' size=expression ']' #ArrayInstantiation
    | 'new' typeName='boolean' '[' size=expression ']' #ArrayInstantiation
    | 'new' typeName='String' '[' size=expression ']' #ArrayInstantiation
    | 'new' typeName=ID '[' size=expression ']' #ArrayInstantiation
    | 'new' name=ID '(' ')' #Instantiation
    | value=INT #Integer
    | value=BOOL #Boolean
    | value=ID #Identifier
    | classField #ExplicitClassFieldAccess
    | 'this' #ClassAccess
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
