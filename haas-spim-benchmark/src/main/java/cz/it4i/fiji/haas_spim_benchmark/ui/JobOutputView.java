package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas_java_client.SynchronizableFileType;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.Job;

public class JobOutputView {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.JobOutputView.class);
	private Timer timer;
	private JDialog theDialog;
	private JTextArea theText;
	private Job job;
	private ExecutorService executor;
	private long readedChars = 0;

	public JobOutputView(Window parent, ExecutorService executor, Job job, long refreshTimeout) {
		this.job = job;
		this.executor = executor;
		constructFrame(parent);
		parent.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				dispose();
			}
		});
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				updateView();
			}
		}, refreshTimeout, refreshTimeout);
		updateView();
	}

	private void dispose() {
		timer.cancel();
		theDialog.dispose();
	}

	private void constructFrame(Window parent) {
		theDialog = new JDialog(parent, "Output of job: " + (job!=null?job.getId():"N/A"));
		theDialog.setPreferredSize(new Dimension(500, 500));
		theDialog.setLocation(550, 400);
		JPanel jPanel = new JPanel(new BorderLayout());
		theDialog.setContentPane(jPanel);
        //Create the text area used for output.  Request
        //enough space for 5 rows and 30 columns.
        theText = new JTextArea(5, 30);
        theText.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(theText);

        //Lay out the main panel.
        jPanel.setPreferredSize(new Dimension(450, 130));
        jPanel.add(scrollPane, BorderLayout.CENTER);
		
		theDialog.pack();
		theDialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				dispose();
			}
		});
		theDialog.setVisible(true);
	}

	private void updateView() {
		executor.execute(() -> {
			JobSynchronizableFile file = new JobSynchronizableFile(SynchronizableFileType.StandardErrorFile,
					readedChars);
			String output;
			if (job != null) {
				output = job.getOutput(Arrays.asList(file)).get(0);
			} else {
				output = "This is testing line\n";
			}
			readedChars += output.length();
			theText.append(output);
			theText.setCaretPosition(theText.getDocument().getLength());
		});
	}
}
