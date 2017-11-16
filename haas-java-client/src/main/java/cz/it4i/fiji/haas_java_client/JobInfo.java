package cz.it4i.fiji.haas_java_client;

import java.util.Calendar;
import java.util.Collection;

public interface JobInfo {
	
	
	Collection<Long> getTasks();
	
	JobState getState();
	
	Calendar getStartTime();

	Calendar getEndTime();
}
