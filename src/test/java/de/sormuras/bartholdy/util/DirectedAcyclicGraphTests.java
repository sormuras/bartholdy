package de.sormuras.bartholdy.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class DirectedAcyclicGraphTests {

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
                    () -> assertThrows(CycleDetectedException.class, () -> buildGraph(list))));
  }

  @Test
  void ab_bc_ca() {
    var graph = buildGraph(List.of("AB", "BC")); // "A -> B", "A -> B -> C"
    assertThrows(CycleDetectedException.class, () -> graph.addEdge("C", "A"));
  }

  @Test
  void bc_ab_ca() {
    var graph = buildGraph(List.of("BC", "AB")); // "B -> C", "A -> B -> C"
    assertThrows(CycleDetectedException.class, () -> graph.addEdge("C", "A"));
  }

  @Test
  void ab_cd_bc_da() {
    var graph = buildGraph(List.of("AB", "CD")); // "A -> B  C -> D"
    assertDoesNotThrow(() -> graph.addEdge("B", "C"));
    assertThrows(CycleDetectedException.class, () -> graph.addEdge("D", "A"));
  }

  @Test
  void ab_cd_cb_da_bd() {
    var graph = buildGraph(List.of("AB", "CD", "CB")); // "A -> B <- C -> D"
    assertDoesNotThrow(() -> graph.addEdge("D", "A"));
    assertThrows(CycleDetectedException.class, () -> graph.addEdge("B", "D"));
  }

  DirectedAcyclicGraph buildGraph(List<String> edges) {
    var graph = new DirectedAcyclicGraph();
    for (var edge : edges) {
      graph.addEdge("" + edge.charAt(0), "" + edge.charAt(1));
    }
    return graph;
  }
}
