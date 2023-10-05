package org.voidlang.compiler.node.local;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.voidlang.compiler.node.NodeInfo;
import org.voidlang.compiler.node.NodeType;

@RequiredArgsConstructor
@Getter
@NodeInfo(type = NodeType.REFERENCE_LOCAL_DECLARE_ASSIGN)
public class ReferenceLocalDeclareAssign {
}
