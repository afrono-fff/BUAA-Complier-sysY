import ErrorHandle.ErrorType;
import LexicalAnalyse.IOInterface;
import LexicalAnalyse.LexicalAnalyzer;
import LlvmGenerate.LlvmGenerator;
import SyntaxAnalyse.SyntaxAnalyzer;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        StringBuffer source = new StringBuffer(); //源文件接收串

        IOInterface.fileToStrB("testfile.txt",source); // 将源文件转为动态字符串
        IOInterface.clearError(); // 清除error.txt文件
        SyntaxAnalyzer SA = new SyntaxAnalyzer(source); // 语法分析器
        IOInterface.setOutToFile("output.txt");
        SA.buildSyntaxTree(); // 构建语法树，检查语法错误
        IOInterface.setOutToConsole();

        if(IOInterface.checkError()){ // error.txt文件没有内容的情况下，进行llvm代码生成
            //llvm代码生成
            IOInterface.setOutToFile("llvm_ir.txt");
            LlvmGenerator LG = new LlvmGenerator(SA);
            LG.traverse();
            IOInterface.setOutToConsole();
        }
        System.out.println("done!");
    }
}