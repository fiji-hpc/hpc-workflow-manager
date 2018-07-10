package cz.it4i.fiji.haas_java_client;

public class JobSettingsBuilder {

	private static final long DEFAULT_TEMPLATE = 1l;


	private static final int DEFAULT_WALLTIME = 600;


	private static final long DEFAULT_CLUSTER_NODE_TYPE = 7L;


	private static final String DEFAULT_JOB_NAME = "DefaultHEAppEJob";


	private static final int DEFAULT_NUMBER_OF_NODES = 1;


	private static final int DEFAULT_NUMBER_OF_CORES_PER_NODE = 24;
	
	
	private long _templateId = DEFAULT_TEMPLATE;
	private int _walltimeLimit = DEFAULT_WALLTIME;
	private long _clusterNodeType = DEFAULT_CLUSTER_NODE_TYPE;
	private String _jobName = DEFAULT_JOB_NAME;
	private int _numberOfNodes = DEFAULT_NUMBER_OF_NODES;
	private int _numberOfCoresPerNode = DEFAULT_NUMBER_OF_CORES_PER_NODE;
	

	public JobSettingsBuilder templateId(long templateId) {
		this._templateId = templateId;
		return this;
	}
	
	public JobSettingsBuilder walltimeLimit(int walltimeLimit) {
		this._walltimeLimit = walltimeLimit;
		return this;
	}
	
	public JobSettingsBuilder clusterNodeType(long clusterNodeType) {
		this._clusterNodeType = clusterNodeType;
		return this;
	}
	public JobSettingsBuilder jobName(String jobName) {
		this._jobName = jobName;
		return this;
	}
	
	public JobSettingsBuilder numberOfNodes(int numberOfNodes) {
		this._numberOfNodes = numberOfNodes;
		return this;
	}
	
	public JobSettingsBuilder numberOfCoresPerNode(int numberOfCoresPerNode) {
		this._numberOfCoresPerNode = numberOfCoresPerNode;
		return this;
	}
	
	public JobSettings build() {
		return new JobSettings() {
			
			@Override
			public int getWalltimeLimit() {
				return _walltimeLimit;
			}
			
			@Override
			public long getTemplateId() {
				return _templateId;
			}
			
			@Override
			public int getNumberOfNodes() {
				return _numberOfNodes;
			}
			
			@Override
			public String getJobName() {
				return _jobName;
			}
			
			@Override
			public long getClusterNodeType() {
				return _clusterNodeType;
			}

			@Override
			public int getNumberOfCoresPerNode() {
				return _numberOfCoresPerNode;
			}
		};
	}
}
