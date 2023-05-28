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

.method public testArgumentArray([I)V
	.limit stack 3
	.limit locals 2
	aload_1
	iconst_0
	iconst_1
	iastore
	aload_1
	iconst_1
	iconst_2
	iastore
	aload_1
	iconst_2
	iconst_3
	iastore
	return
.end method

.method public testCallArgumentArray()V
	.limit stack 2
	.limit locals 4
	iconst_3
	newarray int
	astore_1
	aload_1
	astore_2
	aload_0
	aload_2
	invokevirtual myClass/testArgumentArray([I)V
; ERROR: getStore()
	return
.end method
