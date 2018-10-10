package de.sormuras.bartholdy.util.graph;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;

import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

public class Graph<T> {
  @SafeVarargs
  public static <T> String nodeNames(Node<T>... nodes) {
    return nodeNames(asList(nodes));
  }

  public static <T> String nodeNames(List<Node<T>> nodes) {
    return listToString(", ", nodes, node -> node.getValue().toString());
  }

  public static String listToString(String delimiter, List<?> list) {
    return listToString(delimiter, list, Object::toString);
  }

  public static <T> String listToString(
      String delimiter, List<T> list, Function<T, String> toString) {
    return list.stream().map(toString).collect(joining(delimiter));
  }

  private final List<Node<T>> nodes = new ArrayList<>();

  public List<Node<T>> getNodes() {
    return nodes;
  }

  public Node<T> findOrCreateNode(T value) {
    return findNode(value).orElseGet(() -> createNode(value));
  }

  public Optional<Node<T>> findNode(T value) {
    List<Node<T>> found = find(node -> node.getValue().equals(value));
    if (found.isEmpty()) return Optional.empty();
    else if (found.size() == 1) return Optional.of(found.get(0));
    else
      throw new IllegalStateException("multiple nodes with the same value to search for: " + value);
  }

  public Node<T> createNode(T value) {
    Node<T> node = new Node<>(value);
    nodes.add(node);
    return node;
  }

  public void topologicalSort() {
    List<List<Node<T>>> stronglyConnectedComponents = findStronglyConnectedComponents();

    List<Node<T>> sorted = new ArrayList<>();
    handleCycles(stronglyConnectedComponents, sorted);

    reverse(sorted);
    replaceNodes(sorted);
  }

  public List<List<Node<T>>> findStronglyConnectedComponents() {
    StronglyConnectedComponentsFinder<T> visitor = new StronglyConnectedComponentsFinder<>();

    visit(visitor);

    cleanup();
    return visitor.getStronglyConnectedComponents();
  }

  private void handleCycles(List<List<Node<T>>> stronglyConnectedComponents, List<Node<T>> sorted) {
    List<List<Node<T>>> cycles = new ArrayList<>();
    stronglyConnectedComponents.forEach(
        scc -> {
          if (scc.size() > 1) cycles.add(scc);
          Node<T> node = scc.iterator().next();
          if (node.isLinkedTo(node)) cycles.add(scc);
          sorted.add(node);
        });
    if (!cycles.isEmpty()) throw new CyclesFoundException(cycles);
  }

  private void cleanup() {
    unmark(StronglyConnectedComponentsFinder.Root.class);
    unmark(Mark.Index.class);
  }

  private void replaceNodes(List<Node<T>> nodes) {
    this.nodes.clear();
    this.nodes.addAll(nodes);
  }

  public int mark(Mark mark) {
    return countingVisit(mark::mark);
  }

  public int unmark(Mark mark) {
    return countingVisit(mark::unmark);
  }

  public int unmark(Class<? extends Mark> type) {
    return countingVisit(node -> node.unmark(type).isPresent());
  }

  public int countingVisit(Function<Node<T>, Boolean> visitor) {
    AtomicInteger count = new AtomicInteger(0);
    visit(
        node -> {
          if (visitor.apply(node)) count.incrementAndGet();
        });
    return count.get();
  }

  public List<Node<T>> find(Mark mark) {
    return find(mark::isMarked);
  }

  public List<Node<T>> find(Predicate<Node<T>> predicate) {
    List<Node<T>> found = new ArrayList<>();
    visit(
        node -> {
          if (predicate.test(node)) found.add(node);
        });
    return found;
  }

  public void visit(Consumer<Node<T>> visitor) {
    nodes.forEach(visitor);
  }

  public List<Node<T>> remove(Predicate<Node<T>> predicate) {
    List<Node<T>> found = find(predicate);
    found.forEach(this::remove);
    return found;
  }

  public boolean remove(Node<T> node) {
    return nodes.remove(node);
  }

  public int size() {
    return nodes.size();
  }

  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  @Override
  public String toString() {
    return listToString("\n", nodes);
  }
}
