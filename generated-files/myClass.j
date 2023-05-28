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

.method public dummyFunction(I)V
	.limit stack 2
	.limit locals 3
	iload_1
	iconst_1
	iadd
	istore_2
	iload_2
	istore_1
	return
.end method

.method public testOptimization()V
	.limit stack 2
	.limit locals 4
	iconst_3
	istore_1
	iconst_4
	istore_1
	iconst_1
	iconst_2
	iadd
	istore_2
	iconst_1
	iload_2
	iadd
	istore_3
	iload_3
	istore_1
	return
.end method
