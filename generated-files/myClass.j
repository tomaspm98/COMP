.class myClass
.super another/importedClass
.field private a I
.field private anInt I
.field private aBoolean Z
.field private aStr Ljava/lang/String;
.field private strArr [Ljava/lang/String;
.field private iArr [I
.field private importedType LSomething/more/ioPlus;
.field private madeUpType LmadeUp;

.method <init>()V
	.limit stack 1
	.limit locals 1
	aload_0
	invokespecial importedClass/<init>()V
	return
.end method

.method public testIfs()V
	.limit stack 2
	.limit locals 8
WHILE0:
	iload_2
	iconst_5
	if_icmplt TRUE0
	iconst_0
	goto NEXT0
TRUE0:
	iconst_1
NEXT0:
	istore_1
	iload_1
	ifne ENDWHILE0
	iload_2
	iconst_1
	iadd
	istore_3
	iload_3
	istore_2
	iload 5
	istore 4
	iload 4
	iload 4
	iand
	istore 6
	iload 6
	istore 4
	goto WHILE0
ENDWHILE0:
	iload_2
	iconst_1
	iadd
	istore 7
	iload 7
	istore_2
	return
.end method
