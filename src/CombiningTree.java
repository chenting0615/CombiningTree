import java.util.Stack;

public class CombiningTree {
	Node[] leaf;

	public CombiningTree(int width) {
		Node[] nodes = new Node[width - 1];
		nodes[0] = new Node();
		for (int i = 1; i < nodes.length; i++)
			nodes[i] = new Node(nodes[(i - 1) / 2]);

		leaf = new Node[(width + 1) / 2];
		for (int i = 0; i < leaf.length; i++)
			leaf[i] = nodes[nodes.length - i - 1];
	}

	public int getAndIncrement() throws PanicException {
		Stack<Node> nodeStack = new Stack<Node>();
		Node myLeaf = leaf[((CountingThread) Thread.currentThread())
				.getThreadId() / 2];
		Node node = myLeaf;
		
		/* pre-combining phase */
		while(node.precombine())
			node = node.parent;
		
		Node stopNode = node;
		
		/* combining phase */
		node = myLeaf;
		int combined = 1;
		while(node != stopNode) {
			combined = node.combine(combined);
			nodeStack.push(node);
			node = node.parent;
		}
		
		/* operation phase */
		int prior = stopNode.op(combined);
		
		/* distribution phase */
		while(!nodeStack.isEmpty()) {
			node = nodeStack.pop();
			node.distribute(prior);
		}
		
		return prior;
	}
}
