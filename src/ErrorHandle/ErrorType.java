package ErrorHandle;

public enum ErrorType {
    IllegalSymError, // 非法符号 a
    RedefineError, // 名字重定义 b
    UndefineError, // 名字未定义 c
    ParameterNumError, // 函数参数个数不匹配 d
    ParameterTypeError, // 函数参数类型不匹配 e
    UnmatchedReturnError, // void函数出现不匹配的return f
    LackReturnError, // 有返回值的函数缺少结尾的return g
    ChangeConstError, // 修改常量值 h
    LackSemicolonError, // 缺少分号 i
    LackRParentError, // 缺少右小括号 j
    LackRBracketError, // 缺少右中括号 k
    UnmatchedFormatCharError, // 格式字符数与表达式数不匹配 l
    LoopStmtError; // 循环语句之外出现break和continue m
    public String toString(){
        switch (this){
            case IllegalSymError -> {
                return "a";
            }
            case RedefineError -> {
                return "b";
            }
            case UndefineError -> {
                return "c";
            }
            case ParameterNumError -> {
                return "d";
            }
            case ParameterTypeError -> {
                return "e";
            }
            case UnmatchedReturnError -> {
                return "f";
            }
            case LackReturnError -> {
                return "g";
            }
            case ChangeConstError -> {
                return "h";
            }
            case LackSemicolonError -> {
                return "i";
            }
            case LackRParentError -> {
                return "j";
            }
            case LackRBracketError -> {
                return "k";
            }
            case UnmatchedFormatCharError -> {
                return "l";
            }
            case LoopStmtError -> {
                return "m";
            }
            default -> {
                return null;
            }
        }
    }
}
