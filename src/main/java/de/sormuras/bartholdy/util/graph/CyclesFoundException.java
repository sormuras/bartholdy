package de.sormuras.bartholdy.util.graph;

import static java.util.stream.Collectors.*;

import java.util.List;

public class CyclesFoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  @SuppressWarnings({"unchecked", "rawtypes"})
  <T> CyclesFoundException(List<List<Node<T>>> cycles) {
    this.cycles = (List) cycles;
  }

  private final List<List<Node<?>>> cycles;

  @Override
  public String getMessage() {
    return "found "
        + cycles.size()
        + " cycle(s) in graph:\n  "
        + String.join("\n  ", getCyclesAsListOfStrings());
  }

  public List<String> getCyclesAsListOfStrings() {
    return cycles
        .stream()
        .map(cycle -> Graph.listToString(" -> ", cycle, node -> node.getValue().toString()))
        .collect(toList());
  }
}
