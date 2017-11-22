package cz.it4i.fiji.haas;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.ApplicationFrame;
import org.scijava.ui.UIService;
import org.scijava.widget.UIComponent;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas.ui.ProgressDialog;
import net.imagej.ImageJ;

/**
 * 
 * @author koz01
 *
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Check status of HaaS")
public class CheckStatusOfHaaS implements Command {

	@Parameter
	private LogService log;

	@Parameter(label = "Work directory", persist = true, style = "directory")
	private File workDirectory;

	@Parameter
	private UIService uiService;

	@Parameter
	private Context context;

	private JobManager jobManager;

	@Override
	public void run() {
		try {
			jobManager = new JobManager(getWorkingDirectoryPath(), context);
			if (uiService.isHeadless()) {
				downloadAll();
			} else {
				CheckStatusOfHaaSWindow window;
				window = ModalDialogs.doModal(new CheckStatusOfHaaSWindow(getFrame(), context));
				ProgressDialog dialog = ModalDialogs.doModal(new ProgressDialog(window));
				dialog.setTitle("Downloading info about jobs");
				Collection<JobInfo> jobs = jobManager.getJobs();
				int count = 0;
				for (JobInfo ji : jobs) {
					String item;
					dialog.addItem(item = "job id:" + ji.getId());
					try {
						ji.updateInfo();
					} catch (IOException e) {
						log.error(e);
					}
					window.addJob(ji);
					dialog.itemDone(item);
					dialog.setCount(count, jobs.size());
					count++;
				}
				dialog.done();
			}
		} catch (IOException e) {
			log.error(e);
		}

	}

	private Frame getFrame() {
		ApplicationFrame af = uiService.getDefaultUI().getApplicationFrame();
		if (af instanceof Frame) {
			return (Frame) af;
		} else if (af instanceof UIComponent) {
			Object component = ((UIComponent<?>) af).getComponent();
			if (component instanceof Frame) {
				return (Frame) component;
			}
		}
		return null;
	}

	private void downloadAll() {
		for (JobInfo id : jobManager.getJobsNeedingDownload()) {
			System.out.println("Job " + id.getId() + " needs download");
			jobManager.downloadJob(id.getId());
		}
	}

	private Path getWorkingDirectoryPath() {
		return Paths.get(workDirectory.toString());
	}

	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);

		ij.command().run(CheckStatusOfHaaS.class, true);
	}

}
