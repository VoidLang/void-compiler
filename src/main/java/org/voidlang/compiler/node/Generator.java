package org.voidlang.compiler.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRModule;

@RequiredArgsConstructor
@Getter
public class Generator {
    private final IRContext context;
    
    private final IRModule module;
    
    private final IRBuilder builder;
}
