package util;

import lombok.experimental.UtilityClass;
import org.voidlang.compiler.node.Generator;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRModule;

import static org.bytedeco.llvm.global.LLVM.*;
import static org.bytedeco.llvm.global.LLVM.LLVMInitializeNativeTarget;

@UtilityClass
public class LLVM {
    public static Generator createContext() {
        LLVMInitializeCore(LLVMGetGlobalPassRegistry());
        LLVMLinkInMCJIT();
        LLVMInitializeNativeAsmPrinter();
        LLVMInitializeNativeAsmParser();
        LLVMInitializeNativeTarget();

        IRContext context = IRContext.create();
        IRModule module = IRModule.create(context, "test_module");
        IRBuilder builder = IRBuilder.create(context);

        return new Generator(context, module, builder);
    }
}
