import LexicalAnalyse.IOInterface;
import LexicalAnalyse.LexicalAnalyzer;

import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {

        StringBuffer source = new StringBuffer();
        IOInterface.fileToStr("testfile.txt",source);
        IOInterface.setOutToFile("output.txt");

        LexicalAnalyzer LA = new LexicalAnalyzer(source);
        LA.completeAnalyse();
    }
}