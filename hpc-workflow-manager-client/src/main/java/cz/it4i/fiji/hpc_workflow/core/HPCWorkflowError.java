package cz.it4i.fiji.hpc_workflow.core;

public class HPCWorkflowError {
	
	private final String plainDescription;
	
	public HPCWorkflowError(String plainDescription) {
		this.plainDescription = plainDescription;
	}
	
	public String getPlainDescription() {
		return plainDescription;
	}

}
