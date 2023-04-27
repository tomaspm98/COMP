.class myClass
.super another/importedClass
.field private anInt I
.field private aBoolean Z
.field private aStr Ljava/lang/String;
.field private strArr [Ljava/lang/String;
.field private iArr [I
.field private importedType LSomething/more/ioPlus;
.field private madeUpType LmadeUp;

.method <init>()V
	.limit stack 99
	.limit locals 1
	aload_0
	invokespecial importedClass/<init>()V
	return
.end method

.method public static method1([Ljava/lang/String;[ILmadeUp;I[Ljava/lang/String;[ILmadeUp;I)V
	.limit stack 99
	.limit locals 17
	invokestatic Something/more/ioPlus/randomMethod()V
	aload 8
	invokestatic Something/more/ioPlus/otherRandomMethod([I)V
	aload 9
	invokestatic Something/more/ioPlus/anotherRandomMethod(LmadeUp;)V
	aload 4
	aload 5
	aload 6
	aload 9
	aload 10
	invokestatic Something/more/ioPlus/yetAnotherRandomMethod([Ljava/lang/String;[ILmadeUp;LmadeUp;LSomething/more/ioPlus;)V
	aload_0
	getfield myClass/aStr Ljava/lang/String;
	astore 11
	aload_0
	getfield myClass/aStr Ljava/lang/String;
	astore 12
	aload_0
	getfield myClass/strArr [Ljava/lang/String;
	astore 13
	aload_0
	getfield myClass/strArr [Ljava/lang/String;
	astore 14
	aload_0
	getfield myClass/iArr [I
	astore 15
	aload_0
	getfield myClass/iArr [I
	astore 16
	aload_0
	getfield myClass/importedType LSomething/more/ioPlus;
	astore 17
	aload_0
	getfield myClass/importedType LSomething/more/ioPlus;
	astore 18
	aload 12
	aload 14
	aload 16
	aload 8
	aload 18
	aload 10
	invokestatic Something/more/ioPlus/fifthRandomMethod(Ljava/lang/String;[Ljava/lang/String;[I[ILSomething/more/ioPlus;LSomething/more/ioPlus;)V
	return
.end method

.method public static main([Ljava/lang/String;[Ljava/lang/String;)V
	.limit stack 99
	.limit locals 2
	invokestatic Something/more/ioPlus/printHelloWorld()V
	return
.end method
