package LexicalAnalyse;

import java.io.*;

public class IOInterface {
    public static boolean fileToStr(String pathName,StringBuffer buffer) throws IOException {
        BufferedReader bf = new BufferedReader(new FileReader(pathName));
        String read;
        while((read = bf.readLine()) != null){
            buffer.append(read);
            buffer.append('\n');
        }
        //将源文件内容读入字符串buffer
        return true;
    }
    public static boolean setOutToFile(String output) throws FileNotFoundException {
        PrintStream ps=new PrintStream(new FileOutputStream(output));
        System.setOut(ps);
        return true;
    }
}
