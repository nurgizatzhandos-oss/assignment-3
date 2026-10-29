# Smart City Infrastructure & Route Planning

> A reproducible Java study of graph algorithms for planning city infrastructure and routes.

This project models districts as vertices and roads or service dependencies as weighted edges. It implements two minimum spanning tree algorithms, directed graph connectivity tools, and single-source shortest paths. The benchmark uses fixed-seed generated graphs and records timing and operation counts.

## Contents

- [Overview](#overview)
- [Implementation](#implementation)
- [Complexity analysis](#complexity-analysis)
- [Experimental setup](#experimental-setup)
- [Results](#results)
- [Comparison and discussion](#comparison-and-discussion)
- [Testing](#testing)
- [Run the project](#run-the-project)
- [Conclusion](#conclusion)

## Overview

`WeightedGraph` stores adjacency lists and an edge list. Each graph explicitly declares whether it is directed. Vertices are numbered `0..V-1`; integer edge weights represent construction costs or travel distances. MST and Dijkstra inputs use non-negative weights. The graph data structure itself accepts negative weights so Dijkstra can reject them explicitly.

## Implementation

| Package | Responsibility |
|---|---|
| `smartcity.graph` | Weighted graph model and adjacency lists |
| `smartcity.dsu` | Disjoint Set Union with path compression and union by size |
| `smartcity.mst` | Kruskal's and Prim's minimum spanning tree algorithms |
| `smartcity.connectivity` | Kahn topological ordering and Kosaraju SCC decomposition |
| `smartcity.shortestpath` | Dijkstra with predecessor-based path reconstruction |
| `smartcity.benchmark` | Seeded graph generation and five-repeat timing harness |

Kruskal sorts edges by weight, then uses DSU to accept only edges that join distinct components. Prim grows a tree using a min-priority queue. Both return a connectedness flag and `Long.MAX_VALUE` as the weight when an MST does not exist. Topological ordering uses Kahn's indegree queue and reports a cycle if it cannot emit all vertices. Kosaraju performs DFS passes over the graph and its transpose. Dijkstra uses a binary heap, ignores stale queue entries, stores predecessors, and throws an exception for any negative edge.

## Complexity analysis

Assume adjacency lists, comparison sorting, a binary heap, and `V` vertices / `E` edges.

| Algorithm | Time | Extra space | How size affects work |
|---|---:|---:|---|
| DSU `find` / `union` | Amortized `O(α(V))` per operation | `O(V)` total | Path compression flattens traversed paths; union by size attaches the smaller tree below the larger. Together they give near-constant amortized operations. |
| Kruskal | `O(E log E + E α(V))` | `O(V + E)` | Sorting dominates; denser graphs increase sorting and DSU checks. |
| Prim (binary heap) | `O((V + E) log V)` | `O(V + E)` | Each reachable adjacency can enter the heap; more roads increase heap work. |
| Kosaraju SCC | `O(V + E)` | `O(V + E)` | Each vertex and directed edge is traversed a constant number of times across the two passes. |
| Kahn topological order | `O(V + E)` | `O(V)` | A DAG processes all edges; a cyclic graph may stop after processing only vertices reachable from initial zero-indegree vertices. |
| Dijkstra (binary heap) | `O((V + E) log V)` | `O(V + E)` | More edges mean more relaxations; unreachable vertices are not reached from the source. |

For DSU, `α(V)` is the inverse Ackermann function and is at most a tiny constant for practical input sizes. Dijkstra's greedy finalization depends on non-negative weights: with a negative edge, a vertex removed from the priority queue can later receive a shorter distance, invalidating the algorithm's correctness.

## Experimental setup

The benchmark uses `System.nanoTime()`, two warm-up runs and then five measured runs per algorithm and graph size. It reports integer mean nanoseconds and mean operation counts. The input graphs have the assignment's `(V, E)` configurations: `(10, 30)`, `(100, 500)`, and `(1,000, 5,000)`. Fixed seeds are recorded per algorithm family in the CSV.

- **Undirected graphs:** start with a random spanning tree, then add unique edges until the target `E` is reached. This guarantees connected MST inputs.
- **DAGs:** add `E` unique arcs with `from < to`, which guarantees acyclicity.
- **Cyclic directed graphs:** begin with a directed ring, then add unique non-self arcs. This guarantees at least one cycle.
- All generated weights are uniformly selected integers in `[1, 1000]`.
- Operation counts are edge examinations / queue or DSU work as implemented. For SCC, the reported count is `2E`, the number of directed adjacency entries traversed across both Kosaraju passes. Counts are algorithm-specific and should not be compared as if they were identical machine instructions.

The results are illustrative measurements from one Java 25 runtime in a shared desktop environment. Small-run timings are especially sensitive to JIT compilation, scheduling and system load. Re-running the benchmark can produce different runtimes even though the generated inputs are deterministic.

## Results

### Mean runtime (milliseconds)

| Graph | Kruskal | Prim | Topological DAG | SCC DAG | Topological cyclic | SCC cyclic | Dijkstra |
|---|---:|---:|---:|---:|---:|---:|---:|
| Small (10, 30) | 0.033 | 0.073 | 0.023 | 0.034 | 0.003 | 0.019 | 0.034 |
| Medium (100, 500) | 0.321 | 0.366 | 0.102 | 0.153 | 0.023 | 0.101 | 0.246 |
| Large (1,000, 5,000) | 1.414 | 2.462 | 0.704 | 1.085 | 0.044 | 0.561 | 1.046 |

Detailed nanosecond timings, operation counts, seeds and repeat counts are in [`results/tables/benchmark.csv`](results/tables/benchmark.csv).

### Runtime by graph size

Both plots use a logarithmic runtime axis so the smaller graph measurements remain visible alongside the largest measurements.

![Mean execution time by vertex count](results/plots/runtime-vs-vertices.svg)

![Mean execution time by edge count](results/plots/runtime-vs-edges.svg)

### Mean operation counts

| Graph | Kruskal | Prim | Topological DAG | SCC (either) | Topological cyclic | Dijkstra |
|---|---:|---:|---:|---:|---:|---:|
| Small | 165 | 113 | 30 | 60 | 0 | 81 |
| Medium | 2,919 | 1,989 | 500 | 1,000 | 0 | 1,191 |
| Large | 29,444 | 19,985 | 5,000 | 10,000 | 0 | 12,022 |

The cyclic topological run reports zero edge examinations because the generated ring has no zero-indegree vertex; Kahn's algorithm detects failure immediately. The SCC count is exactly two edge traversals per directed edge.

## Comparison and discussion

Both MST implementations returned the same minimum weight on the demonstration input, as required. Kruskal ran faster than Prim in these generated cases; this is an observation for these graph densities and this runtime, not a general guarantee. Kruskal's edge sort is a good fit when a sorted edge list is available, while Prim is a natural fit for incrementally growing a connected network from a start vertex. DSU's path compression and size balancing keep Kruskal's component checks negligible relative to sorting.

Topological ordering and SCC show approximately linear growth on the tested graph sizes, consistent with `O(V+E)`. Kahn's cycle case was faster because it stopped without scanning the ring's edges. Dijkstra also grew with graph size and edge count as expected for a heap-based adjacency-list implementation. Its current benchmark uses an undirected connected graph, so every vertex is reachable; the correctness checks separately cover unreachable vertices.

## Testing

`src/test/java/smartcity/AlgorithmChecks.java` is a dependency-free assertion-based check program. It covers:

- connected graph with multiple valid MSTs and duplicate weights (Kruskal / Prim agree);
- disconnected graph and single-vertex MST behavior;
- valid topological order and cycle detection;
- SCC partition membership (each of four vertices appears exactly once);
- shortest path reconstruction, an unreachable vertex, and negative-weight rejection.

Run it with assertions enabled: `java -ea -cp build/classes smartcity.AlgorithmChecks`.

## Run the project

Requires Java 17 or later. From the repository root:

```bash
mkdir -p build/classes
javac -d build/classes $(find src/main/java src/test/java -name '*.java')
java -cp build/classes smartcity.Main
java -ea -cp build/classes smartcity.AlgorithmChecks
java -cp build/classes smartcity.Main benchmark results/tables
```

The final command overwrites `results/tables/benchmark.csv` with a fresh run. Generated graphs remain deterministic; measured times are machine-dependent.

## Conclusion

Kruskal and Prim both produce minimum-cost connected infrastructure; their practical performance depends on graph representation and density. DSU makes Kruskal's cycle checks efficient. Kosaraju identifies mutually reachable service groups in linear time, while topological ordering both orders dependencies and flags cycles. Dijkstra provides efficient routes when all road costs are non-negative. The source code, reproducible benchmark, tables, plots, and this report are included together for review.
