package cz.it4i.fiji.hpc_client;

public enum JobState {
	Unknown,
	Configuring,
    Submitted,
    Queued,
    Running,
    Finished,
    Failed,
    Canceled, 
    Disposed;
}
