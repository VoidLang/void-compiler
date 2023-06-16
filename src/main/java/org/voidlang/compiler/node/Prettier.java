package org.voidlang.compiler.node;

import com.google.common.base.Strings;
import dev.inventex.octa.console.ConsoleFormat;
import org.voidlang.compiler.node.type.core.Type;

import java.lang.reflect.Field;

public class Prettier {
    public static final String INDENTATION = "    ";

    private int index;

    public void begin(Node node) {
        index++;
        printNodeType(node.getClass().getSimpleName());
        System.out.print(" ");
        beginObject();
    }

    public void content(Node node) {
        Field[] fields = node.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            indent();
            printName(field.getName());
            try {
                Object value = field.get(node);
                processValue(value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void processValue(Object value) {
        if (value instanceof Node node) {
            begin(node);
            content(node);
            end();
            return;
        }

        ConsoleFormat format = ConsoleFormat.WHITE;
        if (value instanceof Type)
            format = ConsoleFormat.YELLOW;
        else if (value instanceof String)
            format = ConsoleFormat.GREEN;
        System.out.println(format + String.valueOf(value));
    }

    public void indent() {
        System.out.print(Strings.repeat(INDENTATION, index));
    }

    public void printName(String name) {
        System.out.print(ConsoleFormat.WHITE + name + ConsoleFormat.LIGHT_GRAY + ": ");
    }

    public void end() {
        index--;
        indent();
        endObject();
    }

    public void printNodeType(String type) {
        System.out.print(ConsoleFormat.YELLOW + type);
    }

    public void beginObject() {
        System.out.println(ConsoleFormat.DARK_GRAY + "{");
    }

    public void endObject() {
        System.out.println(ConsoleFormat.DARK_GRAY + "}");
    }

    public void enterScope() {
        index++;
    }

    public void exitScope() {
        index--;
    }
}
