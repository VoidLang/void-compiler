package org.voidlang.compiler.runtime;

import lombok.experimental.UtilityClass;
import org.voidlang.compiler.node.Generator;
import org.voidlang.llvm.element.*;
import org.voidlang.llvm.element.Comparator;

import java.util.*;

@UtilityClass
public class Runtime {
    private final Map<IRModule, IRFunction> getStdHandleMap = new HashMap<>();

    private final Map<IRModule, IRFunction> writeConsoleAMap = new HashMap<>();

    private final Map<IRModule, IRFunction> strlenMap = new HashMap<>();

    private final Map<IRModule, IRFunction> exitMap = new HashMap<>();

    private final Map<IRModule, IRFunction> itoaMap = new HashMap<>();

    private final Map<IRModule, Map<String, String>> globalStringMap = new HashMap<>();

    public IRFunction getStdHandle(Generator generator) {
        return getStdHandleMap.computeIfAbsent(generator.getModule(), module -> {
            IRContext context = module.getContext();
            IRType int32Type = IRType.int32(context);

            IRFunctionType type = IRFunctionType.create(int32Type, List.of(int32Type));
            IRFunction function = IRFunction.getByName(module, "GetStdHandle", type);
            if (function == null)
                function = IRFunction.create(module, "GetStdHandle", type);

            return function;
        });
    }

    public IRFunction writeConsoleA(Generator generator) {
        return writeConsoleAMap.computeIfAbsent(generator.getModule(), module -> {
            IRContext context = module.getContext();
            IRType int32Type = IRType.int32(context);

            IRFunctionType type = IRFunctionType.create(int32Type, List.of(
                int32Type, // std handle
                IRType.int8(context).toPointerType(), // message buffer
                int32Type, // buffer length
                int32Type.toPointerType(), // chars written (output)
                int32Type // NULL
            ));

            IRFunction function = IRFunction.getByName(module, "WriteConsoleA", type);
            if (function == null)
                function = IRFunction.create(module, "WriteConsoleA", type);

            return function;
        });
    }

    public IRFunction strlen(Generator generator) {
        return strlenMap.computeIfAbsent(generator.getModule(), module -> {
            IRContext context = module.getContext();
            IRType int32Type = IRType.int32(context);
            IRType int8Type = IRType.int8(context);

            IRFunctionType type = IRFunctionType.create(int32Type, List.of(int8Type.toPointerType()));
            IRFunction function = IRFunction.getByName(module, "strlen", type);
            if (function == null)
                function = IRFunction.create(module, "strlen", type);

            return function;
        });
    }

    public IRFunction exit(Generator generator) {
        return exitMap.computeIfAbsent(generator.getModule(), module -> {
            IRContext context = module.getContext();

            IRFunctionType type = IRFunctionType.create(IRType.voidType(context), List.of(IRType.int32(context)));
            IRFunction function = IRFunction.getByName(module, "exit", type);
            if (function == null)
                function = IRFunction.create(module, "exit", type);

            return function;
        });
    }

    public IRFunction itoa(Generator generator) {
        return itoaMap.computeIfAbsent(generator.getModule(), module -> {
            IRContext context = module.getContext();
            IRType int32Type = IRType.int32(context);
            IRType int8Type = IRType.int8(context);

            IRFunctionType type = IRFunctionType.create(int8Type.toPointerType(), List.of(
                int32Type, // number
                int8Type.toPointerType(), // buffer
                int32Type // radix
            ));

            IRFunction function = IRFunction.getByName(module, "itoa", type);
            if (function == null)
                function = IRFunction.create(module, "itoa", type);

            return function;
        });
    }

    public IRGlobal globalString(Generator generator, String value) {
        IRModule module = generator.getModule();
        IRContext context = module.getContext();

        Map<String, String> strings = globalStringMap.computeIfAbsent(module, m -> new HashMap<>());
        String key = strings.get(value);
        if (key == null)
            key = "internal_string_" + (strings.size() + 1);

        if (!strings.containsValue(key)) {
            IRString string = new IRString(context, value, true);
            IRGlobal global = module.addGlobal(string.getType(), key);
            global.setInitializer(string);
            strings.put(value, key);
        }

        return module.getGlobal(key);
    }

    public IRValue getStringLength(Generator generator, IRValue value) {
        IRBuilder builder = generator.getBuilder();
        IRFunction strlenFunction = strlen(generator);

        return builder.call(strlenFunction, List.of(value));
    }

    public void exit(Generator generator, int code) {
        IRBuilder builder = generator.getBuilder();
        IRContext context = generator.getContext();

        IRFunction exitFunction = exit(generator);
        builder.call(exitFunction, List.of(IRType.int32(context).constInt(code)));
    }

    public IRValue i32ToString(Generator generator, IRValue value) {
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        IRFunction itoaFunction = itoa(generator);

        IRType int32Type = IRType.int32(context);
        IRType int8Type = IRType.int8(context);

        IRValue buffer = builder.alloc(int8Type.toPointerType(), "itoa buffer");
        builder.call(itoaFunction, List.of(value, buffer, int32Type.constInt(10)));

        return buffer;
    }

    public void stdout(Generator generator, IRValue buffer, IRValue length) {
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        IRFunction stdHandleFunction = getStdHandle(generator);
        IRFunction writeConsoleAFunction = writeConsoleA(generator);

        IRType int32Type = IRType.int32(context);

        IRValue stdHandle = builder.call(stdHandleFunction, List.of(int32Type.constInt(-11)), "std handle");

        IRValue bytesWritten = builder.alloc(int32Type, "bytes written");

        builder.call(writeConsoleAFunction, List.of(
            stdHandle,
            buffer,
            length,
            bytesWritten,
            int32Type.constInt(0)
        ));
    }

    public void stdout(Generator generator, String message) {
        IRType int32Type = IRType.int32(generator.getContext());

        IRGlobal buffer = globalString(generator, message);
        IRValue length = int32Type.constInt(message.length());

        stdout(generator, buffer, length);
    }

    public void stdoutI32(Generator generator, IRValue number) {
        IRValue buffer = i32ToString(generator, number);
        IRValue length = getStringLength(generator, buffer);

        stdout(generator, buffer, length);
    }

    public void checkIndex(Generator generator, IRFunction function, IRValue index, int length) {
        IRContext context = generator.getContext();
        IRBuilder builder = generator.getBuilder();

        IRType int32Type = IRType.int32(context);

        IRValue lessThanZero = builder.compareInt(
            Comparator.SIGNED_INTEGER_LESS_THAN, index, int32Type.constInt(0)
        );
        IRValue greaterOrEqualSize = builder.compareInt(
            Comparator.SIGNED_INTEGER_GREATER_OR_EQUAL, index, int32Type.constInt(length)
        );
        IRValue condition = builder.or(lessThanZero, greaterOrEqualSize);

        IRBlock invalidBlock = IRBlock.create(function, "invalid index");
        IRBlock validBlock = IRBlock.create(function, "valid index");

        builder.jumpIf(condition, invalidBlock, validBlock);
        builder.positionAtEnd(invalidBlock);

        stdout(generator, "Thread panicked with error: Array index out of bounds (index: ");
        stdoutI32(generator, index);
        stdout(generator, ", length: " + length + ")\n");

        exit(generator, 101);

        builder.returnValue(int32Type.constInt(-1));

        builder.positionAtEnd(validBlock);
    }
}
