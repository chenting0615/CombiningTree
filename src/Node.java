
public class Node {
	enum CStatus {
		IDLE, FIRST, SECOND, RESULT, ROOT
	};

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

	private void waitIndefinite() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean precombine() throws PanicException {
		while (locked)
			waitIndefinite();

		switch (status) {
		case IDLE:
			status = CStatus.FIRST;
			return true;

		case FIRST:
			locked = true;
			status = CStatus.SECOND;
			return false;

		case ROOT:
			return false;

		default:
			throw new PanicException("precombine: Unexpected state exception: "
					+ status);
		}
	}

	public synchronized int combine(int combined) throws PanicException {
		while (locked)
			waitIndefinite();

		locked = true;
		firstValue = combined;
		switch (status) {
		case FIRST:
			return firstValue;

		case SECOND:
			return firstValue + secondValue;

		default:
			throw new PanicException("combine: Unexpected state exception: "
					+ status);
		}
	}

	public synchronized int op(int combined) throws PanicException {
		switch (status) {
		case ROOT:
			int prior = result;
			result += combined;
			return prior;

		case SECOND:
			secondValue = combined;
			locked = false;
			notifyAll(); // wake up waiting threads

			while (status != CStatus.RESULT)
				waitIndefinite();

			locked = false;
			notifyAll();
			status = CStatus.IDLE;
			return result;

		default:
			throw new PanicException("op: Unexpected state exception: "
					+ status);
		}
	}

	public synchronized void distribute(int prior) throws PanicException {
		switch (status) {
		case FIRST:
			status = CStatus.IDLE;
			locked = false;
			break;

		case SECOND:
			result = prior + firstValue;
			status = CStatus.RESULT;
			break;

		default:
			throw new PanicException("distribute: Unexpected state exception: "
					+ status);
		}

		notifyAll();
	}
}
