package util;

import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.Generator;
import org.voidlang.compiler.node.element.Method;
import org.voidlang.llvm.element.ExecutionEngine;
import org.voidlang.llvm.element.IRGenericValue;
import org.voidlang.llvm.element.IRModule;
import org.voidlang.llvm.element.MMCJITCompilerOptions;

import java.util.List;

import static org.bytedeco.llvm.global.LLVM.LLVMDisposeMessage;

@RequiredArgsConstructor
public class JIT {
    private final ExecutionEngine engine;

    public static JIT create(Package root) {
        Generator generator = root.getGenerator();
        IRModule module = generator.getModule();
        BytePointer error = new BytePointer((Pointer) null);
        if (!module.verify(IRModule.VerifierFailureAction.ABORT_PROCESS, error)) {
            LLVMDisposeMessage(error);
            throw new RuntimeException("LLVM module verification failed");
        }

        ExecutionEngine engine = ExecutionEngine.create();
        MMCJITCompilerOptions options = MMCJITCompilerOptions.create();
        if (!engine.createMCJITCompilerForModule(module, options, error)) {
            LLVMDisposeMessage(error);
            throw new RuntimeException("Failed to create JIT compiler: " + error.getString());
        }

        return new JIT(engine);
    }

    public IRGenericValue run(Method method, List<IRGenericValue> args) {
        return engine.runFunction(method.getFunction(), args);
    }

    public IRGenericValue run(Method method) {
        return run(method, List.of());
    }
}
