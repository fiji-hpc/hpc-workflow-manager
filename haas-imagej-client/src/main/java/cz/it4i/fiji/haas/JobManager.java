package cz.it4i.fiji.haas;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import cz.it4i.fiji.haas_java_client.HaaSClient;

public class JobManager {

	private Path workDirectory;
	
	private Collection<Job> jobs = new LinkedList<>();
	
	private HaaSClient haasClient;

	public JobManager(Path workDirectory) throws IOException {
		super();
		this.workDirectory = workDirectory;
		Files.list(this.workDirectory).filter(p -> Files.isDirectory(p) && Job.isJobPath(p))
				.forEach(p -> {
					try {
						jobs.add(new Job(p,this::getHaasClient));
					} catch (IOException e) {
						e.printStackTrace();
					}
				});

	}

	public void startJob(Path path, Collection<Path> files) throws IOException {
		jobs.add(new Job(path, files,this::getHaasClient));
	}

	private HaaSClient getHaasClient() {
		if(haasClient == null) {
			haasClient = new HaaSClient(2l, 9600, 6l, "DD-17-31");
		}
		return haasClient;
	}

}
