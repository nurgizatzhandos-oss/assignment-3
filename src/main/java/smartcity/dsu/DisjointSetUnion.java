package smartcity.dsu;

/** Union-find with path compression and union by size. */
public final class DisjointSetUnion {
    private final int[] parent, size;
    private int components;
    private long finds, unions;
    public DisjointSetUnion(int n) {
        if (n < 0) throw new IllegalArgumentException("size must be non-negative");
        parent = new int[n]; size = new int[n]; components = n;
        for (int i = 0; i < n; i++) { parent[i] = i; size[i] = 1; }
    }
    public int find(int x) {
        finds++;
        if (parent[x] != x) parent[x] = find(parent[x]);
        return parent[x];
    }
    public boolean union(int a, int b) {
        unions++;
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (size[ra] < size[rb]) { int t = ra; ra = rb; rb = t; }
        parent[rb] = ra; size[ra] += size[rb]; components--;
        return true;
    }
    public int components() { return components; }
    public long findCalls() { return finds; }
    public long unionCalls() { return unions; }
}
