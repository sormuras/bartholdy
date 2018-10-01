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

  abstract static class Base<T extends Base> implements Comparable<T> {
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
      // System.out.println("\n+ " + edge + " // " + edges);

      // clean up
      cyclic.remove(back);

      // update set of "bad" edges
      target.inbounds.add(source);
      walk(source, node -> node.inbounds, node -> cyclic.add(new Edge(target, node)));

      walk(target, node -> node.outbounds, node -> cyclic.add(new Edge(node, source)));
      source.outbounds.add(target);

      // System.out.println("c " + cyclic);
    }
  }

  private Node node(String id) {
    var node = nodes.get(id);
    if (node == null) {
      throw new IllegalArgumentException("Unknown node: " + id);
    }
    return node;
  }

  private void walk(Node source, Function<Node, Set<Node>> nodes, Consumer<Node> consumer) {
    for (var node : nodes.apply(source)) {
      consumer.accept(node);
      walk(node, nodes, consumer);
    }
  }
}
