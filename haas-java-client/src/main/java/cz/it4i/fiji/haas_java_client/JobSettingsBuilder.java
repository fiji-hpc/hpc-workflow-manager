package cz.it4i.fiji.haas_java_client;

public class JobSettingsBuilder {

	private static final long DEFAULT_TEMPLATE = 1l;


	private static final int DEFAULT_WALLTIME = 600;


	private static final long DEFAULT_CLUSTER_NODE_TYPE = 7L;


	private static final String DEFAULT_JOB_NAME = "DefaultHEAppEJob";


	private static final int DEFAULT_NUMBER_OF_NODES = 1;


	private static final int DEFAULT_NUMBER_OF_CORES_PER_NODE = 24;
	
	
	private long templateId = DEFAULT_TEMPLATE;
	private int walltimeLimit = DEFAULT_WALLTIME;
	private long clusterNodeType = DEFAULT_CLUSTER_NODE_TYPE;
	private String jobName = DEFAULT_JOB_NAME;
	private int numberOfNodes = DEFAULT_NUMBER_OF_NODES;
	private int numberOfCoresPerNode = DEFAULT_NUMBER_OF_CORES_PER_NODE;
	

	public JobSettingsBuilder setTemplateId(long templateId) {
		this.templateId = templateId;
		return this;
	}
	
	public JobSettingsBuilder setWalltimeLimit(int walltimeLimit) {
		this.walltimeLimit = walltimeLimit;
		return this;
	}
	
	public JobSettingsBuilder setClusterNodeType(long clusterNodeType) {
		this.clusterNodeType = clusterNodeType;
		return this;
	}
	public JobSettingsBuilder setJobName(String jobName) {
		this.jobName = jobName;
		return this;
	}
	
	public JobSettingsBuilder setNumberOfNodes(int numberOfNodes) {
		this.numberOfNodes = numberOfNodes;
		return this;
	}
	
	public JobSettingsBuilder setNumberOfCoresPerNode(int numberOfCoresPerNode) {
		this.numberOfCoresPerNode = numberOfCoresPerNode;
		return this;
	}
	
	public JobSettings build() {
		return new JobSettings() {
			
			@Override
			public int getWalltimeLimit() {
				return walltimeLimit;
			}
			
			@Override
			public long getTemplateId() {
				return templateId;
			}
			
			@Override
			public int getNumberOfNodes() {
				return numberOfNodes;
			}
			
			@Override
			public String getJobName() {
				return jobName;
			}
			
			@Override
			public long getClusterNodeType() {
				return clusterNodeType;
			}

			@Override
			public int getNumberOfCoresPerNode() {
				return numberOfCoresPerNode;
			}
		};
	}
}
