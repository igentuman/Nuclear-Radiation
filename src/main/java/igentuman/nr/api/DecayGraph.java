package igentuman.nr.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DecayGraph {

    private static final Map<String, List<DecayEdge>> EDGES = new HashMap<>();

    private DecayGraph() {}

    public static void addEdge(String fromIsotopeId, DecayEdge edge) {
        EDGES.computeIfAbsent(fromIsotopeId, k -> new ArrayList<>()).add(edge);
    }

    public static List<DecayEdge> outputs(String fromIsotopeId) {
        return EDGES.getOrDefault(fromIsotopeId, Collections.emptyList());
    }

    public static void clear() { EDGES.clear(); }
}
