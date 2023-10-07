package compiler;

import org.junit.jupiter.api.Test;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.element.Method;
import util.Compiler;
import util.JIT;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class HeapAllocationTest {
    @Test
    public void testPrimitiveAllocation() {
        Package root = assertDoesNotThrow(() -> Compiler.compile("compiler/HeapAllocation.vs"));

        JIT jit = assertDoesNotThrow(() -> JIT.create(root));

        Method allocInteger = root.resolveMethod("allocInteger", new ArrayList<>());
        assertNotNull(allocInteger);

        long result = jit.run(allocInteger).toInt();
        assertEquals(100, result);
    }
}
