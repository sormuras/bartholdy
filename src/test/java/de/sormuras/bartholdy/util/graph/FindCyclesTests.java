package de.sormuras.bartholdy.util.graph;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FindCyclesTests {

  @Test
  void singleCycle() {
    var graph = buildGraph("AB", "BC", "CA");

    Assertions.assertEquals("A -> {B}\nB -> {C}\nC -> {A}", graph.toString());

    var exception = Assertions.assertThrows(CyclesFoundException.class, graph::topologicalSort);

    Assertions.assertLinesMatch(
        List.of("found 1 cycle(s) in graph:", "  C -> B -> A"),
        List.of(exception.getMessage().split("\\R")));

    Assertions.assertTrue(graph.find(node -> node.isMarked(Mark.class)).isEmpty());
  }

  Graph<String> buildGraph(String... edges) {
    var graph = new Graph<String>();
    for (var edge : edges) {
      var source = graph.findOrCreateNode(edge.substring(0, 1));
      var target = graph.findOrCreateNode(edge.substring(1, 2));
      source.linkedTo(target);
    }
    return graph;
  }
}
