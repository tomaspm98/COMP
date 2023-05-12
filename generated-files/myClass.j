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
	.limit stack 99
	.limit locals 1
	aload_0
	invokespecial importedClass/<init>()V
	return
.end method

.method public static method1([Ljava/lang/String;[ILmadeUp;I)V
	.limit stack 99
	.limit locals 24
	aload 4
	invokevirtual Something/more/ioPlus/randomMethod()V
	aload 4
	aload 5
	invokevirtual Something/more/ioPlus/otherRandomMethod([I)V
	aload 4
	aload 6
	invokevirtual Something/more/ioPlus/anotherRandomMethod(LmadeUp;)V
	aload 4
	aload_0
	aload_1
	aload_2
	aload 6
	aload 4
	invokevirtual Something/more/ioPlus/yetAnotherRandomMethod([Ljava/lang/String;[ILmadeUp;LmadeUp;LSomething/more/ioPlus;)V
	aload_0
	getfield myClass/aStr Ljava/lang/String;
	astore 7
	aload_0
	getfield myClass/strArr [Ljava/lang/String;
	astore 8
	aload_0
	getfield myClass/iArr [I
	astore 9
	aload_0
	getfield myClass/importedType LSomething/more/ioPlus;
	astore 10
	aload 4
	aload 7
	aload 8
	aload 9
	aload 5
	aload 10
	aload 4
	invokevirtual Something/more/ioPlus/fifthRandomMethod(Ljava/lang/String;[Ljava/lang/String;[I[ILSomething/more/ioPlus;LSomething/more/ioPlus;)V
	iconst_2
	istore 11
	aload_0
	getfield myClass/anInt I
	istore 12
	iload 12
	istore 11
	aload_0
	getfield myClass/anInt I
	istore 13
	iload 13
	iconst_2
	iadd
	istore 14
	iload 14
	istore 11
	aload_0
	getfield myClass/anInt I
	istore 15
	iload 11
	iload 15
	iadd
	istore 16
	iload 16
	iconst_2
	iadd
	istore 17
	iload 17
	istore 11
	iload 11
	iconst_2
	imul
	istore 18
	iconst_3
	iload_3
	imul
	istore 19
	iload 18
	iload 19
	iadd
	istore 20
	iload 20
	istore 11
	iconst_5
	bipush 10
	imul
	istore 21
	iload 21
	iload 11
	iadd
	istore 22
	aload_0
	iload 22
	putfield myClass/anInt I
	iconst_1
	istore_3
	return
.end method

.method public get()I
	.limit stack 99
	.limit locals 3
	aload_0
	getfield myClass/a I
	istore_1
	iload_1
	ireturn
.end method

.method public put(I)I
	.limit stack 99
	.limit locals 3
	aload_0
	iload_1
	putfield myClass/a I
	iconst_1
	ireturn
.end method

.method public m1()I
	.limit stack 99
	.limit locals 6
	aload_0
	iconst_2
	putfield myClass/a I
	aload_0
	invokevirtual myClass/pt.up.fe.comp2023.node.information.Method@48ae9b55()I
	istore_1
	iload_1
	invokestatic io/println(I)V
	new myClass
	astore_2
	aload_2
	invokespecial myClass/<init>()V
	aload_2
	invokevirtual myClass/get()I
	istore_3
	iload_3
	invokestatic io/println(I)V
	aload_2
	iconst_2
	invokevirtual myClass/put(I)I
	pop
	aload_2
	invokevirtual myClass/get()I
	istore 4
	iload 4
	invokestatic io/println(I)V
	iconst_3
	ireturn
.end method

.method public static main([Ljava/lang/String;)V
	.limit stack 99
	.limit locals 2
	invokestatic Something/more/ioPlus/printHelloWorld()V
	new myClass
	astore_1
	aload_1
	invokespecial myClass/<init>()V
	aload_1
	invokevirtual myClass/m1()I
	pop
	return
.end method
