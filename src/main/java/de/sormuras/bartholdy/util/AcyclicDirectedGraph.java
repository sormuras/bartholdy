package de.sormuras.bartholdy.util;

import java.util.HashMap;
import java.util.Map;
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
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (other == null || getClass() != other.getClass()) {
        return false;
      }
      return id.equals(((Base) other).id);
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
  private final Set<Edge> antis;
  private final Set<Edge> banned;

  AcyclicDirectedGraph(Set<String> nodeIds) {
    this.nodes = new HashMap<>();
    this.edges = new TreeSet<>();
    this.antis = new TreeSet<>();
    this.banned = new TreeSet<>();

    nodeIds.forEach(id -> nodes.put(id, new Node(id)));
  }

  void addEdge(String sourceId, String targetId) {
    var source = node(sourceId);
    var target = node(targetId);
    if (source == target) {
      throw new CyclicEdgeException("Same node: " + source + " == " + target);
    }
    // create edge and check if it is illegal
    var edge = new Edge(source, target);
    if (banned.contains(edge) || antis.contains(edge)) {
      throw new CyclicEdgeException(edge, edges);
    }
    // add edge to set of "good" edges, i.e. the acyclic directed graph
    if (!edges.add(edge)) {
      // duplicated edge, nothing further to do
      return;
    }

    // ban anti-edge and potentially remove it from banned set
    var anti = new Edge(target, source);
    antis.add(anti);
    banned.remove(anti);

    // remember node's connections
    source.outbounds.add(target);
    target.inbounds.add(source);

    // ban all other
    var sources = new TreeSet<Node>();
    var targets = new TreeSet<Node>();
    walk(source, node -> node.outbounds, sources::add);
    walk(target, node -> node.inbounds, targets::add);
    for (var from : sources) {
      for (var to : targets) {
        if (from == to) {
          continue;
        }
        var ban = new Edge(from, to);
        if (antis.contains(ban)) {
          continue;
        }
        banned.add(ban);
      }
    }
  }

  private Node node(String id) {
    var node = nodes.get(id);
    if (node == null) {
      throw new IllegalArgumentException("Unknown node: " + id);
    }
    return node;
  }

  private void walk(Node root, Function<Node, Set<Node>> nodes, Consumer<Node> consumer) {
    for (var node : nodes.apply(root)) {
      consumer.accept(node);
      walk(node, nodes, consumer);
    }
  }
}
