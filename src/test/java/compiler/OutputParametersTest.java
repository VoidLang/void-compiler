package compiler;

import org.junit.jupiter.api.Test;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.element.Method;
import util.Compiler;
import util.JIT;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class OutputParametersTest {
    @Test
    public void testFunctionWithIntOutputParameters() {
        Package root = assertDoesNotThrow(() -> Compiler.compile("compiler/OutputParameters.vs"));

        JIT jit = assertDoesNotThrow(() -> JIT.create(root));

        Method passAsReference = root.resolveMethod("passAsReference", new ArrayList<>());
        assertNotNull(passAsReference);

        long result = jit.run(passAsReference).toInt();
        assertEquals(30, result);

        Method passPointers = root.resolveMethod("passPointers", new ArrayList<>());
        assertNotNull(passPointers);

        long result2 = jit.run(passPointers).toInt();
        assertEquals(30, result2);
    }
}
