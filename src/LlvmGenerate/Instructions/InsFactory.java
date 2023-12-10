package LlvmGenerate.Instructions;

import LlvmGenerate.BasicBlock;
import LlvmGenerate.Instructions.Calculations.*;
import LlvmGenerate.Definitions.Function;
import LlvmGenerate.Definitions.GlobalDecl;
import LlvmGenerate.Definitions.Parameter;
import LlvmGenerate.Instructions.Jump.BrIns;
import LlvmGenerate.Instructions.Jump.CallIns;
import LlvmGenerate.Instructions.Jump.RetIns;
import LlvmGenerate.Instructions.Memories.*;
import LlvmGenerate.MiddleVal;
import LlvmGenerate.RegisterPool;
import SymbolTable.VarSymbol;

import java.util.ArrayList;

public class InsFactory {
    RegisterPool registerPool;
    public InsFactory(){
        this.registerPool = new RegisterPool();
    }
    public AddIns generateAdd(BasicBlock basicBlock, MiddleVal L, MiddleVal R){
        String regName = registerPool.distributeRegister(); // 分配寄存器
        AddIns addIns = new AddIns(basicBlock, regName, "i32", L.toString(), R.toString());
        basicBlock.addIns(addIns);
        return addIns;
    }
    public SubIns generateSub(BasicBlock basicBlock, MiddleVal L, MiddleVal R){
        String regName = registerPool.distributeRegister(); // 分配寄存器
        SubIns subIns = new SubIns(basicBlock, regName, "i32", L.toString(), R.toString());
        basicBlock.addIns(subIns);
        return subIns;
    }
    public MulIns generateMul(BasicBlock basicBlock, MiddleVal L, MiddleVal R){
        String regName = registerPool.distributeRegister(); // 分配寄存器
        MulIns mulIns = new MulIns(basicBlock, regName, "i32", L.toString(), R.toString());
        basicBlock.addIns(mulIns);
        return mulIns;
    }
    public SdivIns generateSdiv(BasicBlock basicBlock, MiddleVal L, MiddleVal R){
        String regName = registerPool.distributeRegister(); // 分配寄存器
        SdivIns sdivIns = new SdivIns(basicBlock, regName, "i32", L.toString(), R.toString());
        basicBlock.addIns(sdivIns);
        return sdivIns;
    }
    public SremIns generateSrem(BasicBlock basicBlock, MiddleVal L, MiddleVal R){
        String regName = registerPool.distributeRegister(); // 分配寄存器
        SremIns sremIns = new SremIns(basicBlock, regName, "i32", L.toString(), R.toString());
        basicBlock.addIns(sremIns);
        return sremIns;
    }
    public Function generateFunction(String name, String retType, ArrayList<Parameter> parameterList, ArrayList<BasicBlock> basicBlockList){
        Function function = new Function(name, retType, parameterList, basicBlockList);
        return function;
    }
    public RetIns generateRetVoid(BasicBlock basicBlock){
        RetIns retIns = new RetIns(basicBlock);
        basicBlock.addIns(retIns);
        return retIns;
    }
    public RetIns generateRetWithExp(BasicBlock basicBlock, String retType, MiddleVal retVal){
        RetIns retIns = new RetIns(basicBlock, retType, retVal);
        basicBlock.addIns(retIns);
        return retIns;
    }
    public GlobalDecl generateGlobalDecl(VarSymbol declSymbol){
        String name = declSymbol.getToken();
        int dimension = declSymbol.getDimension();
        if(dimension == 0){
            MiddleVal value = declSymbol.getValue();
            return new GlobalDecl(name, "i32", value, declSymbol.varIsConst());
        }else if(dimension == 1){
            ArrayList<MiddleVal> innerV_rList = declSymbol.getArrayValue().get(0);
            return new GlobalDecl(name, "i32*", innerV_rList, declSymbol.varIsConst());
        }else{
            ArrayList<ArrayList<MiddleVal>> outerV_rList = declSymbol.getArrayValue();
            return new GlobalDecl(name, outerV_rList, declSymbol.varIsConst());
        }
    }

    public AllocaIns generateAlloca(BasicBlock basicBlock, String type){
        String name = registerPool.distributeRegister();
        AllocaIns allocaIns = new AllocaIns(basicBlock, name, type);
        basicBlock.addIns(allocaIns);
        return allocaIns;
    }
    public StoreIns generateStore(BasicBlock basicBlock, String vt, String vv, String at, String av){
        StoreIns storeIns = new StoreIns(basicBlock, vt, vv, at, av);
        basicBlock.addIns(storeIns);
        return storeIns;
    }
    public LoadIns generateLoad(BasicBlock basicBlock, String vt, String at, String av){
        String regName = registerPool.distributeRegister();
        LoadIns loadIns = new LoadIns(basicBlock, regName, vt, at, av);
        basicBlock.addIns(loadIns);
        return loadIns;
    }
    public CallIns generateCall(BasicBlock basicBlock, String retType, String funPtr, ArrayList<Parameter> parameterList){
        if(retType.equals("void")){ // 无返回函数
            CallIns callIns = new CallIns(basicBlock, true, funPtr, null, "void", parameterList);
            basicBlock.addIns(callIns);
            return callIns;
        }else{ // 有返回
            String regName = registerPool.distributeRegister();
            CallIns callIns = new CallIns(basicBlock, false, funPtr, regName, retType, parameterList);
            basicBlock.addIns(callIns);
            return callIns;
        }
    }
    public String getParameterRegister(){
        return registerPool.distributeRegister();
    }
    public IcmpIns generateIcmp(BasicBlock basicBlock, String cmpMethod, String valType, MiddleVal left, MiddleVal right){
        // 先进行类型转换
        MiddleVal newLeft = left.copy();
        MiddleVal newRight = right.copy();
        if(!left.getValType().equals(valType)){ // 检查左右icmp比较值，若不为规定的值类型，则使用zextTo扩展（假定icmp规定值类型都是i32）
            ZextToIns zextToIns = generateZextTo(basicBlock, left.getValType(), left.toString(), valType);
            newLeft.setRegister(zextToIns.getRegName());
            newLeft.setValType(valType);
        }
        if(!right.getValType().equals(valType)){
            ZextToIns zextToIns = generateZextTo(basicBlock, right.getValType(), right.toString(), valType);
            newRight.setRegister(zextToIns.getRegName());
            newRight.setValType(valType);
        }
        String regName = registerPool.distributeRegister();
        IcmpIns icmpIns = new IcmpIns(basicBlock, regName, cmpMethod, valType, newLeft.toString(), newRight.toString());
        basicBlock.addIns(icmpIns);
        return icmpIns;
    }
    public AndIns generateAnd(BasicBlock basicBlock, String left, String right){
        String regName = registerPool.distributeRegister(); // 分配寄存器
        AndIns andIns = new AndIns(basicBlock, regName, "i1", left, right);
        basicBlock.addIns(andIns);
        return andIns;
    }
    public OrIns generateOr(BasicBlock basicBlock, String left, String right){
        String regName = registerPool.distributeRegister(); // 分配寄存器
        OrIns orIns = new OrIns(basicBlock, regName, "i1", left, right);
        basicBlock.addIns(orIns);
        return orIns;
    }
    public BrIns generateBr(BasicBlock basicBlock, boolean withRestriction, MiddleVal icmpResult, String ifTrueLabel, String ifFalseLabel, String destLabel){
        BrIns brIns = new BrIns(basicBlock, withRestriction, icmpResult, ifTrueLabel, ifFalseLabel, destLabel);
        basicBlock.addIns(brIns);
        return brIns;
    }
    public ZextToIns generateZextTo(BasicBlock basicBlock, String fromType,String value, String toType){
        String regName = registerPool.distributeRegister();
        ZextToIns zextToIns = new ZextToIns(basicBlock, regName, fromType, value, toType);
        basicBlock.addIns(zextToIns);
        return zextToIns;
    }
    public GetelementptrIns generateGetelementptr(BasicBlock basicBlock, String type, String addressReg, MiddleVal offset0, MiddleVal offset1, MiddleVal offset2){
        String regName = registerPool.distributeRegister();
        GetelementptrIns getelementptrIns = new GetelementptrIns(regName, basicBlock, type, addressReg, offset0, offset1, offset2);
        basicBlock.addIns(getelementptrIns);
        return getelementptrIns;
    }
}
