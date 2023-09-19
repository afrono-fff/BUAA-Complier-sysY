package LexicalAnalyse;

import java.io.*;

public class IOInterface {
    private static final PrintStream console = System.out; // 记录标准输出流：控制台
    public static void fileToStrB(String pathName, StringBuffer buffer) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(pathName));
        String read;
        while((read = bf.readLine()) != null){
            buffer.append(read);
            buffer.append('\n');
        }
        //将源文件内容读入字符串buffer
    }
    public static void setOutToFile(String output) throws FileNotFoundException {
        PrintStream ps=new PrintStream(new FileOutputStream(output));
        System.setOut(ps);
    }
    public static void setOutToConsole(){
        System.setOut(console);
    }
}
