package de.sormuras.bartholdy.util.graph;

public interface Mark {

  default <T> boolean mark(Node<T> node) {
    return node.mark(this);
  }

  default <T> boolean isMarked(Node<T> node) {
    return node.isMarked(this);
  }

  default <T> boolean unmark(Node<T> node) {
    return node.unmark(this);
  }

  class StringMark implements Mark {
    String string;

    @Override
    public String toString() {
      return string;
    }
  }

  class Index implements Mark {

    public Index(int index) {
      this.index = index;
    }

    public static int of(Node<?> node) {
      return node.getMark(Index.class).orElseThrow().index;
    }

    int index;
  }
}
