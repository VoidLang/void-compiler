package org.voidlang.compiler.node;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.voidlang.llvm.element.IRBuilder;
import org.voidlang.llvm.element.IRContext;
import org.voidlang.llvm.element.IRModule;

@AllArgsConstructor
@Getter
public class Generator {
    private final IRContext context;
    
    private final IRModule module;
    
    private final IRBuilder builder;
}
