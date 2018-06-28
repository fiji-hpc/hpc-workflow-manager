package cz.it4i.fiji.haas_java_client;

public interface JobSettings {
	long getTemplateId();
	int getWalltimeLimit();
	long getClusterNodeType();
	String getJobName();
	int getNumberOfNodes();
	int getNumberOfCoresPerNode();
}
