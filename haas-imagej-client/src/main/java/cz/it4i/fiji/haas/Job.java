package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Properties;
import java.util.function.Supplier;

import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;

public class Job {

	private static final String JOB_ID_PROPERTY = "job.id";

	private static final String JOB_STATE_PROPERTY = "job.state";

	public static boolean isJobPath(Path p) {
		return isValidPath(p);
	}
	
	private static String JOB_INFO_FILE = ".jobinfo";
	
	private Path jobDir;

	private Supplier<HaaSClient> haasClientSupplier;
	
	private JobState state;
	
	public Job(Path path, Collection<Path> files, Supplier<HaaSClient> haasClientSupplier) throws IOException {
		this(haasClientSupplier);
		HaaSClient client = this.haasClientSupplier.get();
		long id = client.start(files, "TestOutRedirect",
				Collections.emptyList());
		jobDir = path.resolve("" + id);
		Files.createDirectory(jobDir);
		state = client.obtainJobInfo(id).getState();
		saveJobinfo();
	}

	

	public Job(Path p, Supplier<HaaSClient> haasClientSupplier) throws IOException {
		this(haasClientSupplier);
		jobDir = p;
		loadJobInfo();
		checkStateForDownload();
	}

	private synchronized void checkStateForDownload() throws IOException {
		long jobId = getJobId();
		JobState actualState = haasClientSupplier.get().obtainJobInfo(jobId).getState();
		if(EnumSet.of(JobState.Failed, JobState.Finished, JobState.Canceled).contains(actualState) && state != actualState) {
			haasClientSupplier.get().download(jobId, jobDir);
			state = actualState;
			saveJobinfo();
		}
	}



	private Job(Supplier<HaaSClient> haasClientSupplier) {
		this.haasClientSupplier = haasClientSupplier;
	}
	
	private synchronized void saveJobinfo() throws IOException {
		try(OutputStream ow= Files.newOutputStream(jobDir.resolve(JOB_INFO_FILE), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
			Properties prop = new Properties();
			prop.setProperty(JOB_ID_PROPERTY, "" + getJobId());
			prop.setProperty(JOB_STATE_PROPERTY, "" + state);
			prop.store(ow, null);
		}
	}
	
	
	private synchronized void loadJobInfo() throws IOException {
		try(InputStream is= Files.newInputStream(jobDir.resolve(JOB_INFO_FILE))) {
			Properties prop = new Properties();
			prop.load(is);
			state = JobState.valueOf(prop.getProperty(JOB_STATE_PROPERTY));
			assert getJobId() == Long.parseLong(prop.getProperty(JOB_ID_PROPERTY));
		}
	}
	
	private long getJobId() {
		return getJobId(jobDir);
	}



	private static boolean isValidPath(Path path) {

		try {
			getJobId(path);
		} catch (NumberFormatException e) {
			return false;
		}
		return Files.isRegularFile(path.resolve(JOB_INFO_FILE));
	}

	private static long getJobId(Path path) {
		return Long.parseLong(path.getFileName().toString());
	}

}
