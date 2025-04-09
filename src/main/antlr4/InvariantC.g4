grammar InvariantC;

invariant
    : expression EOF
    ;

expression
    : conditionalExpression
    ;

postfixExpression
    : '[' expression ']'          #sqbracket
    | op=('.' | '->') Identifier  #dotarrow
    ;

unaryExpression
    : op=('&' | '*' | '+' | '-' | '~' | '!') castExpression #unary
    | primaryExpression                                     #unarybase
    ;

castExpression
    : primaryExpression (postfixExpression)* #postfix
    | '(' op=typeName ')' castExpression     #cast
    | unaryExpression                        #castbase
    ;

multiplicativeExpression
    : castExpression (op+=('*' | '/' | '%') castExpression)*
    ;

additiveExpression
    : multiplicativeExpression (op+=('+' | '-') multiplicativeExpression)*
    ;

shiftExpression
    : additiveExpression (op+=('<<' | '>>') additiveExpression)*
    ;

relationalExpression
    : shiftExpression (op+=('<' | '>' | '<=' | '>=') shiftExpression)*
    ;

equalityExpression
    : relationalExpression (op+=('==' | '!=') relationalExpression)*
    ;

andExpression
    : equalityExpression (op+='&' equalityExpression)*
    ;

exclusiveOrExpression
    : andExpression (op+='^' andExpression)*
    ;

inclusiveOrExpression
    : exclusiveOrExpression (op+='|' exclusiveOrExpression)*
    ;

logicalAndExpression
    : inclusiveOrExpression (op+='&&' inclusiveOrExpression)*
    ;

logicalOrExpression
    : logicalAndExpression (op+='||' logicalAndExpression)*
    ;

conditionalExpression
    : logicalOrExpression ('?' t_exp=conditionalExpression ':' f_exp=conditionalExpression)?
    ;

typeSpecifier
    : 'void'
    | 'char'
    | 'short'
    | 'int'
    | 'long'
    | 'float'
    | 'double'
    | 'signed'
    | 'unsigned'
    | '_Bool'
    | '_Complex'
    | '__m128'
    | '__m128d'
    | '__m128i'
    | '__extension__' '(' ('__m128' | '__m128d' | '__m128i') ')'
    | structOrUnionSpecifier
    | typedefName
    ;

structOrUnionSpecifier
    : structOrUnion Identifier
    ;

structOrUnion
    : 'struct'
    | 'union'
    ;

specifierQualifierList
    : (typeSpecifier | typeQualifier) specifierQualifierList?
    ;

typeQualifier
    : 'const'
    | 'restrict'
    | 'volatile'
    | '_Atomic'
    ;

pointer
    : (op+=('*' | '^') typeQualifierList?)+ // ^ - Blocks language extension
    ;

typeQualifierList
    : typeQualifier+
    ;

typeName
    : specifierQualifierList abstractDeclarator?
    ;

abstractDeclarator
    : pointer
    ;

typedefName
    : Identifier
    ;

primaryExpression
    : Identifier         #ident
    | Constant           #cons
    | StringLiteral+     #string
    | '(' expression ')' #parens
    ;

Less
    : '<'
    ;

LessEqual
    : '<='
    ;

Greater
    : '>'
    ;

GreaterEqual
    : '>='
    ;

Plus
    : '+'
    ;

Minus
    : '-'
    ;

Star
    : '*'
    ;

AndAnd
    : '&&'
    ;

OrOr
    : '||'
    ;

Not
    : '!'
    ;

Equal
    : '=='
    ;

NotEqual
    : '!='
    ;

Identifier
    : IdentifierNondigit (IdentifierNondigit | Digit)*
    ;

fragment IdentifierNondigit
    : Nondigit
    | UniversalCharacterName
    | '$'
    //|   // other implementation-defined characters...
    ;

fragment Nondigit
    : [a-zA-Z_]
    ;

fragment Digit
    : [0-9]
    ;

fragment UniversalCharacterName
    : '\\u' HexQuad
    | '\\U' HexQuad HexQuad
    ;

fragment HexQuad
    : HexadecimalDigit HexadecimalDigit HexadecimalDigit HexadecimalDigit
    ;

Constant
    : IntegerConstant
    | FloatingConstant
    //|   EnumerationConstant
    | CharacterConstant
    ;

fragment IntegerConstant
    : DecimalConstant IntegerSuffix?
    | OctalConstant IntegerSuffix?
    | HexadecimalConstant IntegerSuffix?
    | BinaryConstant
    ;

fragment BinaryConstant
    : '0' [bB] [0-1]+
    ;

fragment DecimalConstant
    : NonzeroDigit Digit*
    ;

fragment OctalConstant
    : '0' OctalDigit*
    ;

fragment HexadecimalConstant
    : HexadecimalPrefix HexadecimalDigit+
    ;

fragment HexadecimalPrefix
    : '0' [xX]
    ;

fragment NonzeroDigit
    : [1-9]
    ;

fragment OctalDigit
    : [0-7]
    ;

fragment HexadecimalDigit
    : [0-9a-fA-F]
    ;

fragment IntegerSuffix
    : UnsignedSuffix LongSuffix?
    | UnsignedSuffix LongLongSuffix
    | LongSuffix UnsignedSuffix?
    | LongLongSuffix UnsignedSuffix?
    ;

fragment UnsignedSuffix
    : [uU]
    ;

fragment LongSuffix
    : [lL]
    ;

fragment LongLongSuffix
    : 'll'
    | 'LL'
    ;

fragment FloatingConstant
    : DecimalFloatingConstant
    | HexadecimalFloatingConstant
    ;

fragment DecimalFloatingConstant
    : FractionalConstant ExponentPart? FloatingSuffix?
    | DigitSequence ExponentPart FloatingSuffix?
    ;

fragment HexadecimalFloatingConstant
    : HexadecimalPrefix (HexadecimalFractionalConstant | HexadecimalDigitSequence) BinaryExponentPart FloatingSuffix?
    ;

fragment FractionalConstant
    : DigitSequence? '.' DigitSequence
    | DigitSequence '.'
    ;

fragment ExponentPart
    : [eE] Sign? DigitSequence
    ;

fragment Sign
    : [+-]
    ;

DigitSequence
    : Digit+
    ;

fragment HexadecimalFractionalConstant
    : HexadecimalDigitSequence? '.' HexadecimalDigitSequence
    | HexadecimalDigitSequence '.'
    ;

fragment BinaryExponentPart
    : [pP] Sign? DigitSequence
    ;

fragment HexadecimalDigitSequence
    : HexadecimalDigit+
    ;

fragment FloatingSuffix
    : [flFL]
    ;

fragment CharacterConstant
    : '\'' CCharSequence '\''
    | 'L\'' CCharSequence '\''
    | 'u\'' CCharSequence '\''
    | 'U\'' CCharSequence '\''
    ;

fragment CCharSequence
    : CChar+
    ;

fragment CChar
    : ~['\\\r\n]
    | EscapeSequence
    ;

fragment EscapeSequence
    : SimpleEscapeSequence
    | OctalEscapeSequence
    | HexadecimalEscapeSequence
    | UniversalCharacterName
    ;

fragment SimpleEscapeSequence
    : '\\' ['"?abfnrtv\\]
    ;

fragment OctalEscapeSequence
    : '\\' OctalDigit OctalDigit? OctalDigit?
    ;

fragment HexadecimalEscapeSequence
    : '\\x' HexadecimalDigit+
    ;

StringLiteral
    : EncodingPrefix? '"' SCharSequence? '"'
    ;

fragment EncodingPrefix
    : 'u8'
    | 'u'
    | 'U'
    | 'L'
    ;

fragment SCharSequence
    : SChar+
    ;

fragment SChar
    : ~["\\\r\n]
    | EscapeSequence
    | '\\\n'   // Added line
    | '\\\r\n' // Added line
    ;

Whitespace
    : [ \t]+ -> channel(HIDDEN)
    ;

Newline
    : ('\r' '\n'? | '\n') -> channel(HIDDEN)
    ;

