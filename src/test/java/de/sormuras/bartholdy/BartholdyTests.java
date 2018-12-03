package de.sormuras.bartholdy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BartholdyTests {

  @Test
  void version() {
    assertEquals("DEVELOPMENT", Bartholdy.version());
  }

  @Test
  void privateConstructorThrows() {
    assertThrows(Exception.class, () -> Bartholdy.class.getDeclaredConstructor().newInstance());
  }

  @Test
  void treeDeleteForNonExistingPathFails() {
    var path = Paths.get("does not exist");
    var e = assertThrows(UncheckedIOException.class, () -> Bartholdy.treeDelete(path));
    assertEquals("removing tree failed: does not exist", e.getMessage());
  }

  @Test
  void treeListForNonExistingPathFails() {
    var path = Paths.get("does not exist");
    var e =
        assertThrows(
            UncheckedIOException.class, () -> Bartholdy.treeList(path, System.out::println));
    assertEquals("dumping tree failed: does not exist", e.getMessage());
  }

  private void createFiles(Path directory, int count) throws Exception {
    for (int i = 0; i < count; i++) {
      Files.createFile(directory.resolve("file-" + i));
    }
  }

  private void assertTreeDumpMatches(Path root, String... expected) {
    expected[0] = expected[0].replace(File.separatorChar, '/');
    List<String> dumpedLines = new ArrayList<>();
    Bartholdy.treeList(root, line -> dumpedLines.add(line.replace(File.separatorChar, '/')));
    assertLinesMatch(List.of(expected), dumpedLines);
  }

  @Test
  void tree() throws Exception {
    Path root = Files.createTempDirectory("tree-root-");
    assertTrue(Files.exists(root));
    assertEquals(1, Files.walk(root).count());
    assertTreeDumpMatches(root, root.toString(), ".");

    createFiles(root, 3);
    assertEquals(1 + 3, Files.walk(root).count());
    assertTreeDumpMatches(root, root.toString(), ".", "./file-0", "./file-1", "./file-2");

    createFiles(Files.createDirectory(root.resolve("a")), 3);
    createFiles(Files.createDirectory(root.resolve("b")), 3);
    createFiles(Files.createDirectory(root.resolve("x")), 3);
    assertTrue(Files.exists(root));
    assertEquals(1 + 3 + 4 * 3, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/file-0",
        "./a/file-1",
        "./a/file-2",
        "./b",
        "./b/file-0",
        "./b/file-1",
        "./b/file-2",
        "./file-0",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-0",
        "./x/file-1",
        "./x/file-2");

    Bartholdy.treeDelete(root, path -> path.startsWith(root.resolve("b")));
    assertEquals(1 + 2 + 3 * 3, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/file-0",
        "./a/file-1",
        "./a/file-2",
        "./file-0",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-0",
        "./x/file-1",
        "./x/file-2");

    Bartholdy.treeDelete(root, path -> path.endsWith("file-0"));
    assertEquals(1 + 2 + 3 * 2, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/file-1",
        "./a/file-2",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-1",
        "./x/file-2");

    Bartholdy.treeCopy(root.resolve("x"), root.resolve("a/b/c"));
    assertEquals(1 + 4 + 4 * 2, Files.walk(root).count());
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/b",
        "./a/b/c",
        "./a/b/c/file-1",
        "./a/b/c/file-2",
        "./a/file-1",
        "./a/file-2",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-1",
        "./x/file-2");

    Bartholdy.treeCopy(root.resolve("x"), root.resolve("x/y"));
    assertTreeDumpMatches(
        root,
        root.toString(),
        ".",
        "./a",
        "./a/b",
        "./a/b/c",
        "./a/b/c/file-1",
        "./a/b/c/file-2",
        "./a/file-1",
        "./a/file-2",
        "./file-1",
        "./file-2",
        "./x",
        "./x/file-1",
        "./x/file-2",
        "./x/y",
        "./x/y/file-1",
        "./x/y/file-2");

    Bartholdy.treeDelete(root);
    assertTrue(Files.notExists(root));
  }
}
