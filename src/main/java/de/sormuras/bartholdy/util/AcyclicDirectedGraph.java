package de.sormuras.bartholdy.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

class AcyclicDirectedGraph {

  static class CyclicEdgeException extends IllegalArgumentException {

    CyclicEdgeException(Edge edge, Set<Edge> graph) {
      super("Edge " + edge + " creates a cycle in graph:" + graph);
    }

    CyclicEdgeException(String message) {
      super(message);
    }
  }

  private abstract static class Base<T extends Base> implements Comparable<T> {
    final String id;

    Base(String id) {
      this.id = id;
    }

    @Override
    public int compareTo(T o) {
      return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      return id.equals(((Edge) o).id);
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }
  }

  static class Edge extends Base<Edge> {

    final Node source;
    final Node target;

    Edge(Node source, Node target) {
      super(source.id + '\t' + target.id);
      this.source = source;
      this.target = target;
    }

    @Override
    public String toString() {
      return source.id + "->" + target.id;
    }
  }

  static class Node extends Base<Node> {

    final Set<Node> inbounds;
    final Set<Node> outbounds;

    Node(String id) {
      super(id);
      this.inbounds = new TreeSet<>();
      this.outbounds = new TreeSet<>();
    }
  }

  private final Map<String, Node> nodes;
  private final Set<Edge> edges;
  private final Set<Edge> cyclic;

  AcyclicDirectedGraph(Set<String> nodeIds) {
    this.nodes = new HashMap<>();
    this.edges = new TreeSet<>();
    this.cyclic = new TreeSet<>();

    nodeIds.forEach(id -> nodes.put(id, new Node(id)));
  }

  void addEdge(String sourceId, String targetId) {
    if (Objects.equals(sourceId, targetId)) {
      throw new CyclicEdgeException("Source and target id are equal: " + sourceId);
    }
    var source = node(sourceId);
    var target = node(targetId);
    var edge = new Edge(source, target);
    var back = new Edge(target, source);
    if (cyclic.contains(edge) || edges.contains(back)) {
      throw new CyclicEdgeException(edge, edges);
    }
    // add edge to set of "good" edges, i.e. the graph
    if (edges.add(edge)) {
      // remember connections
      source.outbounds.add(target);
      target.inbounds.add(source);

      // update set of "bad" edges
      update(edge);

      // clean up
      cyclic.remove(back);
    }
  }

  private Node node(String id) {
    var node = nodes.get(id);
    if (node == null) {
      throw new IllegalArgumentException("Unknown node: " + id);
    }
    return node;
  }

  private void update(Edge edge) {
    var source = edge.source;
    var target = edge.target;

    walk(source, node -> node.inbounds, node -> mark(target, node));
    walk(target, node -> node.outbounds, node -> mark(node, source));

    var froms = new TreeSet<Node>();
    var tos = new TreeSet<Node>();
    walk(target, node -> node.outbounds, froms::add);
    walk(source, node -> node.inbounds, tos::add);
    for (var from : froms) {
      for (var to : tos) {
        mark(from, to);
      }
    }
  }

  private void mark(Node source, Node target) {
    if (source == target) {
      return;
    }
    var back = new Edge(target, source);
    if (edges.contains(back)) {
      return;
    }
    var edge = new Edge(source, target);
    cyclic.add(edge);
  }

  private void walk(Node source, Function<Node, Set<Node>> nodes, Consumer<Node> consumer) {
    for (var node : nodes.apply(source)) {
      consumer.accept(node);
      walk(node, nodes, consumer);
    }
  }
}
