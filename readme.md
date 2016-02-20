#Finding Euler tours

##Summary:
A graph G is called Eulerian if it is connected and the degree of every vertex is an even number. It is known that such graphs aways have a tour (a cycle that may not be simple) that goes through every edge of the graph exactly once. Such a tour (sometimes called a circuit) is called an Euler tour. If the graph is connected, and it has exactly 2 nodes of odd degree, then it has an Euler Path connecting these two nodes, that includes all the edges of the graph exactly once.

Our program provides the following 2 method for applications:

```java
static List<Edge> findEulerTour(Graph g) {
    // Return an Euler tour of g
}

static boolean verifyTour(Graph g, List<Edge> tour, Vertex start) {
    // verify tour is a valid Euler tour
}
```


The algorithm that we implement to find the Euler tour is called Hierholzer's algorithm (more on [wikipedia Eulerian_path]). The running time is bounded by **O(|E|)**. On a large input graph with 500000 vertices and  5249924 edges, out implementation can output the Euler tour in about **2 seconds** and verify a given tour in **less than 1 second**.

##Input Output specification:
The input graph can be passed from console or from a file, the format of the input is as follows:

First line contains 2 integers: the number of vertices and edges in the graph;

From the second line on, each line contains 3 integers representing an edge in the graph: the start vertex, the end vertex and the weight of the edge;

Output the Euler tour as a list of **edges**.

Below is a sample input and output:

```
Sample input
6 10
1 2 1
1 3 1
1 4 1
1 6 1
2 3 1
3 6 1
3 4 1
4 5 1
4 6 1
5 6 1

Sample output
(1,2)
(2,3)
(3,6)
(4,6)
(4,5)
(5,6)
(1,6)
(1,3)
(3,4)
(1,4)
```
The actual tour is 1->2->3->6->4->5->6->1->3->4->1. If the edge (1,4) did not exist, then the graph has an Euler path between 1 and 4, and the output is same as above, except for the last line is not there. In this case, the algorithm outputs the path starting at node 1, which is the smaller node of 1 and 4.

##Class description:
**Vertex.java:** Class to represent graph vertex

**Edge.java:** Class to represent graph edge

**Graph.java:** Class to represent undirected graph

**Hierholzers.java:** implementation for Hierholzers' algorithm

To run the program, in terminal:

```
$ javac Hierholzers.java
$ java Hierholzers [input_graph.txt]
```


##Algorithm:
Below is the pseudocode for finding the Euler tour:

```
findEulerTour(undirected graph g):
	run DFS on g;
	if (g is not connected, or g has more than 2 vertices with odd degree):
		return; // Euler tour not exists in such graph
	else:
		if (g has 0 vertex with odd degree):
			start <- any vertex in g;
			end <- start;
		else: // g has 2 vertices with odd degree (say v1, v2)
			start <- v1;
			end <- v2;
	
	Stack tempTour <- find any tour from start to end;
	Vertex current <- end;
	Stack finalTour <- empty list;
	while (NOT all edges visited):
		if (all edges of current is visited):
			step <- tempTour.pop();
			current <- step.otherEnd(current);
			finalTour.push(step);
		else:
			find another tour from current to current, and push it into tempTour;
	
	while (tempTour NOT empty):
		finalTour.push(tempTour.pop());
	
	return finalTour;

```
Below is the pseudocode for verifying a given Euler tour together with a given start vertex:

```
verifyTour(Vertex start, List of Edges tour, undirected graph g):
	current <- start;
	tourIterator <- tour.iterator();
	
	while (tourIterator.hasNext()):
		step <- tourIterator.next();
		if (step has been visited, or step does not contain current):
			return false; // step has been used more than onece, or the given tour is not connected by vertex
		
		set step visited;
		current <- step.otherEnd(current);
	
	if (all edges in g are visited):
		return true;
	else:
		return false; // tour does not cover all edges
	
```

##Implementation chanllenges:
###DFS on large input
The first step to find a tour is to check if such tour exists, this requires a DFS traverse of the input graph. If we implement the dfsVisit() function recursively, on a large input we encountered stack overflow exception. The easier solution to this is to increase the stack size of the JVM. Our solution was to rewrite the DFS in iterative mannaer instead of recursive.

###Track the usage of edges in tour
In this problem, the outer loop condition is to check if all edges in the original graph has been added into the tour. Therefore we need to track the use of each edge of each vertex **efficiently**. This is by far the biggest chanllenge we encountered in this project.

Our original implementation was to use a hash table: each vertex as a key, and the list of edges connecting this vertex as value. It turned out to be very inefficient updating the hash table, because not all update operation is guaranteed to be O(1).

A better approach is as follows:

Add a boolean field in Edge class indicating the edge has been added into the Euler tour:

```
public class Edge {
	...
	public boolean seen; // indicator if been visited
	...
}
```

Use array in Vertex class to store the edges adjacent to it, and created a index variable to track what is the next unvisited edge in the adjacent list:

```
public class Vertex {
	...
	public List<Edge> Adj // adjacency list; use ArrayList
	/*
	 * index of next to-be-added edge in the Adj arraylist during the finding of
	 * Euler tour
	 */
	public int nextEdgeIndex = 0;
	...
	
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
	...
}
```
Everytime we need to advance the Euler tour from the current vertex, we first check if the current vertex is exhausted (current.isExhausted == false). And if not exhausted, we can obtain the next unvisited edge by using the index variable we defined in Vertex class (current.getNextEdge()). The getNextEdge() method can return the next unvisited edge in O(1) time and advance the index variable to the next edge in the adjacency list.

This approach works because the order of the edges in the Euler tour does not matter, all we needs to find is "A" Euler tour, not a particular one.

Yet there is still another problem we need to consider, as the input graph is an undirected graph, the same edge appreas twice in 2 different Vertex' adjacency list. After calling current.getNextEdge(), the index variable for current is advanced, but the index variable for the opponent vertex is not. Therefore, before calling getNextEdge() method, we need to skip the already visited edges in the adjacency list by calling skipSeenEdge(), this makes sure that the index variable always point to a unvisited edge before we want to retrieve one.

All the operation used to track the use of the edges takes O(1) time, and add only little overhead to the overall implementation.

##Result:
Running time for different input size:

Graph with 100 vertices and 1050 edges:

```
Find Euler tour:
Time: 7 msec.
Memory: 6 MB / 257 MB.
Verify Euler tour:
Time: 2 msec.
Memory: 6 MB / 257 MB.
```

Graph with 1000 vertices and 5506 edges:

```
Find Euler tour:
Time: 23 msec.
Memory: 14 MB / 257 MB.
Verify Euler tour:
Time: 4 msec.
Memory: 16 MB / 257 MB.
```

Graph with 500000 vertices and 5249924 edges:

```
Find Euler tour:
Time: 2520 msec.
Memory: 1259 MB / 1848 MB.
Verify Euler tour:
Time: 541 msec.
Memory: 1285 MB / 1848 MB.
```


















[wikipedia Eulerian_path]: https://en.wikipedia.org/wiki/Eulerian_path