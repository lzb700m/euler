

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HierholzersEulerPath {

	/*
	 * Return an Euler tour of g
	 */
	public static List<Edge> findEulerTour(Graph g) {

		// initialization for dfs visit
		for (Vertex u : g) {
			u.seen = false;
			u.cno = 0;
		}

		int cno = 0; // connected component number
		// List of vertices with odd degree
		List<Vertex> oddEdgeVertices = new LinkedList<>();
		Vertex start = null; // vertex with smallest index
		Vertex end = null;

		for (Vertex u : g) {
			if (!u.seen) {
				start = u;
				dfsVisitIterative(u, ++cno, oddEdgeVertices, start);
			}
		}

		if (cno > 1 || oddEdgeVertices.size() > 2) {
			// graph not connected or odd degree vertices count more than 2
			throw new IllegalArgumentException("Graph is not Eulerian");
		} else if (oddEdgeVertices.size() == 2) {
			Vertex x = oddEdgeVertices.get(0);
			Vertex y = oddEdgeVertices.get(1);
			/*
			 * Euler path exists, start and end from the odd degree vertex.
			 * start vertex's index should be smaller than end vertex according
			 * to input-output specification.
			 */
			if (x.name < y.name) {
				start = x;
				end = y;
			} else {
				start = y;
				end = x;
			}
		} else {
			/*
			 * Euler tour exists, tour starts and ends at the same vertex
			 */
			end = start;
		}

		/*
		 * Find the Euler trail (for both tour and path). Map (remaining)
		 * containing currently unvisited edges of each vertex, if all edges of
		 * a vertex are visited, the map entry of the corresponding vertex will
		 * be removed from map. Initialization of remaining edges: all vertices
		 * and edges are added
		 */
		Map<Vertex, List<Edge>> remaining = buildGraphMap(g);

		List<Edge> ret = new LinkedList<>(); // result set to be returned
		// list of edges of current tour
		List<Edge> currentTour = new LinkedList<>();
		Vertex current = start; // currently visited vertex
		Vertex currentEnd = end; // end vertex of current tour
		/*
		 * The position of result list where the current tour should be inserted
		 */
		int insertPosition = 0;

		/*
		 * outer loop invariant:
		 */
		while (!remaining.isEmpty()) {
			currentTour = new LinkedList<>();

			/*
			 * inner loop invariant:
			 */
			do {
				Edge step = findNextStep(current, remaining);
				current = step.otherEnd(current);
				currentTour.add(step);
			} while (current != currentEnd);

			// insert current tour into final result set
			ret.addAll(insertPosition, currentTour);

			// find the starting node for next round
			insertPosition = 0;
			Iterator<Edge> it1 = ret.iterator();
			Iterator<Edge> it2 = ret.iterator();
			if (it2.hasNext()) {
				// it2 is one edge ahead of it1
				it2.next();
			}
			while (it2.hasNext()) {
				insertPosition++;
				Edge frontEdge = it2.next();
				Edge behindEdge = it1.next();
				Vertex intersect = findIntersectVertex(frontEdge, behindEdge);
				/*
				 * find the first vertex in current result that has unvisited
				 * edges, and the next tour will start from here
				 */
				if (intersect != null && remaining.containsKey(intersect)) {
					current = intersect;
					currentEnd = intersect;
					break;
				}
			}
		}
		return ret;
	}

	// TODO
	public static boolean verifyTour(Graph g, List<Edge> tour, Vertex start) {
		for (Vertex u : g) {
			for (Edge e : u.Adj) {
				e.seen = false;
			}
		}

		Iterator<Edge> tourIterator = tour.iterator();
		Vertex currentVertex = start;

		while (tourIterator.hasNext()) {
			Edge step = tourIterator.next();
			if (currentVertex.Adj.contains(step) && !step.seen) {
				step.seen = true;
			} else {
				return false;
			}
			currentVertex = step.otherEnd(currentVertex);
		}

		for (Vertex u : g) {
			for (Edge e : u.Adj) {
				if (!e.seen) {
					return false;
				}
			}
		}

		return true;
	}

	public static boolean verifyTour2(Graph g, List<Edge> tour, Vertex start) {
		// verify tour is a valid Euler tour
		Map<Vertex, List<Edge>> remaining = buildGraphMap(g);
		Iterator<Edge> tourIterator = tour.iterator();
		Vertex currentVertex = start;

		while (tourIterator.hasNext()) {
			Edge step = tourIterator.next();
			try {
				currentVertex = verifyOneStep(remaining, step, currentVertex);
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				return false;
			}
		}
		return remaining.isEmpty();
	}

	private static Map<Vertex, List<Edge>> buildGraphMap(Graph g) {
		Map<Vertex, List<Edge>> ret = new HashMap<>();

		for (Vertex u : g) {
			List<Edge> value = new LinkedList<>();
			for (Edge e : u.Adj) {
				value.add(e);
			}
			ret.put(u, value);
		}
		return ret;
	}

	/*
	 * iterative dfs visit of the graph to avoid stack overflow (find connected
	 * components, find odd degree vertex and find the vertex with smallest
	 * index)
	 */
	private static void dfsVisitIterative(Vertex x, int cno,
			List<Vertex> oddEdgeVertices, Vertex start) {
		Deque<Vertex> stack = new ArrayDeque<>();
		stack.push(x);
		x.seen = true;
		x.cno = cno;

		while (!stack.isEmpty()) {
			Vertex u = stack.pop();

			if (u.Adj.size() % 2 != 0) {
				oddEdgeVertices.add(u);
			}

			// find the vertex with smallest index as the start of the tour
			if (start.name > u.name) {
				start = u;
			}

			for (Edge e : u.Adj) {
				Vertex v = e.otherEnd(u);
				if (!v.seen) {
					v.seen = true;
					v.cno = cno;
					stack.push(v);
				}
			}
		}
	}

	/*
	 * helper function to find the next step (edge) for the current tour, also
	 * keep maintaining the internal structure of the remaining edge map
	 */
	private static Edge findNextStep(Vertex u, Map<Vertex, List<Edge>> remaining) {
		List<Edge> edgeSetU = remaining.get(u);
		// always use the first edge in the adjacency list
		Edge step = edgeSetU.get(0);
		Vertex v = step.otherEnd(u);
		List<Edge> edgeSetV = remaining.get(v);
		edgeSetU.remove(step);
		if (edgeSetU.size() == 0) {
			remaining.remove(u);
		}

		edgeSetV.remove(step);
		if (edgeSetV.size() == 0) {
			remaining.remove(v);
		}
		return step;
	}

	/*
	 * helper method to find intersect vertex of 2 edges, return null if no such
	 * vertex exists
	 */
	private static Vertex findIntersectVertex(Edge e1, Edge e2) {
		if (e1.From == e2.From || e1.From == e2.To) {
			return e1.From;
		} else if (e1.To == e2.From || e1.To == e2.To) {
			return e1.To;
		} else {
			return null;
		}
	}

	/*
	 * helper method to verify one single step and maintain remaining map
	 */
	private static Vertex verifyOneStep(Map<Vertex, List<Edge>> remaining,
			Edge step, Vertex start) throws IllegalArgumentException {
		Vertex end = null;
		// verify if step connects with start vertex
		if (step.From == start || step.To == start) {
			end = step.otherEnd(start);
		} else {
			throw new IllegalArgumentException(
					"Input tour is not a connected list of edges");
		}

		/*
		 * verify if there still exists edge (step) connecting start in
		 * remaining map
		 */
		if (remaining.containsKey(start) && remaining.containsKey(end)) {
			List<Edge> startEdgeSet = remaining.get(start);
			List<Edge> endEdgeSet = remaining.get(end);
			if (startEdgeSet.contains(step) && endEdgeSet.contains(step)) {
				startEdgeSet.remove(step);
				endEdgeSet.remove(step);
				if (startEdgeSet.isEmpty()) {
					remaining.remove(start);
				}
				if (endEdgeSet.isEmpty()) {
					remaining.remove(end);
				}
			} else {
				throw new IllegalArgumentException(
						"Remaining graph does not contain edge: " + step + ".");
			}
		} else {
			throw new IllegalArgumentException("All edges of Vertex " + start
					+ " or Vertex " + end + " has been visited.");
		}
		// return the other end of the step
		return end;
	}

	/*
	 * Driver function to test the algorithm
	 */
	public static void main(String[] args) throws FileNotFoundException {
		Scanner sc;

		if (args.length != 0) {
			File file = new File(args[0]);
			sc = new Scanner(file);
		} else {
			sc = new Scanner(System.in);
		}

		Graph g = Graph.readGraph(sc, false);

		List<Edge> result = new ArrayList<>();

		Long start = System.currentTimeMillis();
		try {
			result = findEulerTour(g);
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			return;
		}
		Long end = System.currentTimeMillis();

		System.out
				.println("Find trail running time: " + (end - start) + " ms.");

		for (Edge e : result) {
			System.out.println(e);
		}

		// demostrate verifyTour() method
		// initialization for dfs visit
		for (Vertex u : g) {
			u.seen = false;
			u.cno = 0;
		}

		int cno = 0; // connected component number
		// List of vertices with odd degree
		List<Vertex> oddEdgeVertices = new LinkedList<>();
		Vertex startVertex = null; // vertex with smallest index

		for (Vertex u : g) {
			if (!u.seen) {
				startVertex = u;
				dfsVisitIterative(u, ++cno, oddEdgeVertices, startVertex);
			}
		}

		if (cno > 1 || oddEdgeVertices.size() > 2) {
			return;
		} else if (oddEdgeVertices.size() == 2) {
			Vertex x = oddEdgeVertices.get(0);
			Vertex y = oddEdgeVertices.get(1);
			startVertex = (x.name < y.name) ? x : y;
		}

		start = System.currentTimeMillis();
		System.out.println(verifyTour(g, result, startVertex));
		end = System.currentTimeMillis();
		System.out.println("Verify trail running time: " + (end - start)
				+ " ms.");
	}

}