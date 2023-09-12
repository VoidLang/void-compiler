package org.voidlang.compiler;

import net.voidhttp.optionparser.Option;
import net.voidhttp.optionparser.OptionBuilder;
import net.voidhttp.optionparser.OptionParser;
import net.voidhttp.optionparser.OptionType;
import org.voidlang.compiler.cli.Compiler;
import org.voidlang.compiler.cli.Generator;

public class Main {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();

        Option newOption = new OptionBuilder()
            .setName("new")
            .setType(OptionType.TEXT)
            .setAliases("-n", "--new")
            .setHelp("create a new Void project")
            .build();

        parser.addOption(newOption);

        Option compileOption = new OptionBuilder()
            .setName("compile")
            .setType(OptionType.TEXT)
            .setAliases("-c", "--compile")
            .setHelp("compile a Void project")
            .build();

        parser.addOption(compileOption);

        parser.parse(args);

        if (newOption.isPresent())
            new Generator(newOption.stringValue()).generate();

        else if (compileOption.isPresent())
            new Compiler(compileOption.stringValue()).compile();
    }
}