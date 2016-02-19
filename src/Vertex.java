/**
 * Class to represent a vertex of a graph
 * 
 * add class variable cno, start, finish for connected component number
 * @author Peng Li
 * 
 */

import java.util.ArrayList;
import java.util.List;

public class Vertex {
	public int name; // name of the vertex
	public boolean seen; // flag to check if the vertex has already been visited
	public Vertex parent; // parent of the vertex
	public int distance; // distance to the vertex from the source vertex
	public List<Edge> Adj, revAdj; // adjacency list; use LinkedList or
									// ArrayList
	public int cno; // connected component number
	public int start; // start time in DFS visit
	public int finish; // end time in DFS visit
	/*
	 * index of next to-be-added edge in the Adj arraylist during the finding of
	 * Euler tour
	 */
	public int nextEdgeIndex = 0;

	/**
	 * Constructor for the vertex
	 * 
	 * @param n
	 *            : int - name of the vertex
	 */
	Vertex(int n) {
		name = n;
		seen = false;
		parent = null;
		Adj = new ArrayList<Edge>();
		revAdj = new ArrayList<Edge>(); /* only for directed graphs */
	}

	/**
	 * find the next available edge to be added in the Euler tour
	 * 
	 * @return
	 */
	public Edge getNextEdge() {
		Edge next = Adj.get(nextEdgeIndex);
		next.seen = true;
		nextEdgeIndex++;

		return next;
	}

	/**
	 * update nextEdgeIndex for already seen edges
	 */
	public void skipSeenEdge() {
		while ((nextEdgeIndex < Adj.size()) && Adj.get(nextEdgeIndex).seen) {
			nextEdgeIndex++;
		}
	}

	public boolean isExhausted() {
		return nextEdgeIndex == Adj.size();
	}

	/**
	 * Method to represent a vertex by its name
	 */
	public String toString() {
		return Integer.toString(name);
	}
}
