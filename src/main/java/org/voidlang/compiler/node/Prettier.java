package org.voidlang.compiler.node;

import com.google.common.base.Strings;
import dev.inventex.octa.console.ConsoleFormat;
import org.voidlang.compiler.node.common.Empty;
import org.voidlang.compiler.node.type.core.Type;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

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
            Object value = null;
            try {
                value = field.get(node);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (value == null || (value instanceof List<?> && ((List<?>) value).isEmpty()))
                continue;
            indent();
            printName(field.getName());
            processValue(value);
        }
    }

    public void processValue(Object value) {
        if (value instanceof Node node) {
            begin(node);
            content(node);
            end();
            return;
        }

        else if (value instanceof List) {
            List<?> list = (List<?>) value;
            beginArray();
            enterScope();
            for (Object o : list) {
                if (o instanceof Empty)
                    continue;
                indent();
                processValue(o);
            }
            exitScope();
            indent();
            endArray();
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

    public void beginArray() {
        System.out.println(ConsoleFormat.DARK_GRAY + "[");
    }

    public void endArray() {
        System.out.println(ConsoleFormat.DARK_GRAY + "]");
    }

    public void enterScope() {
        index++;
    }

    public void exitScope() {
        index--;
    }
}
