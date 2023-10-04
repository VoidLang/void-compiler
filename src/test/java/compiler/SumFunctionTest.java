package compiler;

import org.junit.jupiter.api.Test;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.element.Method;
import util.Compiler;
import util.JIT;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SumFunctionTest {
    @Test
    public void testCallingMainMethod() {
        Package root = assertDoesNotThrow(() -> Compiler.compile("compiler/SumFunction.vs"));

        JIT jit = assertDoesNotThrow(() -> JIT.create(root));

        Method method = root.resolveMethod("main", new ArrayList<>());
        assertNotNull(method);

        long result = jit.run(method).toInt();
        assertEquals(20 + 30, result);
    }
}