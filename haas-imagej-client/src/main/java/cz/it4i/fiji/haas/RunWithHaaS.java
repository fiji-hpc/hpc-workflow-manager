package cz.it4i.fiji.haas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import net.imagej.ImageJ;
/**
 * 
 * @author koz01
 *
 */
@Plugin(type = Command.class, headless = true, menuPath = "Plugins>Run with HaaS")
public class RunWithHaaS implements Command {

	@Parameter
	private LogService log;

	@Parameter(label="Work directory",persist=true)
	private File workDirectory;
	
	@Parameter(label="Data directory")
	private File dataDirectory;
	
	private JobManager jobManager;
	
	@Override
	public void run() {
		try {
			jobManager = new JobManager(getWorkingDirectoryPath());
			jobManager.startJob(getWorkingDirectoryPath(),getContent(dataDirectory));
		} catch (IOException e) {
			log.error(e);
		}
	}

	
	private Path getWorkingDirectoryPath() {
		return Paths.get(workDirectory.toString());
	}


	private Collection<Path> getContent(File dataDirectory) throws IOException {
		return Files.list(Paths.get(dataDirectory.toString())).collect(Collectors.toList());
	}


	public static void main(final String... args) {
		// Launch ImageJ as usual.
		final ImageJ ij = new ImageJ();
		ij.launch(args);
	
		ij.command().run(RunWithHaaS.class, true);
	}

}
