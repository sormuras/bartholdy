package de.sormuras.bartholdy.util.graph;

import static java.util.Arrays.*;

import java.util.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class Node<T> {
  private T value;
  private List<Node<T>> links = new ArrayList<>();
  private List<Mark> marks = new ArrayList<>();

  Node(T value) {
    this.value = value;
  }

  public T getValue() {
    return value;
  }

  public List<Node<T>> getLinks() {
    return links;
  }

  public Node<T> linkedTo(Node<T> target) {
    links.add(target);
    return this;
  }

  @SafeVarargs
  public final boolean isLinkedTo(Node<T>... nodes) {
    return isLinkedTo(asList(nodes));
  }

  public boolean isLinkedTo(Collection<Node<T>> nodes) {
    return this.links.containsAll(nodes);
  }

  public boolean hasLinks() {
    return !links.isEmpty();
  }

  public void forEachLink(Consumer<? super Node<T>> consumer) {
    links.forEach(consumer);
  }

  public Node<T> marked(Mark mark) {
    mark(mark);
    return this;
  }

  public boolean mark(Mark mark) {
    if (marks.contains(mark)) return false;
    marks.add(mark);
    return true;
  }

  /** @return was it marked as this? */
  public boolean unmark(Mark mark) {
    return marks.remove(mark);
  }

  public boolean isMarked(Mark mark) {
    return marks.contains(mark);
  }

  public boolean isMarked(Class<? extends Mark> type) {
    return getMark(type).isPresent();
  }

  public <M extends Mark> Optional<M> unmark(Class<M> type) {
    Optional<M> mark = getMark(type);
    mark.ifPresent(m -> marks.remove(m));
    return mark;
  }

  public <M extends Mark> Optional<M> getMark(Class<M> type) {
    return marks.stream().filter(type::isInstance).findAny().map(type::cast);
  }

  @Override
  public String toString() {
    return value
        + (marks.isEmpty() ? "" : marks.toString())
        + " -> {"
        + Graph.nodeNames(links)
        + "}";
  }
}
