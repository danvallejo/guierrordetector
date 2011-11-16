package test.swingerror;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class SwingErrorExample {
	JFrame frame;
	DefaultListModel model;

	public SwingErrorExample() {
		model = new DefaultListModel();
		frame = new JFrame();
		JList list = new JList(model);
		JScrollPane scrollpane = new JScrollPane(list);
		JPanel p = new JPanel();
		p.add(scrollpane);
		frame.getContentPane().add(p, "Center");
		JButton b = new JButton("Fill List");
		p = new JPanel();
		p.add(b);
		frame.getContentPane().add(p, "North");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				WorkingThread t = new WorkingThread();
				t.start();
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		SwingErrorExample b = new SwingErrorExample();
	}

	class WorkingThread extends Thread {
		public void run() {
			Random generator = new Random();
			while (true) {
				Integer i = new Integer(generator.nextInt(10));
				if (model.contains(i)) {
					model.removeElement(i);
					/**remove the comment below, and comment out the above one will fix the bug*/
//				    SwingUtilities.invokeLater(new ElementRemover(model,i));
				}
				else {
					model.addElement(i);
					/**remove the comment below, and comment out the above one will fix the bug*/
//					SwingUtilities.invokeLater(new ElementAdder(model,i));
				}
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	class ElementRemover implements Runnable {
		int index;
		DefaultListModel model;

		public ElementRemover(DefaultListModel m, int i) {
			index = i;
			model = m;
		}

		public void run() {
			model.removeElement(index);
		}
	}

	class ElementAdder implements Runnable {
		int index;
		DefaultListModel model;

		public ElementAdder(DefaultListModel m, int i) {
			index = i;
			model = m;
		}

		public void run() {
			model.addElement(index);
		}
	}
}