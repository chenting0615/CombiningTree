
public class Node {
	enum CStatus {IDLE, FIRST, SECOND, RESULT, ROOT};
	boolean locked;
	CStatus status;
	int firstValue, secondValue;
	int result;
	Node parent;
	
	public Node() {
		status = CStatus.ROOT;
		locked = false;
	}
	
	public Node(Node myParent) {
		parent = myParent;
		status = CStatus.IDLE;
		locked = false;
	}
	
	public boolean precombine() {
		return false;
	}
	
	public int combine(int combined) {
		return 0;
	}
}
