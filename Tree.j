; This is automatically generated by the Tiger compiler.
; Do NOT modify!

.class public Tree
.super java/lang/Object

.field public left LTree;
.field public right LTree;
.field public key I
.field public has_left B
.field public has_right B
.field public my_null LTree;
.method public <init>()V
    aload 0
    invokespecial java/lang/Object/<init>()V
    return
.end method


.method public Init(I)B
.limit stack 4096
.limit locals 3
    aload 0
    iload 1
    putfield Tree/key I
    aload 0
    ldc 0
    putfield Tree/has_left B
    aload 0
    ldc 0
    putfield Tree/has_right B
    ldc 1
    ireturn
.end method
.method public SetRight(LTree;)B
.limit stack 4096
.limit locals 3
    aload 0
    aload 1
    putfield Tree/right LTree;
    ldc 1
    ireturn
.end method
.method public SetLeft(LTree;)B
.limit stack 4096
.limit locals 3
    aload 0
    aload 1
    putfield Tree/left LTree;
    ldc 1
    ireturn
.end method
.method public GetRight()LTree;
.limit stack 4096
.limit locals 2
    aload 0
    getfield Tree/right LTree;
    areturn
.end method
.method public GetLeft()LTree;
.limit stack 4096
.limit locals 2
    aload 0
    getfield Tree/left LTree;
    areturn
.end method
.method public GetKey()I
.limit stack 4096
.limit locals 2
    aload 0
    getfield Tree/key I
    ireturn
.end method
.method public SetKey(I)B
.limit stack 4096
.limit locals 3
    aload 0
    iload 1
    putfield Tree/key I
    ldc 1
    ireturn
.end method
.method public GetHas_Right()B
.limit stack 4096
.limit locals 2
    aload 0
    getfield Tree/has_right B
    ireturn
.end method
.method public GetHas_Left()B
.limit stack 4096
.limit locals 2
    aload 0
    getfield Tree/has_left B
    ireturn
.end method
.method public SetHas_Left(B)B
.limit stack 4096
.limit locals 3
    aload 0
    iload 1
    putfield Tree/has_left B
    ldc 1
    ireturn
.end method
.method public SetHas_Right(B)B
.limit stack 4096
.limit locals 3
    aload 0
    iload 1
    putfield Tree/has_right B
    ldc 1
    ireturn
.end method
.method public Compare(II)B
.limit stack 4096
.limit locals 6
    ldc 0
    istore 3
    iload 2
    ldc 1
    iadd
    istore 4
    iload 1
    iload 2
    if_icmplt L_3
L_4:
    ldc 0
    goto L_5
L_3:
    ldc 1
    goto L_5
L_5:
    ifne L_0
L_1:
    iload 1
    iload 4
    if_icmplt L_12
L_13:
    ldc 0
    goto L_14
L_12:
    ldc 1
    goto L_14
L_14:
    ifne L_9
L_10:
    ldc 1
    goto L_11
L_9:
    ldc 0
    goto L_11
L_11:
    ifne L_6
L_7:
    ldc 1
    istore 3
    goto L_8
L_6:
    ldc 0
    istore 3
    goto L_8
L_8:
    goto L_2
L_0:
    ldc 0
    istore 3
    goto L_2
L_2:
    iload 3
    ireturn
.end method
.method public Insert(I)B
.limit stack 4096
.limit locals 8
    new Tree
    dup
    invokespecial Tree/<init>()V
    astore 2
    aload 2
    iload 1
    invokevirtual Tree/Init(I)B
    istore 3
    aload 0
    astore 4
    ldc 1
    istore 5
    goto L_16
L_15:
    aload 4
    invokevirtual Tree/GetKey()I
    istore 6
    iload 1
    iload 6
    if_icmplt L_20
L_21:
    ldc 0
    goto L_22
L_20:
    ldc 1
    goto L_22
L_22:
    ifne L_17
L_18:
    aload 4
    invokevirtual Tree/GetHas_Right()B
    ifne L_23
L_24:
    ldc 0
    istore 5
    aload 4
    ldc 1
    invokevirtual Tree/SetHas_Right(B)B
    istore 3
    aload 4
    aload 2
    invokevirtual Tree/SetRight(LTree;)B
    istore 3
    goto L_25
L_23:
    aload 4
    invokevirtual Tree/GetRight()LTree;
    astore 4
    goto L_25
L_25:
    goto L_19
L_17:
    aload 4
    invokevirtual Tree/GetHas_Left()B
    ifne L_26
L_27:
    ldc 0
    istore 5
    aload 4
    ldc 1
    invokevirtual Tree/SetHas_Left(B)B
    istore 3
    aload 4
    aload 2
    invokevirtual Tree/SetLeft(LTree;)B
    istore 3
    goto L_28
L_26:
    aload 4
    invokevirtual Tree/GetLeft()LTree;
    astore 4
    goto L_28
L_28:
    goto L_19
L_19:
L_16:
    iload 5
    ifne L_15
    ldc 1
    ireturn
.end method
.method public Delete(I)B
.limit stack 4096
.limit locals 10
    aload 0
    astore 2
    aload 0
    astore 3
    ldc 1
    istore 4
    ldc 0
    istore 5
    ldc 1
    istore 7
    goto L_30
L_29:
    aload 2
    invokevirtual Tree/GetKey()I
    istore 8
    iload 1
    iload 8
    if_icmplt L_34
L_35:
    ldc 0
    goto L_36
L_34:
    ldc 1
    goto L_36
L_36:
    ifne L_31
L_32:
    iload 8
    iload 1
    if_icmplt L_40
L_41:
    ldc 0
    goto L_42
L_40:
    ldc 1
    goto L_42
L_42:
    ifne L_37
L_38:
    iload 7
    ifne L_43
L_44:
    aload 0
    aload 3
    aload 2
    invokevirtual Tree/Remove(LTree;LTree;)B
    istore 6
    goto L_45
L_43:
    aload 2
    invokevirtual Tree/GetHas_Right()B
    ifne L_49
L_50:
    ldc 1
    goto L_51
L_49:
    ldc 0
    goto L_51
L_51:
    aload 2
    invokevirtual Tree/GetHas_Left()B
    ifne L_52
L_53:
    ldc 1
    goto L_54
L_52:
    ldc 0
    goto L_54
L_54:
    iand
    ifne L_46
L_47:
    aload 0
    aload 3
    aload 2
    invokevirtual Tree/Remove(LTree;LTree;)B
    istore 6
    goto L_48
L_46:
    ldc 1
    istore 6
    goto L_48
L_48:
    goto L_45
L_45:
    ldc 1
    istore 5
    ldc 0
    istore 4
    goto L_39
L_37:
    aload 2
    invokevirtual Tree/GetHas_Right()B
    ifne L_55
L_56:
    ldc 0
    istore 4
    goto L_57
L_55:
    aload 2
    astore 3
    aload 2
    invokevirtual Tree/GetRight()LTree;
    astore 2
    goto L_57
L_57:
    goto L_39
L_39:
    goto L_33
L_31:
    aload 2
    invokevirtual Tree/GetHas_Left()B
    ifne L_58
L_59:
    ldc 0
    istore 4
    goto L_60
L_58:
    aload 2
    astore 3
    aload 2
    invokevirtual Tree/GetLeft()LTree;
    astore 2
    goto L_60
L_60:
    goto L_33
L_33:
    ldc 0
    istore 7
L_30:
    iload 4
    ifne L_29
    iload 5
    ireturn
.end method
.method public Remove(LTree;LTree;)B
.limit stack 4096
.limit locals 7
    aload 2
    invokevirtual Tree/GetHas_Left()B
    ifne L_61
L_62:
    aload 2
    invokevirtual Tree/GetHas_Right()B
    ifne L_64
L_65:
    aload 2
    invokevirtual Tree/GetKey()I
    istore 4
    aload 1
    invokevirtual Tree/GetLeft()LTree;
    invokevirtual Tree/GetKey()I
    istore 5
    aload 0
    iload 4
    iload 5
    invokevirtual Tree/Compare(II)B
    ifne L_67
L_68:
    aload 1
    aload 0
    getfield Tree/my_null LTree;
    invokevirtual Tree/SetRight(LTree;)B
    istore 3
    aload 1
    ldc 0
    invokevirtual Tree/SetHas_Right(B)B
    istore 3
    goto L_69
L_67:
    aload 1
    aload 0
    getfield Tree/my_null LTree;
    invokevirtual Tree/SetLeft(LTree;)B
    istore 3
    aload 1
    ldc 0
    invokevirtual Tree/SetHas_Left(B)B
    istore 3
    goto L_69
L_69:
    goto L_66
L_64:
    aload 0
    aload 1
    aload 2
    invokevirtual Tree/RemoveRight(LTree;LTree;)B
    istore 3
    goto L_66
L_66:
    goto L_63
L_61:
    aload 0
    aload 1
    aload 2
    invokevirtual Tree/RemoveLeft(LTree;LTree;)B
    istore 3
    goto L_63
L_63:
    ldc 1
    ireturn
.end method
.method public RemoveRight(LTree;LTree;)B
.limit stack 4096
.limit locals 5
    goto L_71
L_70:
    aload 2
    aload 2
    invokevirtual Tree/GetRight()LTree;
    invokevirtual Tree/GetKey()I
    invokevirtual Tree/SetKey(I)B
    istore 3
    aload 2
    astore 1
    aload 2
    invokevirtual Tree/GetRight()LTree;
    astore 2
L_71:
    aload 2
    invokevirtual Tree/GetHas_Right()B
    ifne L_70
    aload 1
    aload 0
    getfield Tree/my_null LTree;
    invokevirtual Tree/SetRight(LTree;)B
    istore 3
    aload 1
    ldc 0
    invokevirtual Tree/SetHas_Right(B)B
    istore 3
    ldc 1
    ireturn
.end method
.method public RemoveLeft(LTree;LTree;)B
.limit stack 4096
.limit locals 5
    goto L_73
L_72:
    aload 2
    aload 2
    invokevirtual Tree/GetLeft()LTree;
    invokevirtual Tree/GetKey()I
    invokevirtual Tree/SetKey(I)B
    istore 3
    aload 2
    astore 1
    aload 2
    invokevirtual Tree/GetLeft()LTree;
    astore 2
L_73:
    aload 2
    invokevirtual Tree/GetHas_Left()B
    ifne L_72
    aload 1
    aload 0
    getfield Tree/my_null LTree;
    invokevirtual Tree/SetLeft(LTree;)B
    istore 3
    aload 1
    ldc 0
    invokevirtual Tree/SetHas_Left(B)B
    istore 3
    ldc 1
    ireturn
.end method
.method public Search(I)I
.limit stack 4096
.limit locals 7
    aload 0
    astore 2
    ldc 1
    istore 4
    ldc 0
    istore 3
    goto L_75
L_74:
    aload 2
    invokevirtual Tree/GetKey()I
    istore 5
    iload 1
    iload 5
    if_icmplt L_79
L_80:
    ldc 0
    goto L_81
L_79:
    ldc 1
    goto L_81
L_81:
    ifne L_76
L_77:
    iload 5
    iload 1
    if_icmplt L_85
L_86:
    ldc 0
    goto L_87
L_85:
    ldc 1
    goto L_87
L_87:
    ifne L_82
L_83:
    ldc 1
    istore 3
    ldc 0
    istore 4
    goto L_84
L_82:
    aload 2
    invokevirtual Tree/GetHas_Right()B
    ifne L_88
L_89:
    ldc 0
    istore 4
    goto L_90
L_88:
    aload 2
    invokevirtual Tree/GetRight()LTree;
    astore 2
    goto L_90
L_90:
    goto L_84
L_84:
    goto L_78
L_76:
    aload 2
    invokevirtual Tree/GetHas_Left()B
    ifne L_91
L_92:
    ldc 0
    istore 4
    goto L_93
L_91:
    aload 2
    invokevirtual Tree/GetLeft()LTree;
    astore 2
    goto L_93
L_93:
    goto L_78
L_78:
L_75:
    iload 4
    ifne L_74
    iload 3
    ireturn
.end method
.method public Print()B
.limit stack 4096
.limit locals 4
    aload 0
    astore 2
    aload 0
    aload 2
    invokevirtual Tree/RecPrint(LTree;)B
    istore 1
    ldc 1
    ireturn
.end method
.method public RecPrint(LTree;)B
.limit stack 4096
.limit locals 4
    aload 1
    invokevirtual Tree/GetHas_Left()B
    ifne L_94
L_95:
    ldc 1
    istore 2
    goto L_96
L_94:
    aload 0
    aload 1
    invokevirtual Tree/GetLeft()LTree;
    invokevirtual Tree/RecPrint(LTree;)B
    istore 2
    goto L_96
L_96:
    aload 1
    invokevirtual Tree/GetKey()I
    getstatic java/lang/System/out Ljava/io/PrintStream;
    swap
    invokevirtual java/io/PrintStream/println(I)V
    aload 1
    invokevirtual Tree/GetHas_Right()B
    ifne L_97
L_98:
    ldc 1
    istore 2
    goto L_99
L_97:
    aload 0
    aload 1
    invokevirtual Tree/GetRight()LTree;
    invokevirtual Tree/RecPrint(LTree;)B
    istore 2
    goto L_99
L_99:
    ldc 1
    ireturn
.end method
.method public accept(LVisitor;)I
.limit stack 4096
.limit locals 4
    ldc 333
    getstatic java/lang/System/out Ljava/io/PrintStream;
    swap
    invokevirtual java/io/PrintStream/println(I)V
    aload 1
    aload 0
    invokevirtual Visitor/visit(LTree;)I
    istore 2
    ldc 0
    ireturn
.end method