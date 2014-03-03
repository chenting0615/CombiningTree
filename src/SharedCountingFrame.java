import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import javax.swing.JButton;
import javax.swing.JFrame;

public class SharedCountingFrame extends JFrame implements IAtomicCounter,
		ActionListener {
	private final int WIDTH = 800;
	private final int HEIGHT = 600;

	private NodePanel[] nodePanels;
	private NodePanel[] leaf = null;
	private int panelCount;

	public SharedCountingFrame(int width) {
		this.setLayout(null);

		setSize(WIDTH, HEIGHT);

		int midX = WIDTH / 2;
		int midY = HEIGHT / 2;

		nodePanels = new NodePanel[width];
		panelCount = 0;

		NodePanel root = new NodePanel(20, 10);

		int nodeWidthMid = root.getWidth() / 2;

		root.setBounds(midX - nodeWidthMid, 10, root.getWidth(),
				root.getHeight());
		this.add(root);
		nodePanels[panelCount++] = root;

		NodePanel leftChild = new NodePanel(20, 10, root);
		leftChild.setBounds(midX / 2 - nodeWidthMid, 200, leftChild.getWidth(),
				leftChild.getHeight());
		this.add(leftChild);
		nodePanels[panelCount++] = leftChild;

		NodePanel rightChild = new NodePanel(20, 10, root);
		rightChild.setBounds(midX + midX / 2 - nodeWidthMid, 200,
				rightChild.getWidth(), rightChild.getHeight());
		this.add(rightChild);
		nodePanels[panelCount++] = rightChild;

		NodePanel llChild = new NodePanel(20, 10, leftChild);
		llChild.setBounds(midX / 4 - nodeWidthMid, 400, llChild.getWidth(),
				llChild.getHeight());
		this.add(llChild);
		nodePanels[panelCount++] = llChild;

		NodePanel lrChild = new NodePanel(20, 10, leftChild);
		lrChild.setBounds(midX / 2 + midX / 4 - nodeWidthMid, 400,
				lrChild.getWidth(), lrChild.getHeight());
		this.add(lrChild);
		nodePanels[panelCount++] = lrChild;

		NodePanel rlChild = new NodePanel(20, 10, rightChild);
		rlChild.setBounds(midX + midX / 4 - nodeWidthMid, 400,
				rlChild.getWidth(), rlChild.getHeight());
		this.add(rlChild);
		nodePanels[panelCount++] = rlChild;

		NodePanel rrChild = new NodePanel(20, 10, rightChild);
		rrChild.setBounds(midX + midX / 2 + midX / 4 - nodeWidthMid, 400,
				rrChild.getWidth(), rrChild.getHeight());
		this.add(rrChild);
		nodePanels[panelCount++] = rrChild;

		leaf = new NodePanel[(width + 1) / 2];
		for (int i = 0; i < leaf.length; i++)
			leaf[i] = nodePanels[panelCount - i - 1];

		JButton btn = new JButton();
		btn.setBounds(midX - 40, HEIGHT - 50 - OFFSET, 80, 30);
		btn.setText("Next");
		btn.addActionListener(this);

		this.add(btn);

		repaint();
	}

	private final int OFFSET = 28;
	private Stroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE,
			BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
	private Stroke defaultStroke = new BasicStroke();

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		for (int i = 1; i < panelCount; i++) {
			NodePanel current = nodePanels[i];
			NodePanel parent = nodePanels[(i - 1) / 2];

			g.drawLine(parent.getX() + parent.getWidth() / 2, parent.getY()
					+ parent.getHeight() + OFFSET,
					current.getX() + current.getWidth() / 2, current.getY()
							+ 10 + OFFSET);
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.RED);
		g2d.setStroke(dashedStroke);
		for (String key : paths.keySet()) {
			PrecombinePath path = paths.get(key);
			if(path.startNode == path.endNode)
				g2d.drawLine(path.endNode.getX() + path.endNode.getWidth() / 2,
						path.endNode.getY() + path.endNode.getHeight() + OFFSET + 30,
						path.endNode.getX() + path.endNode.getWidth() / 2,
						path.endNode.getY() + path.endNode.getHeight() + OFFSET);
				
			else
				g2d.drawLine(path.endNode.getX() + path.endNode.getWidth() / 2,
					path.endNode.getY() + path.endNode.getHeight() + OFFSET,
					path.startNode.getX() + path.startNode.getWidth() / 2,
					path.startNode.getY() + 10 + OFFSET);
		}

		g2d.setColor(Color.BLACK);
		g2d.setStroke(defaultStroke);
	}

	boolean performNext = false;

	private synchronized void waitForEvent() {
		while (!performNext) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		performNext = false;
	}

	Map<String, PrecombinePath> paths = new HashMap<String, SharedCountingFrame.PrecombinePath>();

	private class PrecombinePath {
		NodePanel startNode;
		NodePanel endNode;
	}

	public int getAndIncrement() throws PanicException {
		waitForEvent();

		Stack<NodePanel> nodeStack = new Stack<NodePanel>();
		NodePanel myLeaf = leaf[((CountingThread) Thread.currentThread())
				.getThreadId() / 2];
		NodePanel node = myLeaf;

		PrecombinePath path = new PrecombinePath();
		path.startNode = node;

		/* pre-combining phase */
		while (node.precombine()) {
			node.setThreadName(Thread.currentThread().getName());
			node = node.graphicalParent;
		}

		path.endNode = node;
		
		paths.put(Thread.currentThread().getName(), path);

		repaint();

		NodePanel stopNode = node;
		node.setThreadName(Thread.currentThread().getName());

		waitForEvent();

		/* combining phase */
		node = myLeaf;
		int combined = 1;
		while (node != stopNode) {
			combined = node.combine(combined);
			nodeStack.push(node);
			node = node.graphicalParent;
		}

		waitForEvent();

		/* operation phase */
		int prior = stopNode.op(combined);
		
		/* distribution phase */
		while (!nodeStack.isEmpty()) {
			node = nodeStack.pop();
			if (node.threadName.equals(Thread.currentThread().getName()))
				node.setThreadName("");
			node.distribute(prior);
		}

		if (stopNode.threadName.equals(Thread.currentThread().getName()))
			stopNode.setThreadName("");

		return prior;
	}

	public static void main(String[] args) {
		SharedCountingFrame frame = new SharedCountingFrame(8);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);

		List<CountingThread> threadList = new ArrayList<CountingThread>();

		char tName = 'A';

		Set<Integer> threadIdSet = new HashSet<Integer>();
		Random num = new Random();
		while (true) {
			int threadId = num.nextInt(5) + 1;
			if (threadIdSet.add(threadId)) {
				CountingThread thread = new CountingThread(frame, threadId);
				char threadName = (char) (tName + (threadId - 1));
				thread.setName(threadName + "");
				threadList.add(thread);
				thread.start();
			}

			if (threadIdSet.size() == 5)
				break;
		}

		for (CountingThread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void setPerformNext(boolean value) {
		performNext = value;
		if (performNext)
			notifyAll();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		setPerformNext(true);
	}
}
