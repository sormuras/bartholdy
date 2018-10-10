package de.sormuras.bartholdy.util.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @see <a
 *     href="https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">Tarjan's
 *     Strongly Connected Components Algorithm</a>
 */
class StronglyConnectedComponentsFinder<T> implements Consumer<Node<T>> {

  static class Root implements Mark {

    Root(int index) {
      this.index = index;
    }

    static int removeRoot(Node<?> node) {
      return node.unmark(Root.class).get().getIndex();
    }

    static int of(Node<?> node) {
      return node.getMark(Root.class).get().getIndex();
    }

    int index;

    public int getIndex() {
      return index;
    }
  }

  private final List<List<Node<T>>> stronglyConnectedComponents = new ArrayList<>();

  private final AtomicInteger index = new AtomicInteger(0);
  private final Stack<Node<T>> stack = new Stack<>();

  public List<List<Node<T>>> getStronglyConnectedComponents() {
    return stronglyConnectedComponents;
  }

  @Override
  public void accept(Node<T> node) {
    if (!node.isMarked(Mark.Index.class)) strongConnect(node);
  }

  private void strongConnect(Node<T> node) {
    int indexValue = index.getAndIncrement();
    node.mark(new Mark.Index(indexValue));
    node.mark(new Root(indexValue));
    stack.push(node);

    for (Node<T> successor : node.getLinks()) {
      if (!successor.isMarked(Mark.Index.class)) {
        strongConnect(successor);
        setRoot(node, Root.of(successor));
      } else if (stack.contains(successor)) {
        // -> it's in the current SCC
        setRoot(node, Mark.Index.of(successor));
      }
    }

    if (isRoot(node)) stronglyConnectedComponents.add(popSCC(stack, node));
  }

  private void setRoot(Node<T> node, int min) {
    node.mark(new Root(Math.min(Root.removeRoot(node), min)));
  }

  private List<Node<T>> popSCC(Stack<Node<T>> stack, Node<T> node) {
    List<Node<T>> scc = new ArrayList<>();
    Node<T> member;
    do {
      member = stack.pop();
      scc.add(member);
    } while (member != node);
    return scc;
  }

  private boolean isRoot(Node<T> node) {
    return Root.of(node) == Mark.Index.of(node);
  }
}
