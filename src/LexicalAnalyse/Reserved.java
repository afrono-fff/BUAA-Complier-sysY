package LexicalAnalyse;

import java.util.HashMap;

public class Reserved extends Word{
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
    public Reserved(LexicalType type, String token, int line) {
        super(type, token, line);

    }
}
