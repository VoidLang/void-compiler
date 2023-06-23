package org.voidlang.compiler.node.element;

import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.Node;

import java.util.List;

@RequiredArgsConstructor
public abstract class Scope extends Node {
    private final List<Node> body;
}
