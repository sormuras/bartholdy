package de.sormuras.bartholdy.util;

import de.sormuras.bartholdy.util.AcyclicDirectedGraph.Edge;
import java.util.Set;

public class CyclicEdgeException extends IllegalArgumentException {

  CyclicEdgeException(Edge edge, Set<Edge> graph) {
    super("Edge " + edge + " creates a cycle in graph:" + graph);
  }

  CyclicEdgeException(String message) {
    super(message);
  }
}
