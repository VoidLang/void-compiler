package org.voidlang.compiler.builder;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
public class ProjectSettings {
    public String name;
    public String version;
    public String description;

    public List<String> author;
}
