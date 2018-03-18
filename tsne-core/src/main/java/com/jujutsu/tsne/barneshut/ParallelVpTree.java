package com.jujutsu.tsne.barneshut;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ParallelVpTree<StorageType> extends VpTree<StorageType> {

	private final ForkJoinPool searcherPool;
	
	public ParallelVpTree(ForkJoinPool pool, Distance distance) {
		super(distance);
		searcherPool = pool;
	}
	
	public ParallelVpTree(ForkJoinPool pool) {
		searcherPool = pool;
	}
	
	public List<Future<ParallelTreeNode.TreeSearchResult>> searchMultiple(ParallelVpTree<StorageType> tree, DataPoint [] targets, int k) {
		Collection<ParallelTreeNode.ParallelTreeSearcher> searchers = new ArrayList<>();
		for(int n = 0; n < targets.length; n++) {
			@SuppressWarnings("unchecked")
			ParallelTreeNode node = (ParallelTreeNode) tree.getRoot();
			searchers.add(node.new ParallelTreeSearcher(node,_items,targets[n], k, n));
		}
		return searcherPool.invokeAll(searchers);
	}

	@Override
	protected VpTree<StorageType>.Node createNode() {
		return new ParallelTreeNode();
	}

	class ParallelTreeNode extends VpTree<StorageType>.Node {
		
		class TreeSearchResult {
			final int n;
			final List<Double> distances;
			final List<DataPoint> indices;
			
			TreeSearchResult(List<DataPoint> indices, List<Double> distances, int n) {
				this.indices = indices;
				this.distances = distances;
				this.n = n;
			}

			public List<DataPoint> getIndices() {
				return indices;
			}

			public List<Double> getDistances() {
				return distances;
			}

			
			public int getIndex() {
				return n;
			}

		}

		class ParallelTreeSearcher implements Callable<TreeSearchResult> {
			final Node node;
			Queue<HeapItem> heap;
			final DataPoint target;
			final int k;
			final int n;
			final DataPoint [] items;

			ParallelTreeSearcher(Node tree, DataPoint[] items, DataPoint target, int k, int n) {
				this.node = tree;
				this.target = target;
				this.k = k;
				this.items = items;
				this.n = n;
			}

			@Override
			public TreeSearchResult call() {
				List<DataPoint> indices = new ArrayList<>();
				List<Double> distances = new ArrayList<>();
				PriorityQueue<HeapItem> heap = new PriorityQueue<>(k, (o1, o2) -> -1 * o1.compareTo(o2));

				double tau = Double.MAX_VALUE;
				// Perform the search
				node.search(node, target, k, heap, tau);

				// Gather final results
				while(!heap.isEmpty()) {
					indices.add(items[heap.peek().index]);
					distances.add(heap.peek().dist);
					heap.remove();
				}
				
				// Results are in reverse order 
				Collections.reverse(indices);
				Collections.reverse(distances);

				return new TreeSearchResult(indices, distances,n);
			}   
		}
	}
}
