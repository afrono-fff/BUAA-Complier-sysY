package LexicalAnalyse;

import java.util.ArrayList;
import java.util.HashMap;

public class LexicalAnalyzer {
    private StringBuffer source; //源码字符串
    private int position; //位置指针
    private int line; //当前行号
    private int sourceLen; //源码长度
    private String token; //当前token
    private LexicalType type; //当前type
    private ArrayList<Word> wordList; //单词表
    private static final HashMap<String, LexicalType> reservedWords = new HashMap<>();
    //保留字表
    static {
        //静态代码区初始化static final的HashMap
        reservedWords.put("main",LexicalType.MAINTK);
        reservedWords.put("const",LexicalType.CONSTTK);
        reservedWords.put("int",LexicalType.INTTK);
        reservedWords.put("break",LexicalType.BREAKTK);
        reservedWords.put("continue",LexicalType.CONTINUETK);
        reservedWords.put("if",LexicalType.IFTK);
        reservedWords.put("else",LexicalType.ELSETK);
        reservedWords.put("for",LexicalType.FORTK);
        reservedWords.put("getint",LexicalType.GETINTTK);
        reservedWords.put("printf",LexicalType.PRINTFTK);
        reservedWords.put("return",LexicalType.RETURNTK);
        reservedWords.put("void",LexicalType.VOIDTK);
    }
    public LexicalAnalyzer(StringBuffer source){
        this.source = source;
        this.position = 0;
        this.line = 1;
        this.sourceLen = source.length();
        this.wordList = new ArrayList<>();
        this.token = "";
        this.type = LexicalType.NONE;
    }
    public Word next(){
        //提供一次解析，返回一个单词
        token = ""; // 清空token
        char readChar = source.charAt(position++); //读取一个字符
        while(readChar == ' ' || readChar == '\t'){ //吃空格
            readChar = source.charAt(position++);
        }
        if(readChar == '\n'){ // 一般换行
            line++;
            if(position < sourceLen)return next();
            else return new Word(LexicalType.NONE,"",0);
        }
        if(readChar == '\r'){
            readChar = source.charAt(position++);
        }
        if(charIsIdent(readChar)){ // 标识符和保留字
            token += readChar;
            while(position < sourceLen && (charIsIdent(source.charAt(position)) || charIsDigit(source.charAt(position)))){
                readChar = source.charAt(position++);
                token += readChar;
            }
            if(isReserve(token)){
                return new Word(reservedWords.get(token),token,line);
            }else{
                return new Word(LexicalType.IDENFR,token,line);
            }
        }else if(charIsDigit(readChar)){ //无符号整数
            token += readChar;
            while(position < sourceLen && charIsDigit(source.charAt(position))){
                readChar = source.charAt(position++);
                token += readChar;
            }
            return new Number(LexicalType.INTCON,token,line,Integer.parseInt(token));
        }else if(readChar == '/'){ // 注释
            token += readChar;
            if(position < sourceLen && source.charAt(position) == '/'){ // 单行注释，第二个'/'
                readChar = source.charAt(position++);
                token += readChar;
                while(position < sourceLen && source.charAt(position) != '\n'){
                    readChar = source.charAt(position++);
                    token += readChar;
                }
                if(position < sourceLen){
                    readChar = source.charAt(position++);
                    token += readChar;
                    line ++;
                }
                type = LexicalType.NOTE;
                return next(); // 注释类型为NONE没有返回，下一个
            }else if(position < sourceLen && source.charAt(position) == '*'){ //多行注释
                readChar = source.charAt(position++);
                token += readChar;
                while(position < sourceLen) {  // 状态转换循环：有可能出现非注释结尾的'*'
                    while (position < sourceLen && source.charAt(position) != '*') { // 注释内容
                        readChar = source.charAt(position++);
                        token += readChar;
                        if(readChar == '\n') line++; // 每行最后的回车
                    }
                    while(position < sourceLen && source.charAt(position) == '*') { // 读走所有出现'*'
                        readChar = source.charAt(position++);
                        token += readChar;
                    }
                    if (position < sourceLen && source.charAt(position) == '/') { //若最后一个'*'后直接出现'/'
                        // /字符 对应状态q7
                        readChar = source.charAt(position++);
                        token += readChar;
                        type = LexicalType.NOTE;
                        return next();
                    }
                }
            }else{ // DIV
                return new Word(LexicalType.DIV,token,line);
            }
        }else if(readChar == '"'){
            token += readChar;
            while(position < sourceLen && source.charAt(position) != '"'){
                readChar = source.charAt(position++);
                token += readChar;
            }
            if(source.charAt(position) == '"'){
                readChar = source.charAt(position++);
                token += readChar;
                return new Word(LexicalType.STRCON, token, line);
            }
        }else if(readChar == '!'){ // NOT 和 NEQ
            token += readChar;
            if(position < sourceLen && source.charAt(position) == '='){ // NEQ
                readChar = source.charAt(position++);
                token += readChar;
                return new Word(LexicalType.NEQ, token, line);
            }else{
                //若无error，为NOT
                return new Word(LexicalType.NOT, token, line);
            }
        }else if(readChar == '&'){ // AND
            token += readChar;
            if(position < sourceLen && source.charAt(position) == '&') { // 词法正确
                readChar = source.charAt(position++);
                token += readChar;
                return new Word(LexicalType.AND, token, line);
            }
        }else if(readChar == '|'){ // OR
            token += readChar;
            if(position < sourceLen && source.charAt(position) == '|') { // 词法正确
                readChar = source.charAt(position++);
                token += readChar;
                return new Word(LexicalType.OR, token, line);
            }
        }else if(readChar == '+') { // PLUS
            token += readChar;
            return new Word(LexicalType.PLUS,token,line);
        }else if(readChar == '-') { // PLUS
            token += readChar;
            return new Word(LexicalType.MINU,token,line);
        }else if(readChar == '*'){ // MULT
            token += readChar;
            return new Word(LexicalType.MULT,token,line);
        }else if(readChar == '%'){ // MOD
            token += readChar;
            return new Word(LexicalType.MOD,token,line);
        }else if(readChar == '<'){ // LSS 和 LEQ
            token += readChar;
            if(position < sourceLen && source.charAt(position) == '='){ // LEQ
                readChar = source.charAt(position++);
                token += readChar;
                return new Word(LexicalType.LEQ,token,line);
            }
            return new Word(LexicalType.LSS,token,line);
        }else if(readChar == '>'){ // GRE 和 GEQ
            token += readChar;
            if(position < sourceLen && source.charAt(position) == '='){ // GEQ
                readChar = source.charAt(position++);
                token += readChar;
                return new Word(LexicalType.GEQ,token,line);
            }
            return new Word(LexicalType.GRE,token,line);
        }else if(readChar == '='){ // ASSIGN 和 EQL
            token += readChar;
            if(position < sourceLen && source.charAt(position) == '='){ // EQL
                readChar = source.charAt(position++);
                token += readChar;
                return new Word(LexicalType.EQL,token,line);
            }
            return new Word(LexicalType.ASSIGN,token,line);
        }else if(readChar == ';'){ // SEMICN
            token += readChar;
            return new Word(LexicalType.SEMICN,token,line);
        }else if(readChar == ','){ // COMMA
            token += readChar;
            return new Word(LexicalType.COMMA,token,line);
        }else if(readChar == '('){ // LPARENT
            token += readChar;
            return new Word(LexicalType.LPARENT,token,line);
        }else if(readChar == ')'){ // RPARENT
            token += readChar;
            return new Word(LexicalType.RPARENT,token,line);
        }else if(readChar == '['){ // LBRACK
            token += readChar;
            return new Word(LexicalType.LBRACK,token,line);
        }else if(readChar == ']'){ // RBRACK
            token += readChar;
            return new Word(LexicalType.RBRACK,token,line);
        }else if(readChar == '{'){ // LBRACE
            token += readChar;
            return new Word(LexicalType.LBRACE,token,line);
        }else if(readChar == '}'){ // RBRACE
            token += readChar;
            return new Word(LexicalType.RBRACE,token,line);
        }
        return null;
    }
    public void completeAnalyse(){
        //完成对源程序的完全词法解析，并将全部单词放入单词表
        int scanP = position; // 记录position
        position = 0; // 位置指针移到source开头
        while(position < sourceLen){
            wordList.add(next());
        }
        position = scanP;
    }
    public void printToFile(){
        if(wordList.isEmpty())return;
        for (Word word: wordList){
            System.out.print(word);
        }
    }
    private boolean charIsIdent(char ch){
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch == '_';
    }
    private boolean charIsDigit(char ch){
        return ch >= '0' && ch <= '9';
    }
    private boolean isReserve(String token){
        return reservedWords.containsKey(token);
    }
}
