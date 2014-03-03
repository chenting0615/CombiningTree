import java.util.ArrayList;
import java.util.List;


public class SharedCountingMain {
	public static void main(String[] args) {
		List<CountingThread> threadList = new ArrayList<CountingThread>();
		
		CombiningTree counterTree = new CombiningTree(8);
		for(int i = 0; i < 5; i++) {
			CountingThread thread = new CountingThread(counterTree, i + 1);
			threadList.add(thread);
			thread.start();
		}
		
		for(CountingThread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
