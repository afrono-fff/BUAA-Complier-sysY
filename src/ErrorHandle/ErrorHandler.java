package ErrorHandle;

import LexicalAnalyse.IOInterface;

import java.io.FileNotFoundException;

public class ErrorHandler {
    private ErrorHandler(){}
    private static final ErrorHandler Single = new ErrorHandler();
    public static ErrorHandler getInstance(){
        return Single;
    }
    public void handleError(ErrorType errorType,int line) {
        try {
            IOInterface.setOutToError(); // 打印错误准备
            System.out.println(line+" "+errorType.toString());
            IOInterface.setOutBack(); // 关闭错误打印
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
