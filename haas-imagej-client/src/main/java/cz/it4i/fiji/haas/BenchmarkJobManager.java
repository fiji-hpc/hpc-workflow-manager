package cz.it4i.fiji.haas;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import cz.it4i.fiji.haas.JobManager.JobSynchronizableFile;
import cz.it4i.fiji.haas_java_client.HaaSClient;
import cz.it4i.fiji.haas_java_client.JobState;
import net.imagej.updater.util.Progress;

public class BenchmarkJobManager {
	private static final String SPIM_OUTPUT_FILENAME_PATTERN = "spim.outputFilenamePattern";
	private static final int HAAS_TEMPLATE_ID = 4;
	private static final String CONFIG_YAML = "config.yaml";
	private JobManager jobManager;
	private Progress progress;
	private Map<Long, JobInfo> jobs = new HashMap<>();

	public BenchmarkJobManager(Path workDirectory, Progress progress) throws IOException {
		jobManager = new JobManager(workDirectory, TestingConstants.getSettings(HAAS_TEMPLATE_ID, 6));
		this.progress = progress;
	}

	public long createJob() throws IOException {
		JobInfo jobInfo = jobManager.createJob(progress);
		jobInfo.storeDataInWorkdirectory(getUploadingFile());
		return indexJob(jobInfo);
	}

	public void startJob(long jobId) throws IOException {
		JobInfo jobInfo = jobs.get(jobId);
		jobInfo.uploadFilesByName(Arrays.asList(CONFIG_YAML));
		String outputName = getOutputName(jobInfo.openLocalFile(CONFIG_YAML));
		jobInfo.submit();
		jobInfo.setProperty(SPIM_OUTPUT_FILENAME_PATTERN, outputName);
	}

	public Collection<Long> getJobs() throws IOException {
		return jobManager.getJobs(progress).stream().map(this::indexJob).collect(Collectors.toList());
	}

	public JobState getState(long jobId) {
		return jobs.get(jobId).getState();
	}

	public void downloadData(long jobId) throws IOException {
		JobInfo ji = jobs.get(jobId);
		if (ji.needsDownload()) {
			if (ji.getState() == JobState.Finished) {
				String filePattern = ji.getProperty(SPIM_OUTPUT_FILENAME_PATTERN);
				ji.downloadData(downloadFinishedData(filePattern), progress);
			} else if (ji.getState() == JobState.Failed) {
				ji.downloadData(downloadFailedData(), progress);
			}
		}

	}

	public Iterable<String> getOutput(long jobId, List<JobSynchronizableFile> files) {
		return jobs.get(jobId).getOutput(files);
	}

	private HaaSClient.UploadingFile getUploadingFile() {
		return new UploadingFileFromResource("", CONFIG_YAML);
	}

	private long indexJob(JobInfo jobInfo) {
		jobs.put(jobInfo.getId(), jobInfo);
		return jobInfo.getId();
	}

	@SuppressWarnings("rawtypes")
	private String getOutputName(InputStream openLocalFile) throws IOException {
		try (InputStream is = openLocalFile) {
			Yaml yaml = new Yaml();

			Map map = yaml.load(is);
			String result = (String) ((Map) map.get("common")).get("hdf5_xml_filename");
			if (result == null) {
				throw new IllegalArgumentException("hdf5_xml_filename not found");
			}
			if (result.charAt(0) == '"' || result.charAt(0) == '\'') {
				if (result.charAt(result.length() - 1) != result.charAt(0)) {
					throw new IllegalArgumentException(result);
				}
				result = result.substring(1, result.length() - 1);
			}

			return result;
		}

	}

	private Predicate<String> downloadFinishedData(String filePattern) {
		return name -> {
			Path p = Paths.get(name);
			String fileName = p.getFileName().toString();
			return fileName.startsWith(filePattern) && fileName.endsWith("h5") || fileName.equals(filePattern + ".xml")
					|| fileName.equals("benchmark_result.csv");
		};
	}

	private Predicate<String> downloadFailedData() {
		return name -> {
			Path p = Paths.get(name);
			return p.getFileName().toString().startsWith("snakejob.")
					|| p.getParent().getFileName().toString().equals("logs");
		};
	}
}
