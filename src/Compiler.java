import ErrorHandle.ErrorType;
import LexicalAnalyse.IOInterface;
import LexicalAnalyse.LexicalAnalyzer;
import SyntaxAnalyse.SyntaxAnalyzer;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        StringBuffer source = new StringBuffer(); //源文件接收串

        IOInterface.fileToStrB("testfile.txt",source); // 将源文件转为动态字符串
        IOInterface.clearError(); // 清除error.txt文件
        SyntaxAnalyzer SA = new SyntaxAnalyzer(source); // 语法分析器
        IOInterface.setOutToFile("output.txt");
        SA.buildSyntaxTree(); // 构建语法树
        IOInterface.setOutToConsole();

        System.out.println("done!");
    }
}