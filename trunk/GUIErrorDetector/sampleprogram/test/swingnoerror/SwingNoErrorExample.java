package test.swingnoerror;

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

public class SwingNoErrorExample {
	JFrame frame;
	DefaultListModel model;

	public SwingNoErrorExample() {
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
		SwingNoErrorExample b = new SwingNoErrorExample();
	}

	class WorkingThread extends Thread {
		public void run() {
			Random generator = new Random();
			while (true) {
				final Integer i = new Integer(generator.nextInt(10));
				if (model.contains(i)) {
				    SwingUtilities.invokeLater(new Runnable() {
				    	public void run() {
				    		model.removeElement(i);
				    	}
				    });
				}
				else {
					SwingUtilities.invokeLater(new Runnable() {
				    	public void run() {
				    		model.addElement(i);
				    	}
				    });
				}
				try {
					Thread.sleep(i);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}