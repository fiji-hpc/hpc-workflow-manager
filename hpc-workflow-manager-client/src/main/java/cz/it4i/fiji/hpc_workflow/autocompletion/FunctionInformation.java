
package cz.it4i.fiji.hpc_workflow.autocompletion;

public class FunctionInformation {

	private String description;

	private String parameters;

	private String name;

	public FunctionInformation(String newName, String newDescription,
		String newParameters)
	{
		this.name = newName;
		this.description = newDescription;
		this.parameters = newParameters;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getParameters() {
		return this.parameters;
	}
}
