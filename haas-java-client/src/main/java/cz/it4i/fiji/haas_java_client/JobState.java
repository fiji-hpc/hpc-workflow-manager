package cz.it4i.fiji.haas_java_client;

public enum JobState {
	Unknown,
	Configuring,
    Submitted,
    Queued,
    Running,
    Finished,
    Failed,
    Canceled;
}
