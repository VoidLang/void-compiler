package compiler;

import org.junit.jupiter.api.Test;
import org.voidlang.compiler.builder.Package;
import org.voidlang.compiler.node.element.Method;
import util.Compiler;
import util.JIT;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SizeofTypeTest {
    @Test
    public void testSizeOfTypes() {
        Package root = assertDoesNotThrow(() -> Compiler.compile("compiler/SizeofType.vs"));

        JIT jit = assertDoesNotThrow(() -> JIT.create(root));

        Method doubleSize = root.resolveMethod("doubleSize", new ArrayList<>());
        assertNotNull(doubleSize);

        long doubleResult = jit.run(doubleSize).toInt();
        assertEquals(8, doubleResult);

        Method floatSize = root.resolveMethod("floatSize", new ArrayList<>());
        assertNotNull(floatSize);

        long floatResult = jit.run(floatSize).toInt();
        assertEquals(4, floatResult);

        Method longSize = root.resolveMethod("longSize", new ArrayList<>());
        assertNotNull(longSize);

        long longResult = jit.run(longSize).toInt();
        assertEquals(8, longResult);

        Method intSize = root.resolveMethod("intSize", new ArrayList<>());
        assertNotNull(intSize);

        long intResult = jit.run(intSize).toInt();
        assertEquals(4, intResult);

        Method shortSize = root.resolveMethod("shortSize", new ArrayList<>());
        assertNotNull(shortSize);

        long shortResult = jit.run(shortSize).toInt();
        assertEquals(2, shortResult);

        Method byteSize = root.resolveMethod("byteSize", new ArrayList<>());
        assertNotNull(byteSize);

        long byteResult = jit.run(byteSize).toInt();
        assertEquals(1, byteResult);

        Method boolSize = root.resolveMethod("boolSize", new ArrayList<>());
        assertNotNull(boolSize);

        long boolResult = jit.run(boolSize).toInt();
        assertEquals(1, boolResult);
    }
}
