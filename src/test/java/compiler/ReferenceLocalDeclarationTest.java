package compiler;

import org.junit.jupiter.api.Test;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.element.Method;
import util.Compiler;
import util.JIT;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class ReferenceLocalDeclarationTest {
    @Test
    public void testCallFunctionWithReferenceLocals() {
        Package root = assertDoesNotThrow(() -> Compiler.compile("compiler/ReferenceLocalDeclaration.vs"));

        JIT jit = assertDoesNotThrow(() -> JIT.create(root));

        Method method = root.resolveMethod("main", new ArrayList<>());
        assertNotNull(method);

        long result = jit.run(method).toInt();
        assertEquals(7, result);
    }
}
