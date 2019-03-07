package cz.it4i.fiji.haas;

import java.util.List;

import cz.it4i.fiji.haas_java_client.SynchronizableFileType;

public interface HaaSOutputHolder {

	List<String> getActualOutput(List<SynchronizableFileType> content);

}