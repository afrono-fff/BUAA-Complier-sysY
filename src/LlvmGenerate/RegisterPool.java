package LlvmGenerate;

public class RegisterPool {
    int number;
    public RegisterPool(){
        number = 0;
    }
    public String distributeRegister(){
        return "%r" + number++ ;
    }
}
