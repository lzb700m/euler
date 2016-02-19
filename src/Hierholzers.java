import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Hierholzers {
	private static Timer timer = new Timer();

	/**
	 * Return an Euler tour of g
	 * 
	 * @param g
	 *            input graph
	 * @return Euler tour of edges if exists, empty list if not
	 */
	public static List<Edge> findEulerTour(Graph g) {
		// List of vertices with odd degree
		List<Vertex> oddEdgeVertices = new LinkedList<>();
		/*
		 * start and end vertices of Euler tour, incase of Euler circle, start
		 * and end are the same vertex
		 */
		Vertex start, end;

		/*
		 * check if input graph g is Eulerian, and if yes - find start and end
		 * vertices
		 */
		if (isEulerian(g, oddEdgeVertices)) {
			// Euler tour exists
			if (oddEdgeVertices.size() == 0) {
				// no odd degree edge
				start = g.verts.get(1);
				end = start;
			} else {
				// 2 odd degree edges
				Vertex x = oddEdgeVertices.get(0);
				Vertex y = oddEdgeVertices.get(1);
				start = (x.name < y.name) ? x : y;
				end = (x.name < y.name) ? y : x;
			}
		} else {
			// graph is not Eulerian, return an empty list
			return new LinkedList<Edge>();
		}

		/*
		 * Find the Euler tour (for both circle and path).
		 */

		Deque<Edge> tempTour = new ArrayDeque<Edge>();
		Deque<Edge> finalTour = new ArrayDeque<Edge>();
		Vertex current = start;
		// find the first tour
		do {
			Edge step = findNextStep(current, g);
			current = step.otherEnd(current);
			tempTour.push(step);
		} while (current != end);

		while (!g.isExhausted()) {
			if (current.isExhausted()) {
				Edge e = tempTour.pop();
				current = e.otherEnd(current);
				finalTour.push(e);
			} else {
				end = current;
				do {
					Edge step = findNextStep(current, g);
					current = step.otherEnd(current);
					tempTour.push(step);
				} while (current != end);
			}
		}

		while (!tempTour.isEmpty()) {
			finalTour.push(tempTour.pop());
		}

		return new LinkedList<Edge>(finalTour);
	}

	/**
	 * Verify if a given tour is a valid Euler tour
	 * 
	 * @param g
	 *            input graph
	 * @param tour
	 *            given tour to be verified
	 * @param start
	 *            start vertex of the tour
	 * @return true if valid, false if not
	 */
	public static boolean verifyTour(Graph g, List<Edge> tour, Vertex start) {
		initGraph(g);

		Iterator<Edge> it = tour.iterator();
		Vertex current = start;
		while (it.hasNext()) {
			Edge step = it.next();
			if (step.seen || !step.containsVertex(current)) {
				// if the edge has been used already or the edges are not
				// connected
				return false;
			}
			step.seen = true;
			current = step.otherEnd(current);
		}

		// check if all edges are exhausted
		for (Vertex u : g) {
			u.skipSeenEdge();
			if (!u.isExhausted()) {
				return false;
			}
		}

		return true;
	}

	/**
	 * find if a given graph is Eulerian (has Eulerian path or circle)
	 * 
	 * @param g
	 *            input graph
	 * @param oddEdgeVertices
	 *            list of Vertices in the graph that has odd degrees
	 * @return true if g is Eulerian, false if not
	 */
	private static boolean isEulerian(Graph g, List<Vertex> oddEdgeVertices) {
		// initialization for dfs visit
		initGraph(g);

		int cno = 0; // connected component number

		for (Vertex u : g) {
			if (!u.seen) {
				dfsVisitIterative(u, ++cno, oddEdgeVertices);
			}
		}
		if (cno > 1
				|| (oddEdgeVertices.size() != 2 && oddEdgeVertices.size() != 0)) {
			// graph not connected or odd degree vertices count more than 2
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Graph initialization for DFS and HierHolzers algorithm
	 * 
	 * @param g
	 */
	private static void initGraph(Graph g) {
		for (Vertex u : g) {
			u.nextEdgeIndex = 0;
			u.seen = false;
			u.cno = 0;
			for (Edge e : u.Adj) {
				e.seen = false;
			}
		}
		g.exhasustedVertexCount = 0;
	}

	/**
	 * iterative dfs visit of the graph to avoid stack overflow (find connected
	 * components, find odd degree vertex and find the vertex with smallest
	 * index)
	 * 
	 * @param x
	 *            start vertex of dfs
	 * @param cno
	 *            connected component number
	 * @param oddEdgeVertices
	 *            list of vertices with odd degree in g
	 * @param start
	 */

	private static void dfsVisitIterative(Vertex x, int cno,
			List<Vertex> oddEdgeVertices) {
		Deque<Vertex> stack = new ArrayDeque<>();
		stack.push(x);
		x.seen = true;
		x.cno = cno;

		while (!stack.isEmpty()) {
			Vertex u = stack.pop();

			if (u.Adj.size() % 2 != 0) {
				oddEdgeVertices.add(u);
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

	/**
	 * helper function to find the next step (edge) for the current tour, also
	 * keep maintaining the internal structure of the graph parameter
	 * 
	 * @param u
	 *            current visiting vertex in the Euler tour
	 * @param g
	 *            input graph
	 * @return next edge to be added in the tour
	 */

	private static Edge findNextStep(Vertex u, Graph g) {
		Edge step = u.getNextEdge();
		Vertex v = step.otherEnd(u);
		u.skipSeenEdge();
		v.skipSeenEdge();
		if (u.isExhausted()) {
			g.exhasustedVertexCount++;
		}
		if (v.isExhausted()) {
			g.exhasustedVertexCount++;
		}
		return step;
	}

	/**
	 * Driver function to test findEulerTour() and verifyTour() method
	 * 
	 * @param args
	 *            input file representing graph
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		/* read graph from file or console */
		Scanner sc;
		if (args.length != 0) {
			File file = new File(args[0]);
			sc = new Scanner(file);
		} else {
			sc = new Scanner(System.in);
		}
		Graph g = Graph.readGraph(sc, false);

		/* find tour and print tour */
		timer.start();
		List<Edge> result = findEulerTour(g);
		timer.end();
		System.out.println(timer);

		for (Edge e : result) {
			System.out.println(e);
		}

		/* given a graph and a tour, find the start vertex and verify tour */
		List<Vertex> oddEdgeVertices = new LinkedList<>();
		Vertex startVertex = null; // vertex with smallest index
		if (isEulerian(g, oddEdgeVertices)) {
			if (oddEdgeVertices.size() == 0) {
				startVertex = g.verts.get(1);
			} else {
				Vertex x = oddEdgeVertices.get(0);
				Vertex y = oddEdgeVertices.get(1);
				startVertex = (x.name < y.name) ? x : y;
			}
		}
		timer.start();
		System.out.println(verifyTour(g, result, startVertex));
		timer.end();
		System.out.println(timer);

	}
}