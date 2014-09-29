/*
 * Copyright (C) 2014 The MANO Project. All rights reserved. 
 * 
 * See more http://mano.diosay.com/
 * 
 */
package mano.expressions;
//http://msdn.microsoft.com/zh-cn/library/bb361179.aspx

/**
 * 表达式类型。
 *
 * @author jun <jun@diosay.com>
 */
public enum ExpressionType {

    /**
     * 加法运算，如 a + b，针对数值操作数，不进行溢出检查。
     */
    Add("Add"),
    /**
     * 加法复合赋值运算，如 (a += b)，针对数值操作数，不进行溢出检查。
     */
    AddAssign("AddAssign"),
    /**
     * 按位或逻辑 AND 运算，如 java 中的 (a && b)。
     */
    And("And"),
    /**
     * 条件 AND 运算，它仅在第一个操作数的计算结果为 true 时才计算第二个操作数。 它与 C# 中的 (a && b) 和 Visual
     * Basic 中的 (a AndAlso b) 对应。
     */
    AndAlso("AndAlso"),
    /**
     * 按位或逻辑 AND 复合赋值运算，如 C# 中的 (a &= b)。
     */
    AndAssign("AndAssign"),
    /**
     * 一维数组中的索引运算，如 C# 中的 array[index] 或 Visual Basic 中的 array(index)。
     */
    ArrayIndex("ArrayIndex"),
    /**
     * 获取一维数组长度的运算，如 array.Length。
     */
    ArrayLength("ArrayLength"),
    /**
     * 赋值运算，如 (a = b)。
     */
    Assign("Assign"),
    /**
     * 表达式块。
     */
    Block("Block"),
    /**
     * 方法调用，如在 obj.sampleMethod() 表达式中。
     */
    Call("Call"),
    /**
     * 表示 null 合并运算的节点，如 C# 中的 (a ?? b) 或 Visual Basic 中的 If(a, b)。
     */
    Coalesce("Coalesce"),
    /**
     * 条件运算，如 C# 中的 a > b ? a : b 或 Visual Basic 中的 If(a > b, a, b)。
     */
    Conditional("Conditional"),
    /**
     * 一个常量值。
     */
    Constant("Constant"),
    /**
     * 强制转换或转换运算，如 C#中的 (SampleType)obj 或 Visual Basic 中的 CType(obj,
     * SampleType)。 对于数值转换，如果转换后的值对于目标类型来说太大，这不会引发异常。
     */
    Convert("Convert"),
    /**
     * 调试信息。
     */
    DebugInfo("DebugInfo"),
    /**
     * 一元递减运算，如 C# 和 Visual Basic 中的 (a - 1)。 不应就地修改 a 对象。
     */
    Decrement("Decrement"),
    /**
     * 默认值。
     */
    Default("Default"),
    /**
     * 除法运算，如 (a / b)，针对数值操作数。
     */
    Divide("Divide"),
    /**
     * 除法复合赋值运算，如 (a /= b)，针对数值操作数。
     */
    DivideAssign("DivideAssign"),
    /**
     * 动态操作。
     */
    Dynamic("Dynamic"),
    /**
     * 表示相等比较的节点，如 C# 中的 (a == b) 或 Visual Basic 中的 (a = b)。
     */
    Equal("Equal"),
    /**
     * 按位或逻辑 XOR 运算，如 C# 中的 (a ^ b) 或 Visual Basic 中的 (a Xor b)。
     */
    ExclusiveOr("ExclusiveOr"),
    /**
     * 按位或逻辑 XOR 复合赋值运算，如 C# 中的 (a ^= b)。
     */
    ExclusiveOrAssign("ExclusiveOrAssign"),
    /**
     * 扩展表达式。
     */
    Extension("Extension"),
    /**
     * “跳转”表达式，如 C# 中的 goto Label 或 Visual Basic 中的 GoTo Label。
     */
    Goto("Goto"),
    /**
     * “大于”比较，如 (a > b)。
     */
    GreaterThan("GreaterThan"),
    /**
     * “大于或等于”比较，如 (a >= b)。
     */
    GreaterThanOrEqual("GreaterThanOrEqual"),
    /**
     * 一元递增运算，如 C# 和 Visual Basic 中的 (a + 1)。 不应就地修改 a 对象。
     */
    Increment("Increment"),
    /**
     * 索引运算或访问使用参数的属性的运算。
     */
    Index("Index"),
    /**
     * 调用委托或 lambda 表达式的运算，如 sampleDelegate.Invoke()。
     */
    Invoke("Invoke"),
    /**
     * false 条件值。
     */
    IsFalse("IsFalse"),
    /**
     * true 条件值。
     */
    IsTrue("IsTrue"),
    /**
     * 标签。
     */
    Label("Label"),
    /**
     * lambda 表达式，如 C# 中的 a => a + a 或 Visual Basic 中的 Function(a) a + a。
     */
    Lambda("Lambda"),
    /**
     * 按位左移运算，如 (a << b)。
     */
    LeftShift("LeftShift"),
    /**
     * 按位左移复合赋值运算，如 (a <<= b)。
     */
    LeftShiftAssign("LeftShiftAssign"),
    /**
     * “小于”比较，如 (a < b)。
     */
    LessThan("LessThan"),
    /**
     * “小于或等于”比较，如 (a <= b)。
     */
    LessThanOrEqual("LessThanOrEqual"),
    /**
     * 创建新的 IEnumerable 对象并从元素列表中初始化该对象的运算，如 C# 中的 new List<SampleType>(){ a, b,
     * c } 或 Visual Basic 中的 Dim sampleList = { a, b, c }。
     */
    ListInit("ListInit"),
    /**
     * 循环，如 for 或 while。
     */
    Loop("Loop"),
    /**
     * 从字段或属性进行读取的运算，如 obj.SampleProperty。
     */
    MemberAccess("MemberAccess"),
    /**
     * 创建新的对象并初始化其一个或多个成员的运算，如 C# 中的 new Point { X = 1, Y = 2 } 或 Visual Basic
     * 中的 New Point With {.X = 1, .Y = 2}。
     */
    MemberInit("MemberInit"),
    /**
     * 算术余数运算，如 C# 中的 (a % b) 或 Visual Basic 中的 (a Mod b)。
     */
    Modulo("Modulo"),
    /**
     * 算术余数复合赋值运算，如 C# 中的 (a %= b)。
     */
    ModuloAssign("ModuloAssign"),
    /**
     * 乘法运算，如 (a * b)，针对数值操作数，不进行溢出检查。
     */
    Multiply("Multiply"),
    /**
     * 乘法复合赋值运算，如 (a *= b)，针对数值操作数，不进行溢出检查。
     */
    MultiplyAssign("MultiplyAssign"),
    /**
     * 算术求反运算，如 (-a)。 不应就地修改 a 对象。
     */
    Negate("Negate"),
    /**
     * 调用构造函数创建新对象的运算，如 new SampleType()。
     */
    New("New"),
    /**
     * 创建新数组（其中每个维度的界限均已指定）的运算，如 C# 中的 new SampleType[dim1, dim2] 或 Visual Basic
     * 中的 New SampleType(dim1, dim2)。
     */
    NewArrayBounds("NewArrayBounds"),
    /**
     * 创建新的一维数组并从元素列表中初始化该数组的运算，如 C# 中的 new SampleType[]{a, b, c} 或 Visual Basic
     * 中的 New SampleType(){a, b, c}。
     */
    NewArrayInit("NewArrayInit"),
    /**
     * 按位求补运算或逻辑求反运算。 在 C# 中，它与整型的 (~a) 和布尔值的 (!a) 等效。 在 Visual Basic 中，它与 (Not
     * a) 等效。 不应就地修改 a 对象。
     */
    Not("Not"),
    /**
     * 不相等比较，如 C# 中的 (a != b) 或 Visual Basic 中的 (a <> b)。
     */
    NotEqual("NotEqual"),
    /**
     * 二进制反码运算，如 C# 中的 (~a)。
     */
    OnesComplement("OnesComplement"),
    /**
     * 按位或逻辑 OR 运算，如 C# 中的 (a | b) 或 Visual Basic 中的 (a Or b)。
     */
    Or("Or"),
    /**
     * 按位或逻辑 OR 复合赋值运算，如 C# 中的 (a |= b)。
     */
    OrAssign("OrAssign"),
    /**
     * 短路条件 OR 运算，如 C# 中的 (a || b) 或 Visual Basic 中的 (a OrElse b)。
     */
    OrElse("OrElse"),
    /**
     * 对在表达式上下文中定义的参数或变量的引用。 有关详细信息，请参阅ParameterExpression。
     */
    Parameter("Parameter"),
    /**
     * 一元后缀递减，如 (a--)。 应就地修改 a 对象。
     */
    PostDecrementAssign("PostDecrementAssign"),
    /**
     * 一元后缀递增，如 (a++)。 应就地修改 a 对象。
     */
    PostIncrementAssign("PostIncrementAssign"),
    /**
     * 对某个数字进行幂运算的数学运算，如 Visual Basic 中的 (a ^ b)。
     */
    Power("Power"),
    /**
     * 对某个数字进行幂运算的复合赋值运算，如 Visual Basic 中的 (a ^= b)。
     */
    PowerAssign("PowerAssign"),
    /**
     * 一元前缀递减，如 (--a)。 应就地修改 a 对象。
     */
    PreDecrementAssign("PreDecrementAssign"),
    /**
     * 一元前缀递增，如 (++a)。 应就地修改 a 对象。
     */
    PreIncrementAssign("PreIncrementAssign"),
    /**
     * 具有类型为 Expression 的常量值的表达式。 Quote 节点可包含对参数的引用，这些参数在该节点表示的表达式的上下文中定义。
     */
    Quote("Quote"),
    /**
     * 按位右移运算，如 (a >> b)。
     */
    RightShift("RightShift"),
    /**
     * 按位右移复合赋值运算，如 (a >>= b)。
     */
    RightShiftAssign("RightShiftAssign"),
    /**
     * 运行时变量的列表。 有关详细信息，请参阅RuntimeVariablesExpression。
     */
    RuntimeVariables("RuntimeVariables"),
    /**
     * 减法运算，如 (a - b)，针对数值操作数，不进行溢出检查。
     */
    Subtract("Subtract"),
    /**
     * 减法复合赋值运算，如 (a -= b)，针对数值操作数，不进行溢出检查。
     */
    SubtractAssign("SubtractAssign"),
    /**
     * 多分支选择运算，如 C# 中的 switch 或 Visual Basic 中的 Select Case。
     */
    Switch("Switch"),
    /**
     * 引发异常的运算，如 throw new Exception()。
     */
    Throw("Throw"),
    /**
     * try-catch 表达式。
     */
    Try("Try"),
    /**
     * 显式引用或装箱转换，其中如果转换失败则提供 null，如 C# 中的 (obj as SampleType) 或 Visual Basic 中的
     * TryCast(obj, SampleType)。
     */
    TypeAs("TypeAs"),
    /**
     * 确切类型测试。
     */
    TypeEqual("TypeEqual"),
    /**
     * 类型测试，如 C# 中的 obj is SampleType 或 Visual Basic 中的 TypeOf obj is
     * SampleType。
     */
    TypeIs("TypeIs"),
    /**
     * 一元加法运算，如 (+a)。 预定义的一元加法运算的结果是操作数的值，但用户定义的实现可以产生特殊结果。
     */
    UnaryPlus("UnaryPlus"),
    /**
     * 取消装箱值类型运算，如 MSIL 中的 unbox 和 unbox.any 指令。
     */
    Unbox("Unbox"),
    /**
     * 读取变量
     */
    VariableAccess(null),
    /**
     * 为运行时变量赋值 
     */
    VariableAssign(null);
    private int value;

    private ExpressionType(String name) {
        this.value = (EXPRESS_TYPEID.ID++);
    }

    private static class EXPRESS_TYPEID {

        public static int ID = 1;
    }
}
