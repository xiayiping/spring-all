grammar MathGrammer;
//
//completeExpression: expression <EOF>;
//
//expression: additiveExpression;
//
//additiveExpression: multiplicativeExpression (('+' | '-') multiplicativeExpression)*;
//
//multiplicativeExpression: primaryExpression ('*' primaryExpression)*;
//
//primaryExpression: INT | '(' expression ')';

//expression: INT | primaryExpression;

expression: additiveExpression | primaryExpression;

additiveExpression: primaryExpression (('+' | '-') primaryExpression)* ;

//multiplicativeExpression: primaryExpression (('*' | '/') primaryExpression)* ;

primaryExpression: INT | '(' expression ')' ;

INT: [0-9]+ ;