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

.method public get()I
	.limit stack 1
	.limit locals 3
	aload_0
	getfield myClass/a I
	istore_1
	iload_1
	ireturn
.end method

.method public put(I)I
	.limit stack 2
	.limit locals 3
	aload_0
	iload_1
	putfield myClass/a I
	iconst_1
	ireturn
.end method

.method public m1()I
	.limit stack 2
	.limit locals 6
	aload_0
	iconst_2
	putfield myClass/a I
	aload_0
	invokevirtual myClass/get()I
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

.method public m2(I[ILmadeUp;)V
	.limit stack 22
	.limit locals 70
	aload 4
	iconst_0
	iconst_1
	istore 4
	aload 4
	iconst_1
	iload 5
	istore 4
	aload 4
	iconst_2
	iload_1
	istore 4
	aload_2
	iconst_0
	iaload
	istore 6
	aload 4
	iconst_3
	iload 6
	istore 4
	aload 8
	invokevirtual madeUp/getInt()I
	istore 7
	aload 4
	iconst_4
	iload 7
	istore 4
	aload_3
	invokevirtual madeUp/getInt()I
	istore 9
	aload 4
	iconst_5
	iload 9
	istore 4
	aload 8
	iload 5
	aload 8
	aload 4
	aload_3
	invokevirtual madeUp/getInt(ILmadeUp;[ILmadeUp;)I
	istore 10
	aload 4
	bipush 6
	iload 10
	istore 4
	aload_0
	invokevirtual myClass/m1()I
	istore 11
	aload_0
	invokevirtual myClass/put()I
	istore 12
	aload_0
	invokevirtual myClass/put()I
	istore 13
	aload_0
	invokevirtual myClass/put()I
	istore 14
	aload_3
	iconst_1
	iload 11
	iload 12
	iload 13
	iload 14
	invokevirtual madeUp/getInt(IIIII)I
	istore 15
	aload 4
	bipush 7
	iload 15
	istore 4
	aload_2
	iconst_0
	iconst_1
	iastore
	aload_2
	iconst_1
	iload 5
	iastore
	aload_2
	iconst_2
	iload_1
	iastore
	aload_2
	iconst_0
	iaload
	istore 16
	aload_2
	iconst_3
	iload 16
	iastore
	aload 8
	invokevirtual madeUp/getInt()I
	istore 17
	aload_2
	iconst_4
	iload 17
	iastore
	aload_3
	invokevirtual madeUp/getInt()I
	istore 18
	aload_2
	iconst_5
	iload 18
	iastore
	aload 8
	iload 5
	aload 8
	aload 4
	aload_3
	invokevirtual madeUp/getInt(ILmadeUp;[ILmadeUp;)I
	istore 19
	aload_2
	bipush 6
	iload 19
	iastore
	aload_0
	invokevirtual myClass/m1()I
	istore 20
	aload_0
	invokevirtual myClass/put()I
	istore 21
	aload_0
	invokevirtual myClass/put()I
	istore 22
	aload_0
	invokevirtual myClass/put()I
	istore 23
	aload_3
	iconst_1
	iload 20
	iload 21
	iload 22
	iload 23
	invokevirtual madeUp/getInt(IIIII)I
	istore 24
	aload_2
	bipush 7
	iload 24
	iastore
	aload_0
	getfield myClass/iArr [I
	astore 25
	aload 25
	iconst_0
	iconst_1
	iastore
	aload_0
	getfield myClass/iArr [I
	astore 26
	aload 26
	iconst_1
	iload 5
	iastore
	aload_0
	getfield myClass/iArr [I
	astore 27
	aload 27
	iconst_2
	iload_1
	iastore
	aload_2
	iconst_0
	iaload
	istore 28
	aload_0
	getfield myClass/iArr [I
	astore 29
	aload 29
	iconst_3
	iload 28
	iastore
	aload 8
	invokevirtual madeUp/getInt()I
	istore 30
	aload_0
	getfield myClass/iArr [I
	astore 31
	aload 31
	iconst_4
	iload 30
	iastore
	aload_3
	invokevirtual madeUp/getInt()I
	istore 32
	aload_0
	getfield myClass/iArr [I
	astore 33
	aload 33
	iconst_5
	iload 32
	iastore
	aload 8
	iload 5
	aload 8
	aload 4
	aload_3
	invokevirtual madeUp/getInt(ILmadeUp;[ILmadeUp;)I
	istore 34
	aload_0
	getfield myClass/iArr [I
	astore 35
	aload 35
	bipush 6
	iload 34
	iastore
	aload_0
	invokevirtual myClass/m1()I
	istore 36
	aload_0
	invokevirtual myClass/put()I
	istore 37
	aload_0
	invokevirtual myClass/put()I
	istore 38
	aload_0
	invokevirtual myClass/put()I
	istore 39
	aload_3
	iconst_1
	iload 36
	iload 37
	iload 38
	iload 39
	invokevirtual madeUp/getInt(IIIII)I
	istore 40
	aload_0
	getfield myClass/iArr [I
	astore 41
	aload 41
	bipush 7
	iload 40
	iastore
	aload 4
	iconst_0
	iaload
	istore 42
	aload 4
	iload 42
	iaload
	istore 43
	iload 43
	istore 5
	aload 4
	iconst_0
	iaload
	istore 44
	aload 4
	iconst_0
	iaload
	istore 45
	aload 4
	iload 45
	iaload
	istore 46
	aload 4
	iload 44
	iload 46
	istore 4
	aload_0
	getfield myClass/iArr [I
	astore 47
	aload 4
	iconst_0
	iaload
	istore 48
	aload 47
	iload 48
	iaload
	istore 49
	aload_0
	getfield myClass/iArr [I
	astore 50
	aload_0
	getfield myClass/iArr [I
	astore 51
	aload 4
	iconst_0
	iaload
	istore 52
	aload 51
	iload 52
	iaload
	istore 53
	aload 50
	iload 53
	iaload
	istore 54
	aload_0
	getfield myClass/iArr [I
	astore 55
	aload 55
	iload 49
	iload 54
	iastore
	aload_0
	getfield myClass/iArr [I
	astore 56
	aload_3
	invokevirtual madeUp/getInt()I
	istore 57
	aload 4
	iload 57
	iaload
	istore 58
	aload 56
	iload 58
	iaload
	istore 59
	aload_0
	getfield myClass/iArr [I
	astore 60
	aload_0
	getfield myClass/iArr [I
	astore 61
	aload_0
	getfield myClass/madeUpType LmadeUp;
	astore 62
	aload_0
	getfield myClass/iArr [I
	astore 63
	aload 63
	iconst_0
	iaload
	istore 64
	aload 62
	iload 64
	invokevirtual madeUp/getInt(I)I
	istore 65
	aload 61
	iload 65
	iaload
	istore 66
	aload 60
	iload 66
	iaload
	istore 67
	aload_0
	getfield myClass/iArr [I
	astore 68
	aload 68
	iload 59
	iload 67
	iastore
	return
.end method
