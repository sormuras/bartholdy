package integration;

public class Brahms {
  public static void main(String... args) {
    var expected = 123;
    var actual = new CustomTool(expected).run();
    if (actual != expected) {
      throw new AssertionError("expect " + expected + " but got: " + actual);
    }
  }
}
