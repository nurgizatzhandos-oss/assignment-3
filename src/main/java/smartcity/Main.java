package smartcity;

import smartcity.benchmark.Benchmark;
import smartcity.connectivity.ConnectivityAlgorithms;
import smartcity.graph.WeightedGraph;
import smartcity.mst.MstAlgorithms;
import smartcity.shortestpath.Dijkstra;
import java.nio.file.Path;
import java.util.Arrays;

public final class Main {
    private Main() {}
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("benchmark")) {
            Path dir = args.length > 1 ? Path.of(args[1]) : Path.of("results/tables");
            Benchmark.run(dir); System.out.println("Benchmark written to " + dir.resolve("benchmark.csv")); return;
        }
        WeightedGraph roads = new WeightedGraph(5, false);
        roads.addEdge(0,1,4); roads.addEdge(0,2,2); roads.addEdge(1,2,1); roads.addEdge(1,3,5); roads.addEdge(2,3,8); roads.addEdge(2,4,10); roads.addEdge(3,4,2);
        var k = MstAlgorithms.kruskal(roads); var p = MstAlgorithms.prim(roads);
        System.out.printf("MST: Kruskal=%d, Prim=%d, connected=%s%n", k.weight(), p.weight(), k.connected());
        WeightedGraph services = new WeightedGraph(5, true); services.addEdge(0,1,1);services.addEdge(1,2,1);services.addEdge(2,0,1);services.addEdge(2,3,1);services.addEdge(3,4,1);
        System.out.println("Topological ordering exists: " + ConnectivityAlgorithms.topologicalOrder(services).acyclic());
        System.out.println("Strongly connected components: " + ConnectivityAlgorithms.kosaraju(services));
        var route = Dijkstra.run(roads, 0);
        System.out.println("Shortest distances from 0: " + Arrays.toString(route.distances()));
        System.out.println("Path 0 -> 4: " + route.pathTo(4));
    }
}
