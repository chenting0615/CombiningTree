import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.border.LineBorder;

class NodePanel extends JPanel {
	private final int WIDTH = 80;
	private final int HEIGHT = 100;

	String threadName = "";
	NodePanel graphicalParent = null;

	public NodePanel(int x, int y) {
		setBounds(x, y, WIDTH, HEIGHT);
		setBorder(new LineBorder(new Color(0.f, 0.f, 0.f)));
		status = CStatus.ROOT;
		locked = false;
	}

	public NodePanel(int x, int y, NodePanel myGraphicalParent) {
		setBounds(x, y, WIDTH, HEIGHT);
		setBorder(new LineBorder(new Color(0.f, 0.f, 0.f)));
		status = CStatus.IDLE;
		locked = false;
		graphicalParent = myGraphicalParent;
		repaint();
	}

	public void setThreadName(String name) {
		threadName = name;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Dimension d = this.getSize();
		int midX = d.width / 2;
		int midY = d.height / 2;

		g.drawString(threadName, 5, 15);

		g.drawLine(0, 20, d.width, 20);
		g.drawLine(0, d.height - 20, d.width, d.height - 20);
		g.drawLine(midX, 20, midX, d.height - 20);
		g.drawLine(0, midY, d.width, midY);

		if (graphicalParent != null)
			g.fillOval(midX - 5, 5, 10, 10);

		g.drawString(locked + "", midX - 16, d.height - 5);

		String statusString = "";
		switch (status) {
		case IDLE:
			statusString = "I";
			break;

		case FIRST:
			statusString = "F";
			break;

		case SECOND:
			statusString = "S";
			break;

		case RESULT:
			statusString = "R";
			break;

		case ROOT:
			statusString = "Ro";
			break;
		}

		g.drawString(statusString, midX / 2 - (statusString.length() - 1) * 5,
				40);

		g.drawString(result + "", midX + midX / 2, 40);
		g.drawString(firstValue + "", midX / 2 - 2, midY + 20);
		g.drawString(secondValue + "", midX + midX / 2, midY + 20);
	}

	/* Node code */

	enum CStatus {
		IDLE, FIRST, SECOND, RESULT, ROOT
	};

	boolean locked;
	CStatus status;
	int firstValue, secondValue;
	int result;

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

		boolean returnValue = true;
		switch (status) {
		case IDLE:
			status = CStatus.FIRST;
			break;

		case FIRST:
			locked = true;
			status = CStatus.SECOND;
			returnValue = false;
			break;

		case ROOT:
			return false;

		default:
			throw new PanicException("precombine: Unexpected state exception: "
					+ status);
		}

		repaint();
		return returnValue;
	}

	public synchronized int combine(int combined) throws PanicException {
		while (locked)
			waitIndefinite();

		locked = true;
		firstValue = combined;

		repaint();

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
		int returnValue;

		switch (status) {
		case ROOT:
			int prior = result;
			result += combined;
			returnValue = prior;
			break;

		case SECOND:
			secondValue = combined;
			locked = false;
			repaint();
			notifyAll(); // wake up waiting threads

			while (status != CStatus.RESULT)
				waitIndefinite();

			locked = false;
			notifyAll();
			status = CStatus.IDLE;
			returnValue = result;
			break;

		default:
			throw new PanicException("op: Unexpected state exception: "
					+ status);
		}

		repaint();
		return returnValue;
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

		repaint();
		notifyAll();
	}
}
