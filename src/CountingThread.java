
public class CountingThread extends Thread {
	private int threadId;
	private IAtomicCounter counterTree;
	
	public CountingThread(IAtomicCounter counter, int id) {
		counterTree = counter;
		threadId = id;
	}
	
	public int getThreadId() {
		return threadId;
	}
	
	@Override
	public void run() {
		int value;
		try {
			value = counterTree.getAndIncrement();
			System.out.println("Thread " + threadId + ": " + value);
			
		} catch (PanicException e) {
			e.printStackTrace();
		}
	}
}
