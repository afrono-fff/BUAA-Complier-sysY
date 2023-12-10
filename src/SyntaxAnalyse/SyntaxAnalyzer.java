package SyntaxAnalyse;

import ErrorHandle.ErrorHandler;
import ErrorHandle.ErrorType;
import LexicalAnalyse.LexicalAnalyzer;
import LexicalAnalyse.LexicalType;
import LexicalAnalyse.Number;
import LexicalAnalyse.Word;
import SymbolTable.SymbolTable;
import SymbolTable.VarSymbol;
import SymbolTable.FuncSymbol;
import SymbolTable.Symbol;
import SyntaxAnalyse.SyntaxTreeNodes.*;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SyntaxAnalyzer {
    private LexicalAnalyzer LA; // 词法分析器
    private ArrayList<Word> wordList; // 词法分析器的结果集：单词表
    private int wordNum; // 单词表的单词数量
    private int symPosition; // 单词表当前下标
    private Word sym; // 当前sym
    private int errorLine; // 错误处理的行号
    private CompUnit root; // 语法树根节点
    private int symbolLayer; // 符号表层级
    private static SymbolTable symTab; // 当前符号表
    private ArrayList<FuncFParam> curFuncFParamList; // 当前函数形参表
    private boolean isLoopStmt; // 是否为循环语句后的Stmt
    private boolean isInVoidFunc; // 是否位于void函数中
    private static final ErrorHandler errorHandler = ErrorHandler.getInstance(); // 错误处理单例
    static {
        symTab = new SymbolTable();
    }
    public SyntaxAnalyzer(StringBuffer source){
        this.LA = new LexicalAnalyzer(source);
        LA.completeAnalyse();
        this.wordList = LA.getWordList();
        this.wordNum = this.wordList.size();
        this.symPosition = 0;
        this.sym = wordList.get(0);
        int errorLine = 1;
        this.root = null;
//        this.symTab = new SymbolTable();
        this.isLoopStmt = false;
        this.curFuncFParamList = null;
        this.isInVoidFunc = false;
    }
    public void getSym(){
        // 参考递归下降法的getSym方法，读取一个sym，并将其值保存在sym中
        // 注意，在本类中此方法外的任意程序片段，访问sym与访问wordList.get(symPosition)等效
        System.out.print(sym);
        sym = wordList.get(++symPosition);
    }
    public void buildSyntaxTree(){ // 核心方法：创建语法树
        this.root = compUnitRec();
    }
    public CompUnit getRoot(){
        if(this.root == null){
            System.out.println("not done yet!");
        }
        return this.root;
    }
    private CompUnit compUnitRec(){ // compUnit递归子程序
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        // Decl 和 FuncDef 都是可选任意次，用List记录
        ArrayList<Decl> declList = new ArrayList<>();
        ArrayList<FuncDef> funcDefList = new ArrayList<>();
        while(symIsDecl()){
            Decl decl = declRec(); // 调用decl递归子程序
            declList.add(decl);
        }
        while(symIsFuncDef()){
            FuncDef funcDef = funcDefRec(); // 调用funcDef递归子程序
            funcDefList.add(funcDef);
        }
        MainFuncDef mainFuncDef = mainFuncDefRec(); // 调用mainFuncDef递归子程序

        CompUnit compUnit = new CompUnit(declList, funcDefList, mainFuncDef);
        compUnit.printSyntax(); // 打印语法成分
        return compUnit;
    }
    private Decl declRec(){
        // Decl不输出：直接区分ConstDecl和VarDecl
        if(symIsConstDecl()){
            // ConstDecl
            ConstDecl constDecl = constDeclRec();
            return new Decl(constDecl);
        }else{
            //VarDecl
            VarDecl varDecl = varDeclRec();
            return new Decl(varDecl);
        }
    }
    private ConstDecl constDeclRec(){ // constDecl递归子程序
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        BType bType;
        ArrayList<ConstDef> constDefList = new ArrayList<>();
        if(sym.getType() == LexicalType.CONSTTK){
            getSym();
            bType = bTypeRec();
            ConstDef constDef = constDefRec();
            constDefList.add(constDef);
            while(sym.getType() == LexicalType.COMMA){
                getSym();
                ConstDef constDef1 = constDefRec();
                constDefList.add(constDef1);
            }
            if(sym.getType() != LexicalType.SEMICN){
                //error(i):分号缺失
                errorLine = wordList.get(symPosition - 1).getLine(); // 分号上一个符号所在行
                errorHandler.handleError(ErrorType.LackSemicolonError, errorLine);
            }else {
                getSym(); // 未缺失，读走分号
            }
            ConstDecl constDecl = new ConstDecl(bType, constDefList);
            constDecl.printSyntax();
            return constDecl;
        }else {
            // Exception
        }
        return null;
    }
    private BType bTypeRec(){
        // BType → 'int'
        getSym();
        return new BType();
    }
    private ConstDef constDefRec(){ // constDef递归子程序
        // ConstDef → Ident { '[' ConstExp ']' } '=' ConstInitVal
        Word ident;
        ArrayList<ConstExp> constExpList = new ArrayList<>();
        ConstInitVal constInitVal;
        if(sym.getType() == LexicalType.IDENFR){ // Ident
            ident = sym;
            getSym();
            int dimension = 0;
            while(sym.getType() == LexicalType.LBRACK){
                getSym();
                ConstExp constExp = constExpRec();
                constExpList.add(constExp);
                if(sym.getType() != LexicalType.RBRACK){
                    // error(j):缺少'k'
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackRBracketError, errorLine);
                }else {
                    getSym();
                }
                dimension ++;
            }
            //TODO:ArrayList length未完成
            if(!declareVar(ident.getToken(), dimension, new ArrayList<>(), true)){ // 填符号表、处理b错误
                // error(b):同名常量重定义
                errorLine = ident.getLine();
                errorHandler.handleError(ErrorType.RedefineError, errorLine);
            }
            if(sym.getType() == LexicalType.ASSIGN){
                getSym();
                constInitVal = constInitValRec();
                ConstDef constDef = new ConstDef(ident, constExpList, constInitVal);
                constDef.printSyntax();
                return constDef;
            }else{
                // Exception
            }
        }else{
            // Exception
        }
        return null;
    }
    private ConstExp constExpRec(){ // constExp递归子程序
        //  ConstExp → AddExp
        AddExp addExp = addExpRec();
        ConstExp constExp = new ConstExp(addExp);
        constExp.printSyntax();
        return constExp;
    }
    private AddExp addExpRec(){ // addExp子程序
        // AddExp → MulExp { ('+' | '−') MulExp }
        ArrayList<MulExp> mulExpList = new ArrayList<>();
        ArrayList<String> opList = new ArrayList<>();
        MulExp mulExp = mulExpRec();
        mulExpList.add(mulExp);
        while(sym.getType() == LexicalType.PLUS || sym.getType() == LexicalType.MINU){
            if(sym.getType() == LexicalType.PLUS){
                opList.add("plus");
            }else{
                opList.add("minu");
            }
            System.out.println("<AddExp>"); // 这里注意，由于消除左递归导致的语法树缺失一层问题
            getSym();
            MulExp mulExp1 = mulExpRec();
            mulExpList.add(mulExp1);
        }
        AddExp ae = new AddExp(mulExpList, opList);
        ae.printSyntax();
        return ae;
    }
    private MulExp mulExpRec(){
        // MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp }
        ArrayList<UnaryExp> unaryExpList = new ArrayList<>();
        ArrayList<String> opList = new ArrayList<>();
        UnaryExp unaryExp = unaryExpRec();
        unaryExpList.add(unaryExp);
        while(sym.getType() == LexicalType.MULT || sym.getType() == LexicalType.DIV || sym.getType() == LexicalType.MOD){
            if(sym.getType() == LexicalType.MULT){
                opList.add("mult");
            } else if(sym.getType() == LexicalType.DIV){
                opList.add("div");
            }else{
                opList.add("mod");
            }
            System.out.println("<MulExp>");
            getSym();
            UnaryExp unaryExp1 = unaryExpRec();
            unaryExpList.add(unaryExp1);
        }
        MulExp me = new MulExp(unaryExpList, opList);
        me.printSyntax();
        return me;
    }
    private UnaryExp unaryExpRec(){
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        PrimaryExp primaryExp;
        Word ident;
        FuncRParams funcRParams;
        UnaryOp unaryOp;
        UnaryExp unaryExp;
        if(sym.getType() == LexicalType.PLUS || sym.getType() == LexicalType.MINU || sym.getType() == LexicalType.NOT){
            // 第三种
            unaryOp = unaryOpRec();
            unaryExp = unaryExpRec();
            UnaryExp ue = new UnaryExp(unaryOp, unaryExp);
            ue.printSyntax();
            ue.setUnaryExpType("signed");
            return ue;
        }else if((sym.getType() == LexicalType.IDENFR || sym.getType() == LexicalType.GETINTTK) && wordList.get(symPosition+1).getType() == LexicalType.LPARENT){
            // 第二种
            ident = sym;
            FuncSymbol fs = null;
            if(searchSymbol(ident.getToken()) == null){
                //error(c):未定义的名字
                errorLine = ident.getLine();
                errorHandler.handleError(ErrorType.UndefineError,errorLine);
            }else {
                if(searchSymbol(ident.getToken()) instanceof FuncSymbol){
                    fs = (FuncSymbol) searchSymbol(ident.getToken());
                }
            }
            getSym();
            getSym(); // 已确定为左括号
            int realParamNum = 0;
            if(sym.getType() == LexicalType.RPARENT){ // 未选FuncRParams
                if(fs != null){
                    if(fs.getParamNum() != realParamNum){
                        //error(d):实参个数!=形参个数
                        errorLine = ident.getLine();
                        errorHandler.handleError(ErrorType.ParameterNumError,errorLine);
                    }
                }
                getSym();
                UnaryExp ue = new UnaryExp(ident);
                ue.printSyntax();
                ue.setUnaryExpType("call");
                return ue;
            }else if(sym.getType() != LexicalType.RPARENT && symNotFirstOfFuncRParams()){
                // 未选FuncRParams，且')'缺失
                if(fs != null){
                    if(fs.getParamNum() != realParamNum){
                        //error(d):实参个数!=形参个数
                        errorLine = ident.getLine();
                        errorHandler.handleError(ErrorType.ParameterNumError,errorLine);
                    }
                }
                errorLine = wordList.get(symPosition - 1).getLine();
                errorHandler.handleError(ErrorType.LackRParentError,errorLine);
                UnaryExp ue = new UnaryExp(ident);
                ue.printSyntax();
                ue.setUnaryExpType("call");
                return ue;
            }else {
                funcRParams = funcRParamsRec();
                realParamNum = funcRParams.paraNum();
                if(fs != null){
                    if(fs.getParamNum() != realParamNum){
                        //error(d):实参个数!=形参个数
                        errorLine = ident.getLine();
                        errorHandler.handleError(ErrorType.ParameterNumError,errorLine);
                    }else{
                        ArrayList<Integer> formatDimensions = fs.getDimensions();
                        ArrayList<Integer> realDimensions = funcRParams.getDimensions();
                        if(!formatDimensions.equals(realDimensions)){
                            //error(e):参数类型不匹配
                            errorLine = ident.getLine();
                            errorHandler.handleError(ErrorType.ParameterTypeError,errorLine);
                        }
                    }
                }
                if(sym.getType() != LexicalType.RPARENT){
                    //error(j):')'缺失
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackRParentError,errorLine);
                }else{
                    getSym();
                }
                UnaryExp ue = new UnaryExp(ident, funcRParams);
                ue.printSyntax();
                ue.setUnaryExpType("call");
                return ue;
            }
        }else {
            // 第一种
            primaryExp = primaryExpRec();
            UnaryExp ue = new UnaryExp(primaryExp);
            ue.printSyntax();
            ue.setUnaryExpType("primary");
            return ue;
        }
    }
    private boolean symNotFirstOfFuncRParams(){
        LexicalType symType = sym.getType();
        return symType != LexicalType.LPARENT &&
                symType != LexicalType.PLUS &&
                symType != LexicalType.MINU &&
                symType != LexicalType.NOT &&
                symType != LexicalType.INTCON &&
                symType != LexicalType.IDENFR;
    }
    private UnaryOp unaryOpRec(){
        // UnaryOp → '+' | '−' | '!'
        if(sym.getType() == LexicalType.PLUS || sym.getType() == LexicalType.MINU || sym.getType() == LexicalType.NOT){
            UnaryOp uo = new UnaryOp(sym);
            getSym();
            uo.printSyntax();
            return uo;
        }else {
            // TODO: error();
        }
        return null;
    }
    private FuncRParams funcRParamsRec(){
        //  FuncRParams → Exp { ',' Exp }
        ArrayList<Exp> expList = new ArrayList<>();
        Exp exp = expRec();
        expList.add(exp);
        while(sym.getType() == LexicalType.COMMA){
            getSym();
            Exp exp1 = expRec();
            expList.add(exp1);
        }
        FuncRParams fr = new FuncRParams(expList);
        fr.printSyntax();
        return fr;
    }
    private Exp expRec(){
        //  Exp → AddExp
        AddExp addExp = addExpRec();
        Exp e = new Exp(addExp);
        e.printSyntax();
        return e;
    }
    private PrimaryExp primaryExpRec(){
        // PrimaryExp → '(' Exp ')' | LVal | Number
        Exp exp;
        LVal lVal;
        Number number;
        if(sym.getType() == LexicalType.LPARENT){
            // 第一种
            getSym();
            exp = expRec();
            if(sym.getType() == LexicalType.RPARENT){
                getSym();
                PrimaryExp pe = new PrimaryExp(exp);
                pe.printSyntax();
                return pe;
            }else{
                // TODO: error();
            }
        }else if(sym.getType() == LexicalType.IDENFR){
            // 第二种
            lVal = lValRec();
            PrimaryExp pe = new PrimaryExp(lVal);
            pe.printSyntax();
            return  pe;
        }else if(sym.getType() == LexicalType.INTCON){
            // 第三种
            number = new Number(sym.getType(), sym.getToken(), sym.getLine());
            getSym();
            PrimaryExp pe = new PrimaryExp(number);
            System.out.println("<Number>");
            pe.printSyntax();
            return  pe;
        }else {
            // TODO: error();
        }
        return null;
    }
    private LVal lValRec(){
        //  LVal → Ident {'[' Exp ']'}
        Word ident;
        ArrayList<Exp> expList = new ArrayList<>();
        if(sym.getType() == LexicalType.IDENFR){
            ident = sym;
            getSym();
            if(searchSymbol(ident.getToken()) == null){
                //error(c):未定义的名字
                errorLine = ident.getLine();
                errorHandler.handleError(ErrorType.UndefineError,errorLine);
            }
            while (sym.getType() == LexicalType.LBRACK){
                getSym();
                Exp exp = expRec();
                expList.add(exp);
                if(sym.getType() == LexicalType.RBRACK){
                    getSym();
                }else {
                    //error(k):']'缺失
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackRBracketError,errorLine);
                }
            }
            LVal lv = new LVal(ident, expList);
            lv.printSyntax();
            return lv;
        }else {
            // Exception
        }
        return null;
    }
    private ConstInitVal constInitValRec(){ // constInitVal子程序
        // ConstInitVal → ConstExp | '{' [ ConstInitVal { ',' ConstInitVal } ] '}'
        ConstExp constExp;
        ArrayList<ConstInitVal> constInitValList = new ArrayList<>();
        if(sym.getType() == LexicalType.LBRACE){ // 选择右边
            getSym();
            if(sym.getType() == LexicalType.RBRACE){ // 未选择[...]部分
                getSym();
                ConstInitVal constInitVal = new ConstInitVal();
                constInitVal.printSyntax();
                return constInitVal;
            }else{
                ConstInitVal constInitVal = constInitValRec();
                constInitValList.add(constInitVal);
                while(sym.getType() == LexicalType.COMMA){
                    getSym();
                    ConstInitVal constInitVal1 = constInitValRec();
                    constInitValList.add(constInitVal1);
                }
                if(sym.getType() == LexicalType.RBRACE){
                    getSym();
                    ConstInitVal ci = new ConstInitVal(constInitValList);
                    ci.printSyntax();
                    return ci;
                }else{
                    // TODO: error();
                }
            }
        }else { // 选择左边
            constExp = constExpRec();
            ConstInitVal ci = new ConstInitVal(constExp);
            ci.printSyntax();
            return ci;
        }
        return null;
    }
    private VarDecl varDeclRec(){ // VarDecl递归子程序
        //  VarDecl → BType VarDef { ',' VarDef } ';'
        BType bType;
        ArrayList<VarDef> varDefList = new ArrayList<>();
        bType = bTypeRec();
        VarDef varDef = varDefRec();
        varDefList.add(varDef);
        while (sym.getType() == LexicalType.COMMA){
            getSym();
            VarDef varDef1 = varDefRec();
            varDefList.add(varDef1);
        }
        if(sym.getType() != LexicalType.SEMICN){
            //error(i):分号缺失
            errorLine = wordList.get(symPosition - 1).getLine();
            errorHandler.handleError(ErrorType.LackSemicolonError, errorLine);
        }else{
            getSym();
        }
        VarDecl varDecl = new VarDecl(bType, varDefList);
        varDecl.printSyntax();
        return varDecl;
    }
    private VarDef varDefRec(){
        // VarDef  → Ident { '[' ConstExp ']' } ( # | '=' InitVal) #表示空串
        Word ident;
        ArrayList<ConstExp> constExpList = new ArrayList<>();
        InitVal initVal;
        if(sym.getType() == LexicalType.IDENFR){
            ident = sym;
            getSym();
            int dimension = 0;
            while(sym.getType() == LexicalType.LBRACK){
                getSym();
                ConstExp constExp = constExpRec();
                constExpList.add(constExp);
                if(sym.getType() == LexicalType.RBRACK){
                    getSym();
                }else {
                    //error(k): 缺少']'
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackRBracketError, errorLine);
                }
                dimension ++;
            }
            //TODO:ArrayList length
            if(!declareVar(ident.getToken(), dimension, new ArrayList<>(), false)){
                //error(b):名字重复定义
                errorLine = ident.getLine();
                errorHandler.handleError(ErrorType.RedefineError,errorLine);
            }
            if(sym.getType() == LexicalType.ASSIGN){ // 有InitVal
                getSym();
                initVal = initValRec();
                VarDef vd = new VarDef(ident, constExpList, initVal);
                vd.printSyntax();
                return vd;
            }else { // 没有InitVal
                VarDef vd = new VarDef(ident, constExpList);
                vd.printSyntax();
                return vd;
            }
        }else {
            // Exception
        }
        return null;
    }
    private InitVal initValRec(){
        // InitVal → Exp | '{' [ InitVal { ',' InitVal } ] '}'
        Exp exp;
        ArrayList<InitVal> initValList = new ArrayList<>();
        if(sym.getType() == LexicalType.LBRACE){
            // 右边
            getSym();
            if(sym.getType() == LexicalType.RBRACE){ // 不选可选项
                getSym();
                InitVal iv = new InitVal();
                iv.printSyntax();
                return iv;
            }else { // 选择可选项
                InitVal initVal = initValRec();
                initValList.add(initVal);
                while(sym.getType() == LexicalType.COMMA){
                    getSym();
                    InitVal initVal1 = initValRec();
                    initValList.add(initVal1);
                }
                if(sym.getType() == LexicalType.RBRACE){
                    getSym();
                    InitVal iv = new InitVal(initValList);
                    iv.printSyntax();
                    return iv;
                }else {
                    // Exception
                }
            }
        }else {
            // 左边
            exp = expRec();
            InitVal iv = new InitVal(exp);
            iv.printSyntax();
            return iv;
        }
        return null;
    }
    private FuncDef funcDefRec(){
        //  FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
        FuncType funcType;
        Word ident;
        FuncFParams funcFParams;
        Block block;
        funcType = funcTypeRec();
        if(sym.getType() == LexicalType.IDENFR){
            ident = sym;
            getSym();
            if(sym.getType() == LexicalType.LPARENT){
                getSym();
                int paramNum = 0;
                if(sym.getType() == LexicalType.RPARENT){
                    //无参数，且')'不缺失
                    if(!declareFunc(ident.getToken(), !funcType.isInt(), paramNum, new ArrayList<>())){
                        //error(b):名字重复定义
                        errorLine = ident.getLine();
                        errorHandler.handleError(ErrorType.RedefineError, errorLine);
                    }
                    getSym();
                    if(!funcType.isInt()){ // void函数内
                        isInVoidFunc = true;
                    }
                    block = blockRec();
                    isInVoidFunc = false;
                    if(funcType.isInt()){
                        // 返回类型为int，检查有无结尾的return
                        if(!block.withReturn()){
                            errorLine = wordList.get(symPosition - 1).getLine();
                            errorHandler.handleError(ErrorType.LackReturnError, errorLine);
                        }
                    }
                    FuncDef fd = new FuncDef(funcType, ident, block);
                    fd.printSyntax();
                    return fd;
                }else if(sym.getType() == LexicalType.LBRACE){
                    //error(j):无参数，且右括号缺失
                    if(!declareFunc(ident.getToken(), !funcType.isInt(), paramNum, new ArrayList<>())){
                        //error(b):名字重复定义
                        errorLine = ident.getLine();
                        errorHandler.handleError(ErrorType.RedefineError, errorLine);
                    }
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackRParentError, errorLine);
                    if(!funcType.isInt()){ // void函数内
                        isInVoidFunc = true;
                    }
                    block = blockRec();
                    isInVoidFunc = false;
                    if(funcType.isInt()){
                        // 返回类型为int，检查有无结尾的return
                        if(!block.withReturn()){
                            errorLine = wordList.get(symPosition - 1).getLine();
                            errorHandler.handleError(ErrorType.LackReturnError, errorLine);
                        }
                    }
                    FuncDef fd = new FuncDef(funcType, ident, block);
                    fd.printSyntax();
                    return fd;
                }else {
                    // 选了FuncFParams
                    funcFParams = funcFParamsRec();
                    paramNum = funcFParams.getParamNum();
                    if(!declareFunc(ident.getToken(), !funcType.isInt(), paramNum, funcFParams.getDimensions())){
                        //error(b):名字重复定义
                        errorLine = ident.getLine();
                        errorHandler.handleError(ErrorType.RedefineError, errorLine);
                    }
                    if(sym.getType() != LexicalType.RPARENT){
                        //error(j):右括号缺失
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackRParentError, errorLine);
                    }else {
                        getSym();
                    }
                    //全局变量传参
                    curFuncFParamList = funcFParams.getFuncFParamList();
                    if(!funcType.isInt()){ // void函数内
                        isInVoidFunc = true;
                    }
                    block = blockRec();
                    isInVoidFunc = false;
                    curFuncFParamList = null;
                    if(funcType.isInt()){
                        // 返回类型为int，检查有无结尾的return
                        if(!block.withReturn()){
                            errorLine = wordList.get(symPosition - 1).getLine();
                            errorHandler.handleError(ErrorType.LackReturnError, errorLine);
                        }
                    }
                    FuncDef fd = new FuncDef(funcType, ident,funcFParams ,block);
                    fd.printSyntax();
                    return fd;
                }
            }else {
                // Exception
            }
        }else {
            // Exception
        }
        return null;
    }
    private FuncType funcTypeRec(){
        // FuncType → 'void' | 'int'
        if(sym.getType() == LexicalType.VOIDTK || sym.getType() == LexicalType.INTTK){
            FuncType ft = new FuncType(sym);
            getSym();
            ft.printSyntax();
            return ft;
        }else {
            // TODO: error();
        }
        return null;
    }
    private Block blockRec(){
        // Block → '{' { BlockItem } '}'
        createSymTab(); // 创建符号表
        if(curFuncFParamList != null){
            // 表示此时block为函数block，需要将curFuncFParams填符号表
            for(FuncFParam ffp:curFuncFParamList){
                // TODO:ArrayList length
                if(!declareVar(ffp.getIdent().getToken(), ffp.getDimension(), new ArrayList<>(), false)){
                    // error(b):形参重复定义
                    errorLine = ffp.getIdent().getLine();
                    errorHandler.handleError(ErrorType.RedefineError, errorLine);
                }
            }
        }
        ArrayList<BlockItem> blockItemList = new ArrayList<>();
        if(sym.getType() == LexicalType.LBRACE){
            getSym();
            while (sym.getType() != LexicalType.RBRACE){
                BlockItem blockItem = blockItemRec();
                blockItemList.add(blockItem);
            }
            if(sym.getType() == LexicalType.RBRACE){
                getSym();
                exitSymTab(); // 退出当前层回到上一层符号表
                Block b = new Block(blockItemList);
                b.printSyntax();
                return b;
            }else {
                // Exception
            }
        }else {
            // Exception
        }
        return null;
    }
    private BlockItem blockItemRec(){
        // BlockItem → Decl | Stmt
        Decl decl;
        Stmt stmt;
        if(symIsDecl()){ // Decl
            decl = declRec();
            BlockItem bi = new BlockItem(decl);
            return bi;
        }else { // Stmt
            stmt = stmtRec();
            BlockItem bi = new BlockItem(stmt);
            return bi;
        }
    }
    private Stmt stmtRec(){
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
        Stmt stmt;
        Stmt elseStmt;
        ForStmt forStmt1;
        ForStmt forStmt2;
        Word forTerminal;
        Word formatString;
        ArrayList<Exp> expList = new ArrayList<>();
        if(sym.getType() == LexicalType.LBRACE) { // Block
            block = blockRec();
            Stmt s = new Stmt(block);
            s.printSyntax();
            s.setStmtType("block");
            return s;
        }else if(sym.getType() == LexicalType.IFTK){ // 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
            getSym(); // 已确定sym为'if'
            if(sym.getType() == LexicalType.LPARENT){
                getSym();
                cond = condRec();
                if(sym.getType() != LexicalType.RPARENT){
                    //error(j):')'缺失
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackRParentError,errorLine);
                }else {
                    getSym();
                }
                stmt = stmtRec();
                if(sym.getType() == LexicalType.ELSETK){ // 选择了else可选项
                    getSym();
                    elseStmt = stmtRec();
                    Stmt s = new Stmt(cond, stmt, elseStmt);
                    s.printSyntax();
                    s.setStmtType("branchWithElse");
                    return s;
                }
                Stmt s = new Stmt(cond, stmt);
                s.printSyntax();
                s.setStmtType("branchWithoutElse");
                return s;
            }else {
                // Exception
            }
        }else if(sym.getType() == LexicalType.FORTK){ // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
            getSym();
            if(sym.getType() == LexicalType.LPARENT){
                getSym();
                if(sym.getType() != LexicalType.SEMICN){ // 选择第一个ForStmt
                    forStmt1 = forStmtRec();
                }else forStmt1 = null;
                if(sym.getType() == LexicalType.SEMICN){
                    getSym();
                    if(sym.getType() != LexicalType.SEMICN){ // 选择Cond
                        cond = condRec();
                    }else cond = null;
                    if(sym.getType() == LexicalType.SEMICN){
                        getSym();
                        if(sym.getType() != LexicalType.RPARENT){ // 选择第二个ForStmt
                            forStmt2 = forStmtRec();
                        }else forStmt2 = null;
                        if(sym.getType() == LexicalType.RPARENT){
                            getSym();
                            isLoopStmt = true; // 循环的Stmt语句
                            stmt = stmtRec();
                            isLoopStmt = false; // 退出循环的Stmt语句
                            Stmt s = new Stmt(forStmt1, cond, forStmt2, stmt);
                            s.printSyntax();
                            s.setStmtType("loop");
                            return s;
                        }else {
                            // Exception
                        }
                    }else {
                        // Exception
                    }
                }else {
                    // Exception
                }
            }else {
                // Exception
            }
        }else if(sym.getType() == LexicalType.BREAKTK || sym.getType() == LexicalType.CONTINUETK){ // 'break' ';' | 'continue' ';'
            forTerminal = sym;
            getSym();
            if(!isLoopStmt){
                //error(m):forTerminal不在循环block之中
                errorLine = forTerminal.getLine();
                errorHandler.handleError(ErrorType.LoopStmtError,errorLine);
            }
            if(sym.getType() != LexicalType.SEMICN){
                //error(i):';'缺失
                errorLine = wordList.get(symPosition - 1).getLine();
                errorHandler.handleError(ErrorType.LackSemicolonError,errorLine);
            }else {
                getSym();
            }
            Stmt s = new Stmt(forTerminal);
            s.printSyntax();
            if(forTerminal.getType() == LexicalType.BREAKTK){
                s.setStmtType("break");
            }else{
                s.setStmtType("continue");
            }
            return s;
        }else if(sym.getType() == LexicalType.RETURNTK){ // 'return' [Exp] ';'
            errorLine = sym.getLine();
            getSym();
            if(sym.getType() == LexicalType.SEMICN){ // 未选Exp
                getSym();
                Stmt s = new Stmt();
                s.printSyntax();
                s.setStmtType("returnVoid");
                s.setReturn(); // 错误处理g使用
                return s;
            }else {
                // 选择Exp
                exp = expRec();
                if(isInVoidFunc){
                    //error(f):在void函数内选择了Exp
                    errorHandler.handleError(ErrorType.UnmatchedReturnError,errorLine);
                }
                if(sym.getType() != LexicalType.SEMICN){
                    //error(i):';'缺失
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackSemicolonError,errorLine);
                }else {
                    getSym();
                }
                Stmt s = new Stmt(exp);
                s.printSyntax();
                s.setStmtType("returnWithExp");
                s.setReturn(); // 错误处理g使用
                return s;
            }
        }else if(sym.getType() == LexicalType.PRINTFTK){ // 'printf''('FormatString{','Exp}')'';'
            errorLine = sym.getLine();
            getSym();
            if(sym.getType() == LexicalType.LPARENT){
                getSym();
                if(sym.getType() == LexicalType.STRCON){
                    formatString = sym;
                    checkFormatStringError(formatString);
                    getSym();
                    int formatCharNum = formatCharNum(formatString);
                    int expNum = 0;
                    while (sym.getType() == LexicalType.COMMA){
                        getSym();
                        Exp exp1 = expRec();
                        expList.add(exp1);
                        expNum ++;
                    }
                    if(formatCharNum != expNum){
                        //error(l):格式字符数和表达式数不匹配
                        errorHandler.handleError(ErrorType.UnmatchedFormatCharError,errorLine);
                    }
                    if(sym.getType() != LexicalType.RPARENT){
                        //error(j):')'缺失；
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackRParentError,errorLine);
                    }else {
                        getSym();
                    }
                    if(sym.getType() != LexicalType.SEMICN){
                        //error(i):';'缺失
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackSemicolonError,errorLine);
                    }else {
                        getSym();
                    }
                    Stmt s = new Stmt(formatString, expList);
                    s.printSyntax();
                    s.setStmtType("printf");
                    return s;
                }
            }else {
                // Exception
            }
        }else if(symIsLVal()){
            // LVal '=' Exp ';' | LVal '=' 'getint''('')'';'
            lVal = lValRec();
            if(sym.getType() == LexicalType.ASSIGN){
                getSym();
            }else{
                // Exception
            }
            Symbol lValSymbol;
            if((lValSymbol = searchSymbol(lVal.getIdent().getToken())) != null){ // 找到lVal所填符号（如果没填过应该在lValRec中已经报错error(c)）
                if(lValSymbol instanceof VarSymbol lValVarSymbol){
                    if(lValVarSymbol.varIsConst()){
                        //error(h):lVal作为常量被改变值
                        errorLine = lVal.getIdent().getLine();
                        errorHandler.handleError(ErrorType.ChangeConstError,errorLine);
                    }
                }
            }
            if(sym.getType() == LexicalType.GETINTTK){
                //getint
                getSym();
                if(sym.getType() == LexicalType.LPARENT){
                    getSym();
                    if(sym.getType() != LexicalType.RPARENT){
                        //error(j):')'缺失
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackRParentError,errorLine);
                    }else {
                        getSym();
                    }
                    if(sym.getType() != LexicalType.SEMICN){
                        //error(i):';'缺失
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackSemicolonError,errorLine);
                    }else {
                        getSym();
                    }
                    Stmt s = new Stmt(lVal);
                    s.printSyntax();
                    s.setStmtType("assign_getint");
                    return s;
                }else {
                    // Exception
                }
            }else {
                //Exp
                exp = expRec();
                if(sym.getType() != LexicalType.SEMICN){
                    //error(i):';'缺失
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackSemicolonError,errorLine);
                }else {
                    getSym();
                }
                Stmt s = new Stmt(lVal, exp);
                s.printSyntax();
                s.setStmtType("assign");
                return s;
            }
        }else { //  [Exp] ';'
            if(sym.getType() == LexicalType.SEMICN){ // 未选Exp
                getSym();
                Stmt s = new Stmt();
                s.printSyntax();
                s.setStmtType("exp");
                return s;
            }else {
                // 选择Exp
                exp = expRec();
                if(sym.getType() == LexicalType.SEMICN){
                    getSym();
                    Stmt s = new Stmt(exp);
                    s.printSyntax();
                    s.setStmtType("exp");
                    return s;
                }else {
                    // TODO: error();
                }
            }
        }
        return null;
    }
    private Cond condRec(){
        // // Cond → LOrExp
        LOrExp lOrExp = lOrExpRec();
        Cond c = new Cond(lOrExp);
        c.printSyntax();
        return c;
    }
    private LOrExp lOrExpRec(){
        // LOrExp → LAndExp { '||' LAndExp }
        ArrayList<LAndExp> lAndExpList = new ArrayList<>();
        LAndExp lAndExp = lAndExpRec();
        lAndExpList.add(lAndExp);
        while(sym.getType() == LexicalType.OR){
            System.out.println("<LOrExp>");
            getSym();
            LAndExp lAndExp1 = lAndExpRec();
            lAndExpList.add(lAndExp1);
        }
        LOrExp loe = new LOrExp(lAndExpList);
        loe.printSyntax();
        return loe;
    }
    private LAndExp lAndExpRec(){
        // LAndExp → EqExp { '&&' EqExp }
        ArrayList<EqExp> eqExpList = new ArrayList<>();
        EqExp eqExp = eqExpRec();
        eqExpList.add(eqExp);
        while(sym.getType() == LexicalType.AND){
            System.out.println("<LAndExp>");
            getSym();
            EqExp eqExp1 = eqExpRec();
            eqExpList.add(eqExp1);
        }
        LAndExp lae = new LAndExp(eqExpList);
        lae.printSyntax();
        return lae;
    }
    private EqExp eqExpRec(){
        // EqExp → RelExp { ('==' | '!=') RelExp }
        ArrayList<RelExp> relExpList = new ArrayList<>();
        ArrayList<String> opList = new ArrayList<>();
        RelExp relExp = relExpRec();
        relExpList.add(relExp);
        while (sym.getType() == LexicalType.EQL || sym.getType() == LexicalType.NEQ){
            if(sym.getType() == LexicalType.EQL){
                opList.add("eq");
            }else{
                opList.add("ne");
            }
            System.out.println("<EqExp>");
            getSym();
            RelExp relExp1 = relExpRec();
            relExpList.add(relExp1);
        }
        EqExp ee = new EqExp(relExpList, opList);
        ee.printSyntax();
        return ee;
    }
    private RelExp relExpRec(){
        // RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp }
        ArrayList<AddExp> addExpList = new ArrayList<>();
        ArrayList<String> opList = new ArrayList<>();
        AddExp addExp = addExpRec();
        addExpList.add(addExp);
        while (sym.getType() == LexicalType.LSS || sym.getType() == LexicalType.LEQ || sym.getType() == LexicalType.GRE || sym.getType() == LexicalType.GEQ){
            if(sym.getType() == LexicalType.LSS){
                opList.add("slt");
            }else if(sym.getType() == LexicalType.LEQ){
                opList.add("sle");
            }else if(sym.getType() == LexicalType.GRE){
                opList.add("sgt");
            }else{
                opList.add("sge");
            }
            System.out.println("<RelExp>");
            getSym();
            AddExp addExp1 = addExpRec();
            addExpList.add(addExp1);
        }
        RelExp re = new RelExp(addExpList, opList);
        re.printSyntax();
        return re;
    }
    private ForStmt forStmtRec(){
        // // ForStmt → LVal '=' Exp
        LVal lVal;
        Exp exp;
        lVal = lValRec();
        Symbol lValSymbol;
        if((lValSymbol = searchSymbol(lVal.getIdent().getToken())) != null){ // 找到lVal所填符号（如果没填过应该在lValRec中已经报错error(c)）
            if(lValSymbol instanceof VarSymbol lValVarSymbol){
                if(lValVarSymbol.varIsConst()){
                    //error(h):lVal作为常量被改变值
                    errorLine = lVal.getIdent().getLine();
                    errorHandler.handleError(ErrorType.ChangeConstError,errorLine);
                }
            }
        }
        if(sym.getType() == LexicalType.ASSIGN){
            getSym();
            exp = expRec();
            ForStmt fs = new ForStmt(lVal, exp);
            fs.printSyntax();
            return fs;
        }else {
            // Exception
        }
        return null;
    }
    private FuncFParams funcFParamsRec(){
        // FuncFParams → FuncFParam { ',' FuncFParam }
        ArrayList<FuncFParam> funcFParamList = new ArrayList<>();
        FuncFParam funcFParam = funcFParamRec();
        funcFParamList.add(funcFParam);
        while (sym.getType() == LexicalType.COMMA){
            getSym();
            FuncFParam funcFParam1 = funcFParamRec();
            funcFParamList.add(funcFParam1);
        }
        FuncFParams ffps = new FuncFParams(funcFParamList);
        ffps.printSyntax();
        return ffps;
    }
    private FuncFParam funcFParamRec(){
        // FuncFParam → BType Ident ['[' ']' { '[' ConstExp ']' }]
        BType bType;
        Word ident;
        ArrayList<ConstExp> constExpList = new ArrayList<>();
        bType = bTypeRec();
        if(sym.getType() == LexicalType.IDENFR){
            ident = sym;
            getSym();
            int dimension = 0; // 默认为普通变量
            if(sym.getType() == LexicalType.LBRACK){ // 选择可选项
                getSym();
                dimension ++;
                if(sym.getType() != LexicalType.RBRACK){
                    //error(k):']'缺失
                    errorLine = wordList.get(symPosition - 1).getLine();
                    errorHandler.handleError(ErrorType.LackRBracketError,errorLine);
                }else{
                    getSym();
                }
                while(sym.getType() == LexicalType.LBRACK){
                    getSym();
                    dimension ++;
                    ConstExp constExp = constExpRec();
                    constExpList.add(constExp);
                    if(sym.getType() != LexicalType.RBRACK){
                        //error(k):']'缺失
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackRBracketError,errorLine);
                    }else {
                        getSym();
                    }
                }
                FuncFParam ffp = new FuncFParam(bType, ident, constExpList);
                ffp.setDimension(dimension);
                ffp.printSyntax();
                return ffp;
            }else { // 不选可选项：普通变量
                FuncFParam ffp = new FuncFParam(bType, ident);
                ffp.printSyntax();
                return ffp;
            }
        }else {
            // Exception
        }
        return null;
    }
    private MainFuncDef mainFuncDefRec(){
        //  MainFuncDef → 'int' 'main' '(' ')' Block
        Block block;
        if(sym.getType() == LexicalType.INTTK){
            getSym();
            if(sym.getType() == LexicalType.MAINTK){
                getSym();
                if(sym.getType() == LexicalType.LPARENT){
                    getSym();
                    if(sym.getType() != LexicalType.RPARENT){
                        //error(j): ')'缺失
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackRParentError, errorLine);
                    }else {
                        getSym();
                    }
                    block = blockRec();
                    if(!block.withReturn()){
                        errorLine = wordList.get(symPosition - 1).getLine();
                        errorHandler.handleError(ErrorType.LackReturnError, errorLine);
                    }
                    MainFuncDef mfd = new MainFuncDef(block);
                    mfd.printSyntax();
                    return mfd;
                }else {
                    // Exception
                }
            }else {
                // Exception
            }
        }else {
            // Exception
        }
        return null;
    }

    //辅助判断方法
    private boolean symIsDecl(){ // 判断sym是否为Decl：终结串首符号集FIRST
        return symIsConstDecl() || symIsVarDecl();
    }
    private boolean symIsConstDecl(){
        // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
        return sym.getType() == LexicalType.CONSTTK;
    }
    private boolean symIsVarDecl(){
        Word sym = wordList.get(symPosition);
        Word symNext1 = wordList.get(symPosition + 1);
        Word symNext2 = wordList.get(symPosition + 2);
        return sym.getType() == LexicalType.INTTK && symNext1.getType() == LexicalType.IDENFR && symNext2.getType() != LexicalType.LPARENT;
    }
    private boolean symIsFuncDef(){
        Word sym = wordList.get(symPosition);
        Word symNext1 = wordList.get(symPosition + 1);
        Word symNext2 = wordList.get(symPosition + 2);
        return sym.getType() == LexicalType.VOIDTK || (sym.getType() == LexicalType.INTTK && symNext1.getType() == LexicalType.IDENFR && symNext2.getType() == LexicalType.LPARENT);
    }
    private boolean symIsLVal(){
        int tempPos = symPosition;
        while(wordList.get(tempPos).getType() != LexicalType.SEMICN){
            if(wordList.get(tempPos).getType() == LexicalType.ASSIGN){
                return true;
            }
            tempPos ++;
        }
        return false;
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
    public boolean declareVar(String token, int dimension, ArrayList<Integer> length, boolean isConst){ // 声明符号：填表
        if(symHadDeclared(token)){
            return false;
        }
        VarSymbol vs = new VarSymbol(token, symTab.getLayer(), dimension, length, isConst);
        symTab.getTable().put(token,vs);
        return true;
    }
    public boolean declareFunc(String token, boolean isVoidFunc, int paramNum, ArrayList<Integer> dimensions){
        if(symHadDeclared(token)){
            return false;
        }
        FuncSymbol fs = new FuncSymbol(token, symTab.getLayer(), isVoidFunc, paramNum, dimensions);
        symTab.getTable().put(token,fs);
        return true;
    }
    public static Symbol searchSymbol(String token){ // 从当前层开始逐层寻找符号，若存在（本层或外层声明过）则返回
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
    public int formatCharNum(Word formatString){
        int formatChar = 0;
        for(int index = 0;index < formatString.getToken().length() - 1;index ++){
            if(formatString.getToken().toCharArray()[index] == '%' && formatString.getToken().toCharArray()[index + 1] == 'd'){
                formatChar ++;
            }
        }
        return formatChar;
    }
    public void checkFormatStringError(Word formatString){
        String strToken = formatString.getToken();
        boolean haveHandled = false;
        for(int index = 1;index < strToken.length() - 1;index ++){
            if(!(strToken.charAt(index) == '%' || strToken.charAt(index) == 32 || strToken.charAt(index) == 33 || (strToken.charAt(index) >= 40 && strToken.charAt(index) <= 126))){
                if(!haveHandled){
                    errorHandler.handleError(ErrorType.IllegalSymError, formatString.getLine());
                    haveHandled = true;
                }
            } else if(strToken.charAt(index) == 92){
                if(strToken.charAt(index + 1) != 'n'){
                    // 格式字符串非法字符（'\'单独出现）
                    if(!haveHandled){
                        errorHandler.handleError(ErrorType.IllegalSymError, formatString.getLine());
                        haveHandled = true;
                    }
                }
            }else if(strToken.charAt(index) == '%'){
                //格式字符
                if(strToken.charAt(index + 1) != 'd'){
                    if(!haveHandled){
                        errorHandler.handleError(ErrorType.IllegalSymError, formatString.getLine());
                        haveHandled = true;
                    }
                }
            }
        }
    }
}
