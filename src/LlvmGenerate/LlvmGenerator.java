package LlvmGenerate;

import LexicalAnalyse.LexicalType;
import LexicalAnalyse.Number;
import LexicalAnalyse.Word;
import LlvmGenerate.Instructions.Calculations.*;
import LlvmGenerate.Definitions.Function;
import LlvmGenerate.Definitions.GlobalDecl;
import LlvmGenerate.Definitions.Parameter;
import LlvmGenerate.Instructions.InsFactory;
import LlvmGenerate.Instructions.Instruction;
import LlvmGenerate.Instructions.Jump.BrIns;
import LlvmGenerate.Instructions.Jump.CallIns;
import LlvmGenerate.Instructions.Memories.AllocaIns;
import LlvmGenerate.Instructions.Memories.GetelementptrIns;
import LlvmGenerate.Instructions.Memories.LoadIns;
import SymbolTable.FuncSymbol;
import SymbolTable.Symbol;
import SymbolTable.SymbolTable;
import SymbolTable.VarSymbol;
import SyntaxAnalyse.SyntaxAnalyzer;
import SyntaxAnalyse.SyntaxTreeNodes.*;

import java.util.ArrayList;

public class LlvmGenerator {
    private SyntaxAnalyzer SA;
    private SymbolTable symTab;
    private ArrayList<BasicBlock> curBasicBlockList;
    private BasicBlock curBasicBlock;
    private LlvmTree out; // 生成的llvm文件
    private InsFactory insFactory;
    private ArrayList<FuncFParam> curFuncFParamList; // 当前函数形参表
    private ArrayList<MiddleVal> curFuncRParamValList; // 当前函数调用的实参值表
    private boolean isInFuncBlockWithParam;
    private boolean isGlobalDecl;
    private MiddleVal middleVal;
    private ArrayList<ArrayList<MiddleVal>> arrayMiddleVal;
    private int basicBlockLabelIndex;
    private FuncSymbol curFuncSymbol;
    private BasicBlock curIfBasicBlock; // 条件语句使用
    private BasicBlock curElseBasicBlock; // 条件语句使用
    private BasicBlock curNextBasicBlock; // 条件语句使用
    private BasicBlock curLoopBasicBlock; // 循环语句使用
    private BasicBlock curIncrementBasicBlock; // 循环增量基本块
    private BasicBlock curOutBasicBlock; // 退出循环的第一个基本块，不与if共用next，防止嵌套引起的问题
    private boolean isLoopCond; // condRec执行时，区分是循环还是分支语句
    private ArrayList<BasicBlock> orCondBasicBlockList;
    private int orCondBasicBlockIndex; // 一个括号内计数，标识由"||"连接的条件表达式基本块
    private int condBasicBlockIndex; // 一个if括号标识一次
    private boolean branchIsWithElse; // 跳转语句是否带有else
    private boolean eqExpHasNoOp; // 表示当前eqExp没有相等性判断，配合relExp中有无不等性判断，可以判断出所在的lAnd是否是一个单独的exp。
    private boolean lValIsAssign; // 表示当前lVal是赋值语句，lVal充当左值
    public LlvmGenerator(SyntaxAnalyzer SA){
        this.SA = SA;
        this.symTab = new SymbolTable();
        this.curBasicBlockList = new ArrayList<>();
        this.out = new LlvmTree();
        this.insFactory = new InsFactory();
        this.curFuncFParamList = null;
        this.isInFuncBlockWithParam = false;
        this.isGlobalDecl = false;
        this.middleVal = new MiddleVal();
        this.arrayMiddleVal = new ArrayList<>();
        this.basicBlockLabelIndex = 0;
        this.curFuncSymbol = null;
        this.isLoopCond = false;
        this.orCondBasicBlockIndex = 0;
        this.condBasicBlockIndex = 0;
        this.branchIsWithElse = false;
        this.eqExpHasNoOp = false;
        this.lValIsAssign = false;
    }
    public void traverse(){
        CompUnitRec(SA.getRoot());
        System.out.println(out);
    }
    public void CompUnitRec(CompUnit compUnit){
        ArrayList<Decl> declList = compUnit.getDeclList();
        ArrayList<FuncDef> funcDefList = compUnit.getFuncDefList();
        MainFuncDef mainFuncDef = compUnit.getMainFuncDef();
        for(Decl decl: declList){
            isGlobalDecl = true;
            declRec(decl);
            isGlobalDecl = false;
        }
        for(FuncDef funcDef: funcDefList){
            funcDefRec(funcDef);
        }
        mainFuncDefRec(mainFuncDef);
    }

    private void declRec(Decl decl){
        // Decl不输出：直接区分ConstDecl和VarDecl
        ConstDecl constDecl = decl.getConstDecl();
        VarDecl varDecl = decl.getVarDecl();
        if(constDecl != null){
            // 常量声明
            constDeclRec(constDecl);
        }else{
            // 变量声明
            varDeclRec(varDecl);
        }
    }
    private void constDeclRec(ConstDecl constDecl){ // constDecl递归子程序
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        BType bType = constDecl.getbType();
        ArrayList<ConstDef> constDefList = constDecl.getConstDefList();
        for(ConstDef constDef: constDefList){
            constDefRec(constDef);
        }
    }
    private void constDefRec(ConstDef constDef){ // constDef递归子程序
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        Word ident = constDef.getIdent();
        ArrayList<ConstExp> constExpList = constDef.getConstExpList();
        ConstInitVal constInitVal = constDef.getConstInitVal();

        if(constExpList.isEmpty()){ // 普通变量
            //填符号表
            constInitValRec(constInitVal);
            VarSymbol vs = declareVar(ident.getToken(), 0, new ArrayList<>(), true, middleVal.copy());
            if(isGlobalDecl){ // 全局
                vs.setStoreReg("@" + vs.getToken());
                GlobalDecl globalDecl = insFactory.generateGlobalDecl(vs);
                out.addGlobalDecl(globalDecl);
            }else {
                // 局部
                AllocaIns allocaIns = insFactory.generateAlloca(curBasicBlock, "i32");
                String allocaReg = allocaIns.getName(); // 存储空间的寄存器名
                insFactory.generateStore(curBasicBlock, "i32", middleVal.toString(),"i32*" , allocaReg);
                // store命令在val和addr之间建立了索引关系,下次访问val时生成load命令
                vs.setStoreReg(allocaReg);
            }
        }else{ // 数组
            int dimension = constExpList.size(); // 数组维数
            MiddleVal innerLen, outerLen;
            boolean isGlobal = isGlobalDecl;
            isGlobalDecl = true;
            constExpRec(constExpList.get(0));
            isGlobalDecl = isGlobal;
            if(dimension == 1){ // 一维数组
                innerLen = middleVal.copy(); // 数组长度
                outerLen = new MiddleVal("intConst", 1, null, null);
            }else{ // 二维数组
                outerLen = middleVal.copy(); // a[x][]
                isGlobal = isGlobalDecl;
                isGlobalDecl = true;
                constExpRec(constExpList.get(1));
                isGlobalDecl = isGlobal;
                innerLen = middleVal.copy(); // a[][x]
            }
            if(isGlobalDecl){ // 全局
                arrayMiddleVal = new ArrayList<>();
                constInitValRec(constInitVal);
                ArrayList<Integer> lengths = new ArrayList<>();
                lengths.add(outerLen.getIntCon());
                lengths.add(innerLen.getIntCon());
                VarSymbol vs = declareArray(ident.getToken(), dimension, lengths, true, arrayMiddleVal);
                vs.setStoreReg("@" + vs.getToken());
                out.addGlobalDecl(insFactory.generateGlobalDecl(vs));
            }else{ // 局部
                arrayMiddleVal = new ArrayList<>();
                AllocaIns allocaIns;
                if(dimension == 1){
                    allocaIns = insFactory.generateAlloca(curBasicBlock, "[" + innerLen + " x i32]");
                }else{
                    allocaIns = insFactory.generateAlloca(curBasicBlock, "[" + outerLen + " x [" + innerLen + " x i32]]");
                }
                String allocaReg = allocaIns.getName(); // 存储空间的寄存器名
                constInitValRec(constInitVal);
                ArrayList<Integer> lengths = new ArrayList<>();
                lengths.add(outerLen.getIntCon());
                lengths.add(innerLen.getIntCon());
                VarSymbol vs = declareArray(ident.getToken(), dimension, lengths, true, arrayMiddleVal);
                vs.setStoreReg(allocaReg);
                // 生成getelementptr指令：
                String regName; // getelementptr指令的寄存器名，即计算得到的地址寄存器
                if(dimension == 1){
                    ArrayList<MiddleVal> inner = arrayMiddleVal.get(0);
                    for(int i = 0; i < inner.size(); i ++){
                        MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                        MiddleVal offset1 = new MiddleVal("intConst", i, null, null);
                        GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + innerLen + " x i32]", allocaReg, offset0, offset1, null);
                        regName = getelementptrIns.getName();
                        // 生成store指令：
                        String vt = inner.get(i).getValType();
                        String vv = inner.get(i).toString();
                        String at = vt + "*";
                        String av = regName;
                        insFactory.generateStore(curBasicBlock, vt, vv, at, av);
                    }
                }else{
                    for(int i = 0; i < arrayMiddleVal.size(); i ++){
                        ArrayList<MiddleVal> inner = arrayMiddleVal.get(i);
                        for(int j = 0; j < inner.size(); j ++){
                            MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                            MiddleVal offset1 = new MiddleVal("intConst", i, null, null);
                            MiddleVal offset2 = new MiddleVal("intConst", j, null, null);
                            GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + outerLen + " x [" + innerLen + " x i32]]", allocaReg, offset0, offset1, offset2);
                            regName = getelementptrIns.getName();
                            // 生成store指令：
                            String vt = inner.get(j).getValType();
                            String vv = inner.get(j).toString();
                            String at = vt + "*";
                            String av = regName;
                            insFactory.generateStore(curBasicBlock, vt, vv, at, av);
                        }
                    }
                }
            }
        }
    }
    private void constExpRec(ConstExp constExp){ // constExp递归子程序
        //  ConstExp → AddExp
        AddExp addExp = constExp.getAddExp();
        addExpRec(addExp);
    }
    private void addExpRec(AddExp addExp){ // addExp子程序
        // AddExp → MulExp { ('+' | '−') MulExp }
        ArrayList<MulExp> mulExpList = addExp.getMulExpList();
        ArrayList<String> opList = addExp.getOpList();
        int entryNum = mulExpList.size(); // 加减表达式的项数

        if(entryNum == 1){ // 一项，即mulExp
            mulExpRec(mulExpList.get(0));
        }else {
            MulExp mulExp1 = mulExpList.get(0);
            mulExpRec(mulExp1);
            MiddleVal L = middleVal.copy();
            for(int i = 1; i < entryNum; i++){
                MulExp mulExp_r = mulExpList.get(i);
                mulExpRec(mulExp_r);
                MiddleVal R = middleVal.copy();
                String op = opList.get(i - 1);
                if(op.equals("plus")){ // add
                    if(isGlobalDecl){
                        middleVal.setIntCon(L.getIntCon() + R.getIntCon());
                    }else{
                        AddIns addIns = insFactory.generateAdd(curBasicBlock, L, R);
                        middleVal.setRegister(addIns.getRegName());
                    }
                }else{ // "minu" sub
                    if(isGlobalDecl){
                        middleVal.setIntCon(L.getIntCon() - R.getIntCon());
                    }else{
                        SubIns subIns = insFactory.generateSub(curBasicBlock, L, R);
                        middleVal.setRegister(subIns.getRegName());
                    }
                }
                L = middleVal.copy();
            }
        }
    }
    private void mulExpRec(MulExp mulExp){
        // MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp }
        ArrayList<UnaryExp> unaryExpList = mulExp.getUnaryExpList();
        ArrayList<String> opList = mulExp.getOpList();
        int entryNum = unaryExpList.size(); // 乘除模表达式的项数

        if(entryNum == 1){ // 一项，即unaryExp
            unaryExpRec(unaryExpList.get(0));
        }else {
            UnaryExp unaryExp1 = unaryExpList.get(0);
            unaryExpRec(unaryExp1);
            MiddleVal L = middleVal.copy();
            for(int i = 1; i < entryNum; i++){
                UnaryExp unaryExp_r = unaryExpList.get(i);
                unaryExpRec(unaryExp_r);
                MiddleVal R = middleVal.copy();
                String op = opList.get(i - 1);
                if(op.equals("mult")){ // mul
                    if(isGlobalDecl){
                        middleVal.setIntCon(L.getIntCon() * R.getIntCon());
                    }else{
                        MulIns mulIns = insFactory.generateMul(curBasicBlock, L, R);
                        middleVal.setRegister(mulIns.getRegName());
                    }
                }else if(op.equals("div")){ // sdiv
                    if(isGlobalDecl){
                        middleVal.setIntCon(L.getIntCon() / R.getIntCon());
                    }else{
                        SdivIns sdivIns = insFactory.generateSdiv(curBasicBlock, L, R);
                        middleVal.setRegister(sdivIns.getRegName());
                    }
                }else{ // "mod" srem
                    if(isGlobalDecl){
                        middleVal.setIntCon(L.getIntCon() % R.getIntCon());
                    }else{
                        SremIns sremIns = insFactory.generateSrem(curBasicBlock, L, R);
                        middleVal.setRegister(sremIns.getRegName());
                    }
                }
                L = middleVal.copy();
            }
        }
    }
    private void unaryExpRec(UnaryExp unaryExp){
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        PrimaryExp primaryExp = unaryExp.getPrimaryExp();
        Word ident = unaryExp.getIdent();
        FuncRParams funcRParams = unaryExp.getFuncRParams();
        UnaryExp unaryExp1 = unaryExp.getUnaryExp();
        UnaryOp unaryOp = unaryExp.getUnaryOp();

        if(unaryExp.getUnaryExpType().equals("primary")){ // 数字、括号、变量名等
            primaryExpRec(primaryExp);
        }else if(unaryExp.getUnaryExpType().equals("call")){ // 函数调用
            if(ident.getToken().equals("getint")){ // 表达式调用库函数getint()
                CallIns callIns = insFactory.generateCall(curBasicBlock, "i32", "@getint", new ArrayList<>());
                middleVal.setRegister(callIns.getRegName());
            }else{ // 自定义的函数
                FuncSymbol fs = (FuncSymbol) searchSymbol(ident.getToken());
                String retType = fs.voidFunc()?"void":"i32";
                ArrayList<MiddleVal> funcRParamValList = curFuncRParamValList; // 为上一层调用（如果有的话）记录下当前全局参数值列表
                funcRParamsRec(funcRParams);
                ArrayList<Parameter> parameterList = new ArrayList<>();
                int index = 0;
                ArrayList<FuncFParam> funcFParamList = fs.getFuncFParams().getFuncFParamList();
                for(FuncFParam funcFParam: funcFParamList){ // 遍历形参表
                    String parameterType = new String();
                    if(funcFParam.getDimension() == 0){
                        parameterType = "i32";
                    }else if(funcFParam.getDimension() == 1){
                        parameterType = "i32*";
                    }else if(funcFParam.getDimension() == 2){
                        ConstExp constExp = funcFParam.getConstExpList().get(0);
                        isGlobalDecl = true;
                        constExpRec(constExp);
                        isGlobalDecl = false;
                        MiddleVal innerLen = middleVal.copy();
                        parameterType = "[" + innerLen + " x i32]*";
                    }
                    parameterList.add(new Parameter(parameterType, curFuncRParamValList.get(index ++)));
                }
                CallIns callIns = insFactory.generateCall(curBasicBlock, retType, "@" + ident.getToken(), parameterList);
                middleVal.setRegister(callIns.getRegName());
                curFuncRParamValList = funcRParamValList; // 退出函数调用，为上一层调用（如果有的话）恢复其全局参数值列表
            }
            middleVal.setValType("i32");
        }else{ // "signed" 一元运算符
            unaryExpRec(unaryExp1);
            MiddleVal R = middleVal.copy();
            if(unaryOp.getTerminal().getType() == LexicalType.MINU){ // 负号，相当于用0减
                MiddleVal zero = new MiddleVal("intConst", 0, null, null);
                if(isGlobalDecl){
                    middleVal.setIntCon(-R.getIntCon());
                }else{
                    SubIns subIns = insFactory.generateSub(curBasicBlock, zero, R);
                    middleVal.setRegister(subIns.getRegName());
                }
                middleVal.setValType("i32");
            }else if(unaryOp.getTerminal().getType() == LexicalType.NOT) { // 非号，相当于用0比较
                MiddleVal zero = new MiddleVal("intConst", 0, null, null);
                zero.setValType(R.getValType());
                IcmpIns icmpIns = insFactory.generateIcmp(curBasicBlock, "eq", R.getValType(), zero, R);
                middleVal.setRegister(icmpIns.getRegName());
                middleVal.setValType("i1");
            }
        }
    }
    private void unaryOpRec(){
        // UnaryOp → '+' | '−' | '!'

    }
    private void funcRParamsRec(FuncRParams funcRParams){
        //  FuncRParams → Exp { ',' Exp }
        ArrayList<Exp> expList = funcRParams.getExpList();
        curFuncRParamValList = new ArrayList<>();
        for(Exp exp: expList){
            expRec(exp);
            curFuncRParamValList.add(middleVal.copy());
        }
    }
    private void expRec(Exp exp){
        //  Exp → AddExp
        AddExp addExp = exp.getAddExp();
        addExpRec(addExp);
    }
    private void primaryExpRec(PrimaryExp primaryExp){
        // PrimaryExp → '(' Exp ')' | LVal | Number
        Exp exp = primaryExp.getExp();
        LVal lVal= primaryExp.getlVal();
        Number number = primaryExp.getNumber();
        if(exp != null){ // 第一种
            expRec(exp);
        }else if(lVal != null){ // 第二种
            lValRec(lVal);
            middleVal.setValType("i32");
        }else{ // 第三种：数字
            middleVal.setType("intConst");
            middleVal.setIntCon(number.getNumber());
            middleVal.setValType("i32");
        }
    }
    private void lValRec(LVal lVal){
        //  LVal → Ident {'[' Exp ']'}
        Word ident = lVal.getIdent();
        ArrayList<Exp> expList = lVal.getExpList();
        VarSymbol vs = (VarSymbol) searchSymbol(ident.getToken());
//        int dimension = vs.getDimension() - expList.size();
        int dimension = vs.getDimension();
        if(dimension == 0){
            if(vs.getStoreReg() != null && !isGlobalDecl){
                // 需要通过load取变量
                LoadIns loadIns = insFactory.generateLoad(curBasicBlock, "i32", "i32*", vs.getStoreReg());
                vs.setValue(new MiddleVal("register", 0, loadIns.getName(), null));
                middleVal.setRegister(loadIns.getName());
            }else{
                middleVal = vs.getValue().copy();
            }
        }else if(dimension == 1){ // 一维数组
            if(vs.isPointer()){
                // 以指针形式访问一维数组（函数传参）
                String ppReg = vs.getStoreReg(); // 传入的指针参数的地址：指针的指针
                LoadIns loadIns = insFactory.generateLoad(curBasicBlock, "i32*", "i32**", ppReg);
                String pointerReg = loadIns.getName(); // 对ppReg取load运算，得到的就是传入的指针形参，用于做getelementptr计算指令
                middleVal.setRegister(pointerReg);
                if(expList.size() == 1){
                    // 对一维数组取值
                    boolean assign = lValIsAssign;
                    lValIsAssign = false; // 如果是左值访问数组，那么[]内表达式一定不是赋值访问
                    expRec(expList.get(0));
                    lValIsAssign = assign; // 恢复lValIsAssign的值
                    MiddleVal index = middleVal.copy();
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "i32", pointerReg, index, null, null);
                    if(!lValIsAssign){ // 如果lVal是左值，不需要load取值
                        LoadIns loadIns1 = insFactory.generateLoad(curBasicBlock, "i32", "i32*", getelementptrIns.getName());
                        middleVal.setRegister(loadIns1.getName());
                    }else{
                        middleVal.setRegister(getelementptrIns.getName());
                    }
                }
            }else{
                if(expList.size() == 1){
                    // 对一维数组取值
                    ArrayList<MiddleVal> inner = vs.getArrayValue().get(0);
                    boolean assign = lValIsAssign;
                    lValIsAssign = false; // 如果是左值访问数组，那么[]内表达式一定不是赋值访问
                    expRec(expList.get(0));
                    lValIsAssign = assign; // 恢复lValIsAssign的值
                    MiddleVal index = middleVal.copy();
                    MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + inner.size() + " x i32]", vs.getStoreReg(), offset0, index, null);
                    if(!lValIsAssign){ // 如果lVal是左值，不需要load取值
                        LoadIns loadIns = insFactory.generateLoad(curBasicBlock, "i32", "i32*", getelementptrIns.getName());
                        middleVal.setRegister(loadIns.getName());
                    }else{
                        middleVal.setRegister(getelementptrIns.getName());
                    }
                }else if(expList.isEmpty()){
                    // 对一维数组取地址
                    int innerLen = vs.getArrayValue().get(0).size();
                    MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + innerLen + " x i32]", vs.getStoreReg(), offset0, offset0, null);
                    middleVal.setRegister(getelementptrIns.getName());
//                    middleVal.setValType("i32*"); // 类型为i32*
                }
            }
        }else{ // 二维数组
            if(vs.isPointer()){
                // 以指针形式访问二维数组（函数传参）
                String ppReg = vs.getStoreReg(); // 传入的指针参数的地址：指针的指针
                int innerLen = vs.getLength().get(1);
                LoadIns loadIns = insFactory.generateLoad(curBasicBlock, "[" + innerLen + " x i32]*", "[" + innerLen + " x i32]**", ppReg);
                String pointerReg = loadIns.getName(); // 对ppReg取load运算，得到的就是传入的指针形参，用于做getelementptr计算指令
                middleVal.setRegister(loadIns.getName());
                if(expList.size() == 2){
                    // 对二维数组取值再取值
                    boolean assign = lValIsAssign;
                    lValIsAssign = false; // 如果是左值访问数组，那么[]内表达式一定不是赋值访问
                    expRec(expList.get(0));
                    MiddleVal outerIndex = middleVal.copy();
                    expRec(expList.get(1));
                    MiddleVal innerIndex = middleVal.copy();
                    lValIsAssign = assign; // 恢复lValIsAssign的值
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + innerLen + " x i32]", pointerReg, outerIndex, innerIndex, null);
                    if(!lValIsAssign){ // 如果lVal是左值，不需要load取值
                        LoadIns loadIns1 = insFactory.generateLoad(curBasicBlock, "i32", "i32*", getelementptrIns.getName());
                        middleVal.setRegister(loadIns1.getName());
                    }else{
                        middleVal.setRegister(getelementptrIns.getName());
                    }
                }else if(expList.size() == 1){
                    // 对二维数组取值，得到一维数组
                    boolean assign = lValIsAssign;
                    lValIsAssign = false;
                    expRec(expList.get(0));
                    lValIsAssign = assign;
                    MiddleVal outerIndex = middleVal.copy();
                    MiddleVal offset1 = new MiddleVal("intConst", 0, null, null);
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + innerLen + " x i32]", pointerReg, outerIndex, offset1, null);
                    middleVal.setRegister(getelementptrIns.getName());
                }
            }else{
                if(expList.size() == 2){
                    // 对二维数组取值,再取值
                    int outerLen = vs.getArrayValue().size();
                    int innerLen = vs.getArrayValue().get(0).size();
                    boolean assign = lValIsAssign;
                    lValIsAssign = false;
                    expRec(expList.get(0));
                    MiddleVal outerIndex = middleVal.copy();
                    expRec(expList.get(1));
                    MiddleVal innerIndex = middleVal.copy();
                    lValIsAssign = assign; // 恢复lValIsAssign的值
                    MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + outerLen + " x [" + innerLen + " x i32]]", vs.getStoreReg(), offset0, outerIndex, innerIndex);
                    if(!lValIsAssign){
                        LoadIns loadIns = insFactory.generateLoad(curBasicBlock, "i32", "i32*", getelementptrIns.getName());
                        middleVal.setRegister(loadIns.getName());
                    }else{
                        middleVal.setRegister(getelementptrIns.getName());
                    }
                }else if(expList.size() == 1){
                    // 对二维数组取值，得到一维数组地址
                    int outerLen = vs.getArrayValue().size();
                    int innerLen = vs.getArrayValue().get(0).size();
                    boolean assign = lValIsAssign;
                    lValIsAssign = false;
                    expRec(expList.get(0));
                    lValIsAssign = assign;
                    MiddleVal outerIndex = middleVal.copy();
                    MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + outerLen + " x [" + innerLen + " x i32]]", vs.getStoreReg(), offset0, outerIndex, offset0);
                    middleVal.setRegister(getelementptrIns.getName());
//                    middleVal.setValType("i32*"); // 类型为i32*
                }else if(expList.isEmpty()){
                    // 对二维数组取地址
                    int outerLen = vs.getArrayValue().size();
                    int innerLen = vs.getArrayValue().get(0).size();
                    MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                    GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + outerLen + " x [" + innerLen + " x i32]]", vs.getStoreReg(), offset0, offset0, null);
                    middleVal.setRegister(getelementptrIns.getName());
//                    middleVal.setValType("i32**"); // 类型为i32**
                }
            }
        }
    }
    private void constInitValRec(ConstInitVal constInitVal){ // constInitVal子程序
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        ConstExp constExp = constInitVal.getConstExp();
        ArrayList<ConstInitVal> constInitValList = constInitVal.getConstInitValList();
        if(constInitValList == null){ // 普通变量
            constExpRec(constExp);
        }else{ // 数组
            if(constInitValList.get(0).isConstExp()){
                // 一维数组
                arrayMiddleVal = new ArrayList<>();
                ArrayList<MiddleVal> inner = new ArrayList<>();
                for(ConstInitVal constInitVal1: constInitValList){
                    constInitValRec(constInitVal1);
                    inner.add(middleVal.copy());
                }
                arrayMiddleVal.add(inner);
            }else{
                // 二维数组
                arrayMiddleVal = new ArrayList<>();
                ArrayList<ArrayList<MiddleVal>> temp = new ArrayList<>(); // 用于临时记录arrayMiddleVal
                for(ConstInitVal constInitVal1: constInitValList){
                    constInitValRec(constInitVal1); // 此时arrayMiddleVal中有一个元素，就是initVal1的ArrayList
                    temp.add(arrayMiddleVal.get(0));
                }
                arrayMiddleVal = temp;
            }
        }
    }
    private void varDeclRec(VarDecl varDecl){ // VarDecl递归子程序
        //  VarDecl → BType VarDef { ',' VarDef } ';'
        ArrayList<VarDef> varDefList = varDecl.getVarDefList();
        for(VarDef varDef: varDefList){
            varDefRec(varDef);
        }
    }
    private void varDefRec(VarDef varDef){
        // VarDef  → Ident { '[' ConstExp ']' } ( # | '=' InitVal) #表示空串
        Word ident = varDef.getIdent();
        ArrayList<ConstExp> constExpList = varDef.getConstExpList();
        InitVal initVal = varDef.getInitVal();
        if(constExpList.isEmpty()){ // 普通变量
            if(isGlobalDecl){ // 全局
                if(initVal != null){ // 有初始值
                    initValRec(initVal);
                }else{
                    middleVal.setIntCon(0); // 全局变量初始化为0
                }
                VarSymbol vs = declareVar(ident.getToken(), 0, new ArrayList<>(), false, middleVal.copy());
                vs.setStoreReg("@" + vs.getToken());
                out.addGlobalDecl(insFactory.generateGlobalDecl(vs));
            }else{ // 局部
                AllocaIns allocaIns = insFactory.generateAlloca(curBasicBlock, "i32");
                String allocaReg = allocaIns.getName(); // 存储空间的寄存器名
                if(initVal != null){ // 有初始值
                    initValRec(initVal);
                }else{
                    middleVal.setIntCon(0); //
                }
                VarSymbol vs = declareVar(ident.getToken(), 0, new ArrayList<>(), false, middleVal.copy());
                insFactory.generateStore(curBasicBlock, "i32", middleVal.toString(),"i32*" , allocaReg);
                // store命令在val和addr之间建立了索引关系,下次访问val时生成load命令
                vs.setStoreReg(allocaReg);
            }
        }else{ // 数组
            int dimension = constExpList.size(); // 数组维数
            MiddleVal innerLen, outerLen;
            boolean isGlobal = isGlobalDecl;
            isGlobalDecl = true;
            constExpRec(constExpList.get(0));
            isGlobalDecl = isGlobal;
            if(dimension == 1){ // 一维数组
                innerLen = middleVal.copy(); // 数组长度
                outerLen = new MiddleVal("intConst", 1, null, null);
            }else{ // 二维数组
                outerLen = middleVal.copy(); // a[x][]
                isGlobal = isGlobalDecl;
                isGlobalDecl = true;
                constExpRec(constExpList.get(1));
                isGlobalDecl = isGlobal;
                innerLen = middleVal.copy(); // a[][x]
            }
            if(isGlobalDecl){ // 全局
                arrayMiddleVal = new ArrayList<>();
                if(initVal != null && initVal.getInitValList() != null){ // 有初始值
                    initValRec(initVal);
                }else{
                    // 全零初始化
                    for(int i = 0; i < outerLen.getIntCon(); i ++){
                        ArrayList<MiddleVal> inner = new ArrayList<>();
                        for(int j = 0; j < innerLen.getIntCon(); j ++){
                            inner.add(new MiddleVal("intConst", 0, null, null));
                        }
                        arrayMiddleVal.add(inner);
                    }
                }
                ArrayList<Integer> lengths = new ArrayList<>();
                lengths.add(outerLen.getIntCon());
                lengths.add(innerLen.getIntCon());
                VarSymbol vs = declareArray(ident.getToken(), dimension, lengths, false, arrayMiddleVal);
                vs.setStoreReg("@" + vs.getToken());
                out.addGlobalDecl(insFactory.generateGlobalDecl(vs));
            }else{ // 局部
                arrayMiddleVal = new ArrayList<>();
                AllocaIns allocaIns;
                if(dimension == 1){
                    allocaIns = insFactory.generateAlloca(curBasicBlock, "[" + innerLen + " x i32]");
                }else{
                    allocaIns = insFactory.generateAlloca(curBasicBlock, "[" + outerLen + " x [" + innerLen + " x i32]]");
                }
                String allocaReg = allocaIns.getName(); // 存储空间的寄存器名
                if(initVal != null){ // 有初始值
                    initValRec(initVal);
                }else{
                    for(int i = 0; i < outerLen.getIntCon(); i ++){
                        ArrayList<MiddleVal> inner = new ArrayList<>();
                        for(int j = 0; j < innerLen.getIntCon(); j ++){
                            inner.add(new MiddleVal("intConst", 0, null, null));
                        }
                        arrayMiddleVal.add(inner);
                    }
                    // 全零初始化
                }
                ArrayList<Integer> lengths = new ArrayList<>();
                lengths.add(outerLen.getIntCon());
                lengths.add(innerLen.getIntCon());
                VarSymbol vs = declareArray(ident.getToken(), dimension, lengths, false, arrayMiddleVal);
                vs.setStoreReg(allocaReg);
                // 生成getelementptr指令：
                String regName; // getelementptr指令的寄存器名，即计算得到的地址寄存器
                if(dimension == 1){
                    ArrayList<MiddleVal> inner = arrayMiddleVal.get(0);
                    for(int i = 0; i < inner.size(); i ++){
                        MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                        MiddleVal offset1 = new MiddleVal("intConst", i, null, null);
                        GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + innerLen + " x i32]", allocaReg, offset0, offset1, null);
                        regName = getelementptrIns.getName();
                        // 生成store指令：
                        String vt = inner.get(i).getValType();
                        String vv = inner.get(i).toString();
                        String at = vt + "*";
                        String av = regName;
                        insFactory.generateStore(curBasicBlock, vt, vv, at, av);
                    }
                }else{
                    for(int i = 0; i < arrayMiddleVal.size(); i ++){
                        ArrayList<MiddleVal> inner = arrayMiddleVal.get(i);
                        for(int j = 0; j < inner.size(); j ++){
                            MiddleVal offset0 = new MiddleVal("intConst", 0, null, null);
                            MiddleVal offset1 = new MiddleVal("intConst", i, null, null);
                            MiddleVal offset2 = new MiddleVal("intConst", j, null, null);
                            GetelementptrIns getelementptrIns = insFactory.generateGetelementptr(curBasicBlock, "[" + outerLen + " x [" + innerLen + " x i32]]", allocaReg, offset0, offset1, offset2);
                            regName = getelementptrIns.getName();
                            // 生成store指令：
                            String vt = inner.get(j).getValType();
                            String vv = inner.get(j).toString();
                            String at = vt + "*";
                            String av = regName;
                            insFactory.generateStore(curBasicBlock, vt, vv, at, av);
                        }
                    }
                }
            }
        }
    }
    private void initValRec(InitVal initVal){
        // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        Exp exp = initVal.getExp();
        ArrayList<InitVal> initValList = initVal.getInitValList();
        if(initValList == null){ // 普通变量
            expRec(exp);
        }else{ // 数组
            if(initValList.get(0).isExp()){
                // 一维数组
                arrayMiddleVal = new ArrayList<>();
                ArrayList<MiddleVal> inner = new ArrayList<>();
                for(InitVal initVal1: initValList){
                    initValRec(initVal1);
                    inner.add(middleVal.copy());
                }
                arrayMiddleVal.add(inner);
            }else{
                // 二维数组
                arrayMiddleVal = new ArrayList<>();
                ArrayList<ArrayList<MiddleVal>> temp = new ArrayList<>(); // 用于临时记录arrayMiddleVal
                for(InitVal initVal1: initValList){
                    initValRec(initVal1); // 此时arrayMiddleVal中有一个元素，就是initVal1的ArrayList
                    temp.add(arrayMiddleVal.get(0));
                }
                arrayMiddleVal = temp;
            }
        }
    }
    private void funcDefRec(FuncDef funcDef){
        //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncType funcType = funcDef.getFuncType();
        Word ident = funcDef.getIdent();
        FuncFParams funcFParams = funcDef.getFuncFParams();

        Block block = funcDef.getBlock();
        ArrayList<FuncFParam> funcFParamList = new ArrayList<>(funcFParams.getFuncFParamList());
        curFuncSymbol = declareFunc(ident.getToken(), !funcType.isInt(), funcFParams.getParamNum(), funcFParams.getDimensions(), new FuncFParams(funcFParamList));
        basicBlockLabelIndex = 0;
        curBasicBlock = new BasicBlock("start"); // 函数起始：新的基本块
        curBasicBlockList.add(curBasicBlock); // 初始为空，函数建立完成后也要清空为下一个函数做准备
        funcFParamsRec(funcFParams); // 递归访问参数表，为每个参数分配寄存器
        ArrayList<Parameter> parameterList = new ArrayList<>();
        for(FuncFParam funcFParam: curFuncFParamList){
            int dimension = funcFParam.getDimension();
            String type = new String();
            if(dimension == 0){
                type = "i32";
            }else if(dimension == 1){
                type = "i32*";
            }else if(dimension == 2){
                ConstExp constExp = funcFParam.getConstExpList().get(0);
                isGlobalDecl = true;
                constExpRec(constExp);
                isGlobalDecl = false;
                MiddleVal innerLen = middleVal.copy();
                type = "[" + innerLen + " x i32]*";
            }
            Parameter parameter = new Parameter(type, funcFParam.getValue());
            parameterList.add(parameter);
        }
        if(funcFParams.getParamNum() != 0){
            isInFuncBlockWithParam = true; // 表示进入了带参函数体，需要在新的block中给函数参数填符号表
        }
        blockRec(block); // 递归访问函数体block，建立相应基本块
        isInFuncBlockWithParam = false;
        String retType = funcType.isInt()?"i32":"void";
        Function function = insFactory.generateFunction("@" + ident.getToken(), retType, parameterList, curBasicBlockList);
        fillBlankLabels(function);
        out.addFunction(function);
        curBasicBlockList.clear();
        curFuncFParamList.clear();
    }
    private void blockRec(Block block){
        // Block → '{' { BlockItem } '}'
        createSymTab(); // 创建符号表
        if(isInFuncBlockWithParam){
            // 表示此时block为带参函数block，需要将curFuncFParams填符号表
            for(FuncFParam ffp:curFuncFParamList){
                // TODO:ArrayList length
                VarSymbol vs = declareVar(ffp.getIdent().getToken(), ffp.getDimension(), new ArrayList<>(), false,new MiddleVal());
                String valtype = new String();
                if(ffp.getDimension() == 0){ // 普通变量
                    valtype = "i32";
                }else if(ffp.getDimension() == 1){ // 一维数组
                    valtype = "i32*";
                    vs.setPointer(true);
                }else if(ffp.getDimension() == 2){ // 二维数组
                    if(!ffp.getConstExpList().isEmpty()){
                        ConstExp constExp = ffp.getConstExpList().get(0);
                        isGlobalDecl = true;
                        constExpRec(constExp);
                        isGlobalDecl = false;
                        MiddleVal innerLen = middleVal.copy();
                        valtype = "[" + innerLen + " x i32]*";
                        vs.setPointer(true);
                        ArrayList<Integer> lengths = new ArrayList<>();
                        lengths.add(0);
                        lengths.add(innerLen.getIntCon());
                        vs.setLength(lengths);
                    }
                }
                String addrType = valtype + "*";
                String storeRegName = insFactory.generateAlloca(curBasicBlock, valtype).getName();
                insFactory.generateStore(curBasicBlock, valtype, ffp.getValue().toString(), addrType, storeRegName);
                vs.setStoreReg(storeRegName);
            }
            isInFuncBlockWithParam = false;
        }

        ArrayList<BlockItem> blockItemList = block.getBlockItemList();
        for(BlockItem blockItem: blockItemList){
            blockItemRec(blockItem);
        }
        exitSymTab();
    }
    private void blockItemRec(BlockItem blockItem){
        // BlockItem → Decl | Stmt
        Decl decl = blockItem.getDecl();
        Stmt stmt = blockItem.getStmt();
        if(decl != null){
            declRec(decl);
        }else{
            stmtRec(stmt);
        }
    }
    private void stmtRec(Stmt stmt){
        /* Stmt → LVal '=' Exp ';'
            | [Exp] ';'
            | Block
            | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            | 'break' ';'
            | 'continue' ';'
            | 'return' [Exp] ';'
            | LVal '=' 'getint''('')'';'
            | 'printf''('FormatString{','Exp}')'';'
        */
        LVal lVal;
        Exp exp;
        Block block;
        Cond cond;
        Stmt _stmt;
        Stmt elseStmt;
        ForStmt forStmt1;
        ForStmt forStmt2;
        Word forTerminal;
        Word formatString;
        ArrayList<Exp> expList = new ArrayList<>();
        if(stmt.getStmtType().equals("returnWithExp")){
            exp = stmt.getExp();
            expRec(exp);
            insFactory.generateRetWithExp(curBasicBlock,"i32",middleVal.copy());
        }else if(stmt.getStmtType().equals("returnVoid")){
            insFactory.generateRetVoid(curBasicBlock);
        }else if(stmt.getStmtType().equals("block")){
            block = stmt.getBlock();
            blockRec(block);
        }else if(stmt.getStmtType().equals("assign")){
            lVal = stmt.getlVal();
            exp = stmt.getExp();
            VarSymbol vs = (VarSymbol) searchSymbol(lVal.getIdent().getToken());
            String storeRegName = vs.getStoreReg();
            expRec(exp);
            MiddleVal rVal = middleVal.copy();
            if(vs.getDimension() > 0){ // 数组
                lValIsAssign = true;
                lValRec(lVal);
                lValIsAssign = false;
                storeRegName = middleVal.getRegister();
            }
            insFactory.generateStore(curBasicBlock, "i32", rVal.toString(), "i32*", storeRegName);
        }else if(stmt.getStmtType().equals("assign_getint")){
            lVal = stmt.getlVal();
            VarSymbol vs = (VarSymbol) searchSymbol(lVal.getIdent().getToken());
            String storeRegName = vs.getStoreReg();
            CallIns callIns = insFactory.generateCall(curBasicBlock, "i32", "@getint", new ArrayList<>());
            middleVal.setRegister(callIns.getRegName());
            MiddleVal rVal = middleVal.copy();
            if(vs.getDimension() > 0){ // 数组
                lValIsAssign = true;
                lValRec(lVal);
                lValIsAssign = false;
                storeRegName = middleVal.getRegister();
            }
            insFactory.generateStore(curBasicBlock, "i32", rVal.toString(), "i32*", storeRegName);
        }else if(stmt.getStmtType().equals("printf")){
            formatString = stmt.getFormatString();
            expList = stmt.getExpList();
            int expIndex = 0;
            char[] charArray = formatString.getToken().toCharArray();
            for(int i = 0; i < formatString.getToken().length(); i ++ ){
                if(charArray[i] == '"'){
                    continue;
                }else if(charArray[i] == '%' && charArray[i + 1] == 'd'){
                    i += 1;
                    Exp paramExp = expList.get(expIndex ++);
                    expRec(paramExp);
                    ArrayList<Parameter> putchParams = new ArrayList<>();
                    Parameter parameter = new Parameter("i32", middleVal.copy());
                    putchParams.add(parameter);
                    insFactory.generateCall(curBasicBlock, "void", "@putint", putchParams);
                }else if(charArray[i] == '\\' && charArray[i + 1] == 'n'){
                    i += 1;
                    ArrayList<Parameter> putchParams = new ArrayList<>();
                    Parameter parameter = new Parameter("i32", new MiddleVal("intConst", '\n', null, null));
                    putchParams.add(parameter);
                    insFactory.generateCall(curBasicBlock, "void", "@putch", putchParams);
                }else{
                    ArrayList<Parameter> putchParams = new ArrayList<>();
                    middleVal.setIntCon(charArray[i]);
                    Parameter parameter = new Parameter("i32", middleVal.copy());
                    putchParams.add(parameter);
                    insFactory.generateCall(curBasicBlock, "void", "@putch", putchParams);
                }
            }
        }else if(stmt.getStmtType().equals("exp")){
            exp = stmt.getExp();
            if(exp != null){
                expRec(exp);
            }
        }else if(stmt.getStmtType().equals("branchWithoutElse")){
            branchIsWithElse = false; // 不同的branchIsWithElse值决定了condRec()中构建br指令不同的跳转逻辑
            isLoopCond = false; // 防止loop内嵌套的if语句标签错乱
            cond = stmt.getCond();
            _stmt = stmt.getStmt();
            BasicBlock nextBasicBlock = curNextBasicBlock; // 为上一层记录全局变量
            curIfBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "if_" + basicBlockLabelIndex ++);
            curNextBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "next_" + basicBlockLabelIndex ++);
            condRec(cond);
            curBasicBlock = curIfBasicBlock;
            curBasicBlockList.add(curBasicBlock);
            stmtRec(_stmt);
            insFactory.generateBr(curBasicBlock, false, null, null, null, curNextBasicBlock.getLabel());
            curBasicBlock = curNextBasicBlock;
            curBasicBlockList.add(curBasicBlock);
            curNextBasicBlock = nextBasicBlock;
        }else if(stmt.getStmtType().equals("branchWithElse")){
            branchIsWithElse = true; // 不同的branchIsWithElse值决定了condRec()中构建br指令不同的跳转逻辑
            isLoopCond = false; // 防止loop内嵌套的if语句标签错乱
            cond = stmt.getCond();
            _stmt = stmt.getStmt();
            elseStmt = stmt.getElseStmt();
            BasicBlock elseBasicBlock = curElseBasicBlock;
            BasicBlock nextBasicBlock = curNextBasicBlock; // 为上一层branch记录下其全局基本快变量
            curIfBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "if_" + basicBlockLabelIndex ++);
            curElseBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "else_" + basicBlockLabelIndex ++);
            curNextBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "next_" + basicBlockLabelIndex ++); // 整个if-else语句结束后后面的代码
            condRec(cond);
            curBasicBlock = curIfBasicBlock;
            curBasicBlockList.add(curBasicBlock);
            stmtRec(_stmt);
            insFactory.generateBr(curBasicBlock, false, null, null, null, curNextBasicBlock.getLabel()); // if true的基本块最后要跳转到next基本块
            curBasicBlock = curElseBasicBlock;
            curBasicBlockList.add(curBasicBlock);
            stmtRec(elseStmt);
            curBasicBlock = curNextBasicBlock;
            curBasicBlockList.add(curBasicBlock);
            curNextBasicBlock = nextBasicBlock;
            curElseBasicBlock = elseBasicBlock;
        }else if(stmt.getStmtType().equals("loop")){
            forStmt1 = stmt.getForStmt1();
            cond = stmt.getCond();
            forStmt2 = stmt.getForStmt2();
            _stmt = stmt.getStmt();
            if(forStmt1 != null){
                forStmtRec(forStmt1); // 先执行forstmt1中的代码
            }
            int loopNum = basicBlockLabelIndex;
            BasicBlock incrementBasicBlock = curIncrementBasicBlock;
            BasicBlock outBasicBlock = curOutBasicBlock; // 记录下其外层的全局基本快变量
            curLoopBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "loop_" + basicBlockLabelIndex ++);
            curIncrementBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "increment_" + basicBlockLabelIndex ++);
            curOutBasicBlock = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "out_" + basicBlockLabelIndex ++);
            // loop_start标签：
            BasicBlock loop_start = new BasicBlock("label_" + curFuncSymbol.getToken() + "_" + "loop_start_" + loopNum);
            curBasicBlockList.add(loop_start);
            // 所有的cond条件基本块：
            isLoopCond = true;
            if(cond != null) {
                condRec(cond);
            }
            isLoopCond = false;
            // loop标签（loop循环体）：
            curBasicBlock = curLoopBasicBlock;
            curBasicBlockList.add(curBasicBlock);
            stmtRec(_stmt); // 执行循环体
            curBasicBlock = curIncrementBasicBlock;
            curBasicBlockList.add(curBasicBlock);
            if(forStmt2 != null){
                forStmtRec(forStmt2); // 增减量
            }
            insFactory.generateBr(curBasicBlock, false, null, null, null, loop_start.getLabel()); // 循环体结尾无条件跳转语句
            curBasicBlock = curOutBasicBlock;
            curBasicBlockList.add(curBasicBlock);

            curIncrementBasicBlock = incrementBasicBlock;
            curOutBasicBlock = outBasicBlock; // 为上一层恢复其全局基本块
        }else if(stmt.getStmtType().equals("break")){
            // 循环体中的break语句，直接产生一个到当前循环体out基本块的跳转
            insFactory.generateBr(curBasicBlock, false, null, null, null, curOutBasicBlock.getLabel());
        }else if(stmt.getStmtType().equals("continue")){
            // 循环体中的continue语句，直接产生一个到当前循环体increment（增量语句块）基本块的跳转
            insFactory.generateBr(curBasicBlock, false, null, null, null, curIncrementBasicBlock.getLabel());
        }
    }
    private void condRec(Cond cond){
        // Cond → LOrExp
        LOrExp lOrExp = cond.getlOrExp();
        lOrExpRec(lOrExp);
        condBasicBlockIndex ++;
//        middleVal.setValType("i32"); // 消除所有可能的i1
    }
    private void lOrExpRec(LOrExp lOrExp){
        // LOrExp → LAndExp { '||' LAndExp }
        ArrayList<LAndExp> lAndExpList = lOrExp.getlAndExpList();
        orCondBasicBlockList = new ArrayList<>();
        orCondBasicBlockIndex = 0;
        for(int i = 0; i < lAndExpList.size(); i ++){
            BasicBlock basicBlock = new BasicBlock("label_orCond_" + condBasicBlockIndex + "_" + i);
            orCondBasicBlockList.add(basicBlock);
        }
        // 不要试图将这两个for循环合并！后果很严重！
        for (LAndExp lAndExp : lAndExpList) {
            curBasicBlock = orCondBasicBlockList.get(orCondBasicBlockIndex++);
            curBasicBlockList.add(curBasicBlock);
            lAndExpRec(lAndExp);
        }
    }
    private void lAndExpRec(LAndExp lAndExp){
        // LAndExp → EqExp { '&&' EqExp }
        ArrayList<EqExp> eqExpList = lAndExp.getEqExpList();
        ArrayList<BasicBlock> andCondBasicBlockList = new ArrayList<>();
        for(int i = 0; i < eqExpList.size(); i ++){
            BasicBlock basicBlock = new BasicBlock("label_andCond_" + condBasicBlockIndex + "_" + (orCondBasicBlockIndex-1) + "_" + i);
            andCondBasicBlockList.add(basicBlock);
        }
        for(int condBasicBlockIndex = 0; condBasicBlockIndex < eqExpList.size(); condBasicBlockIndex ++){
//            middleVal.setValType("i32");
            curBasicBlock = andCondBasicBlockList.get(condBasicBlockIndex);
            curBasicBlockList.add(curBasicBlock);
            EqExp eqExp = eqExpList.get(condBasicBlockIndex);
            eqExpRec(eqExp);
            MiddleVal icmpResult1 = middleVal.copy();
            String ifTrue, ifFalse;
            if(condBasicBlockIndex + 1 < eqExpList.size() && orCondBasicBlockIndex < orCondBasicBlockList.size()){
                // 此情况跳转目的标签都是条件表达式，有无else没有区别，是否是循环也无区别
                ifTrue = andCondBasicBlockList.get(condBasicBlockIndex + 1).getLabel();
                ifFalse = orCondBasicBlockList.get(orCondBasicBlockIndex).getLabel();
                insFactory.generateBr(curBasicBlock, true, icmpResult1, ifTrue, ifFalse, null);
            }else if(condBasicBlockIndex + 1 == eqExpList.size() && orCondBasicBlockIndex < orCondBasicBlockList.size()){
                // 此情况跳转到if基本块或条件表达式，有无else没有区别，若是循环则ifTrue不同
                if(isLoopCond){
                    ifTrue = curLoopBasicBlock.getLabel();
                }else{
                    ifTrue = curIfBasicBlock.getLabel();
                }
                ifFalse = orCondBasicBlockList.get(orCondBasicBlockIndex).getLabel();
                insFactory.generateBr(curBasicBlock, true, icmpResult1, ifTrue, ifFalse, null);
            }else if(condBasicBlockIndex + 1 == eqExpList.size() && orCondBasicBlockIndex == orCondBasicBlockList.size()){
                // 此情况true跳转到if基本块，false跳转到else或next基本块，有区别，循环ifTue和ifFalse都不同
                if(isLoopCond){
                    ifTrue = curLoopBasicBlock.getLabel();
                    ifFalse = curOutBasicBlock.getLabel();
                }else{
                    ifTrue = curIfBasicBlock.getLabel();
                    if(branchIsWithElse){
                        ifFalse = curElseBasicBlock.getLabel();
                    }else{
                        ifFalse = curNextBasicBlock.getLabel();
                    }
                }
                insFactory.generateBr(curBasicBlock, true, icmpResult1, ifTrue, ifFalse, null);
            }else if(condBasicBlockIndex + 1 < eqExpList.size() && orCondBasicBlockIndex == orCondBasicBlockList.size()){
                // 此情况true跳转到条件表达式，false跳转到else或next基本块，有区别，循环的ifFalse不同
                ifTrue = andCondBasicBlockList.get(condBasicBlockIndex + 1).getLabel();
                if(isLoopCond){
                    ifFalse = curOutBasicBlock.getLabel();
                }else{
                    if(branchIsWithElse){
                        ifFalse = curElseBasicBlock.getLabel();
                    }else{
                        ifFalse = curNextBasicBlock.getLabel();
                    }
                }
                insFactory.generateBr(curBasicBlock, true, icmpResult1, ifTrue, ifFalse, null);
            }
        }
    }
    private void eqExpRec(EqExp eqExp){
        // EqExp → RelExp { ('==' | '!=') RelExp }
        ArrayList<RelExp> relExpList = eqExp.getRelExpList();
        ArrayList<String> opList = eqExp.getOpList();
        if(opList.isEmpty()){
            eqExpHasNoOp = true;
        }
        RelExp relExp = relExpList.get(0);
        relExpRec(relExp);
        eqExpHasNoOp = false;
        MiddleVal L = middleVal.copy();
//        middleVal.setValType("i32"); // 防止因为生成单个表达式与0的icmp运算结果middleVal的valType没有及时改回"i32"对后续产生影响
        for(int i = 1;i < relExpList.size();i ++){
            RelExp relExp_r = relExpList.get(i);
            relExpRec(relExp_r);
            MiddleVal R = middleVal.copy();
            String op = opList.get(i - 1);
            IcmpIns icmpIns = insFactory.generateIcmp(curBasicBlock, op, "i32", L, R);
            middleVal.setRegister(icmpIns.getRegName());
            middleVal.setValType("i1");
            L = middleVal.copy();
        }
    }
    private void relExpRec(RelExp relExp){
        // RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp }
        ArrayList<AddExp> addExpList = relExp.getAddExpList();
        ArrayList<String> opList = relExp.getOpList();
        AddExp addExp = addExpList.get(0);
        addExpRec(addExp); // 取第一个AddExp的值
        MiddleVal L = middleVal.copy();
//        middleVal.setValType("i32");
        if(opList.isEmpty() && eqExpHasNoOp){ // 单个表达式，生成与0的icmp ne指令
            MiddleVal zero = new MiddleVal("intConst", 0, null, null);
            IcmpIns icmpIns = insFactory.generateIcmp(curBasicBlock, "ne", "i32", L, zero);
            middleVal.setRegister(icmpIns.getRegName());
            middleVal.setValType("i1"); // 逻辑运算结果值类型为i1
        }
        for(int i = 1; i < addExpList.size(); i ++){
            AddExp addExp_r = addExpList.get(i);
            addExpRec(addExp_r);
            MiddleVal R = middleVal.copy();
            String op = opList.get(i - 1);
            IcmpIns icmpIns = insFactory.generateIcmp(curBasicBlock, op, "i32", L, R);
            middleVal.setRegister(icmpIns.getRegName());
            middleVal.setValType("i1"); // 逻辑运算结果值类型为i1
            L = middleVal.copy();
        }
    }
    private void forStmtRec(ForStmt forStmt){
        // ForStmt → LVal '=' Exp
        LVal lVal = forStmt.getlVal();
        Exp exp = forStmt.getExp();
        VarSymbol vs = (VarSymbol) searchSymbol(lVal.getIdent().getToken());
        String storeRegName = vs.getStoreReg();
        expRec(exp);
        insFactory.generateStore(curBasicBlock, "i32", middleVal.toString(), "i32*", storeRegName);
        vs.setStoreReg(storeRegName);
    }
    private void funcFParamsRec(FuncFParams funcFParams){
        // FuncFParams → FuncFParam { ',' FuncFParam }
        ArrayList<FuncFParam> funcFParamList = funcFParams.getFuncFParamList();
        for(FuncFParam funcFParam: funcFParamList){
            funcFParamRec(funcFParam); // 为每个参数分配了一个临时寄存器，相当于一个标签
        }
        curFuncFParamList = funcFParamList;
    }
    private void funcFParamRec(FuncFParam funcFParam){
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        Word ident = funcFParam.getIdent();
        int dimension = funcFParam.getDimension();
        if(dimension == 0){ // 普通变量 "i32"
            String regName = insFactory.getParameterRegister();
            funcFParam.setValue(new MiddleVal("register", 0, regName, null));
        }else if(dimension == 1){ // 一维数组 "i32*"
            String regName = insFactory.getParameterRegister();
            funcFParam.setValue(new MiddleVal("register", 0, regName, null));
        }else{ // 二维数组"i32**"
            String regName = insFactory.getParameterRegister();
            funcFParam.setValue(new MiddleVal("register", 0, regName, null));
        }
    }
    private void mainFuncDefRec(MainFuncDef mainFuncDef){
        //  MainFuncDef → 'int' 'main' '(' ')' Block
        Block block = mainFuncDef.getBlock();
        curFuncSymbol = declareFunc("main", false, 0, new ArrayList<>(), new FuncFParams(new ArrayList<>()));
        basicBlockLabelIndex = 0;
        curBasicBlock = new BasicBlock("start"); // 函数起始的基本块
        curBasicBlockList.add(curBasicBlock);
        blockRec(block);
        FuncSymbol main = (FuncSymbol) symTab.getTable().get("main");
        String retType = main.voidFunc()?"void":"i32";
        Function mainFunc = insFactory.generateFunction("@main", retType, new ArrayList<>(), curBasicBlockList);
        fillBlankLabels(mainFunc);
        curBasicBlockList.clear(); // 清空，为下一个函数做准备
        out.addFunction(mainFunc);
    }
    public void fillBlankLabels(Function function){ // 为空标签设置向下一基本块跳转的指令
        ArrayList<BasicBlock> basicBlockList = function.getBasicBlockList();
        for(int i = 0; i < basicBlockList.size(); i ++){
            Instruction lastIns = null; // 基本块的最后一块指令
            if(!basicBlockList.get(i).getInstructionList().isEmpty()){
                lastIns = basicBlockList.get(i).getInstructionList().get(basicBlockList.get(i).getInstructionList().size() - 1);
            }
            if((basicBlockList.get(i).getInstructionList().isEmpty() || !lastIns.isTerminalIns())){
                if(i + 1 < basicBlockList.size()){
                    BrIns brIns = insFactory.generateBr(basicBlockList.get(i),false,null,null,null ,basicBlockList.get(i + 1).getLabel());
                }else{
                    // 已经是最后一个基本块，这意味着此函数实际上是一个void函数，而且没有return
                    insFactory.generateRetVoid(curBasicBlock);
                }
            }
        }
    }

    //符号表相关
    private void createSymTab(){ // 创建符号表
        symTab = new SymbolTable(symTab);
    }
    private void exitSymTab(){
        symTab = symTab.getParent();
    }
    private boolean symHadDeclared(String token){ // 符号已声明，在当前符号表中已存在token相同的符号
        for (String key:symTab.getTable().keySet()){ // 遍历符号表
            if(symTab.getTable().get(key).getToken().equals(token)){
                return true;
            }
        }
        return false;
    }
    public VarSymbol declareVar(String token, int dimension, ArrayList<Integer> length, boolean isConst, MiddleVal middleVal){ // 声明符号：填表
        VarSymbol vs = new VarSymbol(token, symTab.getLayer(), dimension, length, isConst, middleVal);
        symTab.getTable().put(token,vs);
        return vs;
    }
    public VarSymbol declareArray(String token, int dimension, ArrayList<Integer> length, boolean isConst, ArrayList<ArrayList<MiddleVal>> arrayValue){
        VarSymbol vs = new VarSymbol(token, symTab.getLayer(), dimension, length, isConst, arrayValue);
        symTab.getTable().put(token,vs);
        return vs;
    }
    public FuncSymbol declareFunc(String token, boolean isVoidFunc, int paramNum, ArrayList<Integer> dimensions, FuncFParams funcFParams){
        FuncSymbol fs = new FuncSymbol(token, symTab.getLayer(), isVoidFunc, paramNum, dimensions);
        fs.setFuncFParams(funcFParams);
        symTab.getTable().put(token,fs);
        return fs;
    }
    public Symbol searchSymbol(String token){ // 从当前层开始逐层寻找符号，若存在（本层或外层声明过）则返回
        SymbolTable symbolTable = symTab; // 当前层
        while(symbolTable != null && symbolTable.getLayer() >= 0){
            for (String key:symbolTable.getTable().keySet()){ // 遍历符号表
                if(symbolTable.getTable().get(key).getToken().equals(token)){
                    return symbolTable.getTable().get(key); // 找到后立刻返回：取最靠近内层的变量声明
                }
            }
            symbolTable = symbolTable.getParent();
        }
        return null;
    }

}
