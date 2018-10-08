package de.sormuras.bartholdy.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class AcyclicDirectedGraphTests {

  @TestFactory
  Stream<DynamicTest> acyclic() {
    var samples =
        List.of(
            List.of("AB"),
            List.of("BA"),
            List.of("AB", "BC"),
            List.of("AB", "AC"),
            List.of("AB", "AC", "BC"),
            List.of("AB", "AC", "CB"),
            List.of("AB", "AD", "CD", "BC", "BD", "AC"),
            List.of("AB", "AF", "CD", "DE", "CE", "BD", "FD", "BC"));

    return samples.stream().map(list -> dynamicTest(list.toString(), () -> buildGraph(list)));
  }

  @TestFactory
  Stream<DynamicTest> cyclic() {
    var samples =
        List.of(
            List.of("AA"),
            List.of("AB", "BA"),
            List.of("BA", "AB"),
            List.of("AB", "BC", "CA"),
            List.of("AB", "BC", "CB"),
            List.of("AB", "CB", "BA"),
            List.of("AB", "BC", "CD", "DA"),
            List.of("AB", "AD", "CD", "BC", "BD", "AC", "DA"),
            List.of("AB", "AF", "CD", "DE", "CE", "BD", "FD", "BC", "ED"),
            List.of("AB", "AF", "CD", "DE", "CE", "BD", "FD", "BC", "EA"));

    return samples
        .stream()
        .map(
            list ->
                dynamicTest(
                    list.toString(),
                    () -> assertThrows(CyclicEdgeException.class, () -> buildGraph(list))));
  }

  @Test
  void ab_bc_ca() {
    var graph = buildGraph(List.of("AB", "BC")); // "A -> B", "A -> B -> C"
    assertThrows(CyclicEdgeException.class, () -> graph.addEdge("C", "A"));
  }

  @Test
  void bc_ab_ca() {
    var graph = buildGraph(List.of("BC", "AB")); // "B -> C", "A -> B -> C"
    assertThrows(CyclicEdgeException.class, () -> graph.addEdge("C", "A"));
  }

  @Test
  void islands() {
    var graph = buildGraph(List.of("AB", "CD")); // "A -> B  C -> D"
    graph.addEdge("B", "C");
    assertThrows(CyclicEdgeException.class, () -> graph.addEdge("D", "A"));
  }

  AcyclicDirectedGraph buildGraph(List<String> edges) {
    var labels = new HashSet<String>();
    for (var edge : edges) {
      for (char ch : edge.toCharArray()) {
        labels.add("" + ch);
      }
    }
    var graph = new AcyclicDirectedGraph(labels);
    for (var edge : edges) {
      graph.addEdge("" + edge.charAt(0), "" + edge.charAt(1));
    }
    return graph;
  }
}
