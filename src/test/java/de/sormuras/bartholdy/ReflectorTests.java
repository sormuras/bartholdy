package de.sormuras.bartholdy;

import static java.util.Comparator.comparing;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ReflectorTests {

  @Test
  void empty() {
    assertTrue(Reflector.reflect(new Object()).isEmpty());
  }

  @Test
  @DisplayName("Options are reflected, ordered by name and added to the command instance")
  @SuppressWarnings("unused")
  void reflectAnonymousClass() {
    Object options =
        new Object() {
          @Reflector.Option("--ZETA")
          boolean z = true;

          Boolean flag1 = Boolean.TRUE;
          byte hex = 13;
          int valueOfTheAnswer = 42;
          Boolean flag2 = Boolean.FALSE;

          transient String unused = "hidden";
          private Byte hidden = Byte.valueOf("123");
          // static Number ignored = Short.valueOf("456");
          Object nullIsSkipped = null;
          Collection<?> emptyCollectionsIsSkipped = Set.of();

          List<String> collection = List.of("a", "b", "c");
          Set<Path> paths = Set.of(Paths.get("path"));

          void hex(Reflector reflector) {
            reflector.add("--prime-as-hex");
            reflector.add("0x" + Integer.toHexString(hex));
          }
        };
    var actualLines = new ArrayList<String>();
    var reflector =
        new Reflector(
            options, fields -> fields.sorted(comparing(Field::getName)), actualLines::add);
    reflector.reflect();
    reflector.add(Stream.of(1, 2, 3), "+");
    reflector.add(List.of(Paths.get("p"), Paths.get("q")));
    reflector.add("final");

    assertLinesMatch(
        List.of(
            "-collection",
            "[a, b, c]",
            "-flag1",
            "true",
            "-flag2",
            "false",
            "--prime-as-hex",
            "0xd",
            "-paths",
            "path",
            "--value-of-the-answer",
            "42",
            "--ZETA",
            "1+2+3",
            "p" + File.pathSeparator + "q",
            "final"),
        actualLines);
  }


  @Test
  void addAllOptionsWithIllegalProperties() {
    var options = new ClassWithPrivateField();
    var fields = Arrays.stream(ClassWithPrivateField.class.getDeclaredFields());
    var reflector = new Reflector(options, __ -> fields, new ArrayList<String>()::add);
    var error = assertThrows(Error.class, (Executable) reflector::reflect);
    assertTrue(error.getMessage().startsWith("reflecting field 'private int"), error.getMessage());
    var cause = error.getCause();
    assertTrue(cause.getMessage().startsWith(Reflector.class +" cannot access a member of class"), cause.getMessage());
    assertTrue(cause.getMessage().endsWith("with modifiers \"private\""));
  }

  @Test
  void reflectIgnoresStaticFields() {
    assertLinesMatch(List.of("-a", "1"), Reflector.reflect(new ClassWithStaticField()));
  }

  private static class ClassWithPrivateField {

    private int x = 3;

    @Override
    public String toString() {
      return "ClassWithPrivateField [x=" + x + "]";
    }
  }

  private static class ClassWithStaticField {

    int a = 1;
    static int b = 2;

    @Override
    public String toString() {
      return "ClassWithStaticField [a=" + a + ", b=" + b + "]";
    }
  }
}
