import LexicalAnalyse.IOInterface;
import LexicalAnalyse.LexicalAnalyzer;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        StringBuffer source = new StringBuffer(); //源文件接收串

        IOInterface.fileToStrB("testfile.txt",source); // 将源文件转为动态字符串

        IOInterface.setOutToFile("output.txt"); // 输出重定向至输出文件
        LexicalAnalyzer LA = new LexicalAnalyzer(source);
        LA.completeAnalyse(); // 调用词法分析接口
        LA.printToFile(); //打印词法分析结果
        IOInterface.setOutToConsole(); // 输出重定向至控制台
        System.out.println("done!");
    }
}