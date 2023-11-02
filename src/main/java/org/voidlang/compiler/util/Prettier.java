package org.voidlang.compiler.util;

import com.google.common.base.Strings;
import dev.inventex.octa.console.ConsoleFormat;
import lombok.Getter;
import lombok.Setter;
import org.voidlang.compiler.node.Node;
import org.voidlang.compiler.node.common.Empty;
import org.voidlang.compiler.node.type.core.Type;
import org.voidlang.compiler.util.PrettierIgnore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

public class Prettier {
    @Getter
    @Setter
    private static boolean enabled = true;

    public static final String INDENTATION = "    ";

    private int index;

    public void begin(Node node) {
        if (!enabled)
            return;

        index++;
        printNodeType(node.getClass().getSimpleName());
        System.out.print(" ");
        beginObject();
    }

    public void content(Node node) {
        if (!enabled)
            return;

        Field[] fields = node.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (Modifier.isStatic(field.getModifiers()))
                continue;
            if (field.isAnnotationPresent(PrettierIgnore.class))
                continue;
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
        if (!enabled)
            return;

        if (value instanceof Node node) {
            begin(node);
            content(node);
            end();
            return;
        }

        else if (value instanceof List<?> list) {
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
        if (!enabled)
            return;

        System.out.print(Strings.repeat(INDENTATION, index));
    }

    public void printName(String name) {
        if (!enabled)
            return;

        System.out.print(ConsoleFormat.WHITE + name + ConsoleFormat.LIGHT_GRAY + ": ");
    }

    public void end() {
        if (!enabled)
            return;

        index--;
        indent();
        endObject();
    }

    public void printNodeType(String type) {
        if (!enabled)
            return;

        System.out.print(ConsoleFormat.YELLOW + type);
    }

    private void beginObject() {
        System.out.println(ConsoleFormat.DARK_GRAY + "{");
    }

    private void endObject() {
        System.out.println(ConsoleFormat.DARK_GRAY + "}");
    }

    private void beginArray() {
        System.out.println(ConsoleFormat.DARK_GRAY + "[");
    }

    private void endArray() {
        System.out.println(ConsoleFormat.DARK_GRAY + "]");
    }

    public void enterScope() {
        if (!enabled)
            return;

        index++;
    }

    public void exitScope() {
        if (!enabled)
            return;

        index--;
    }
}
