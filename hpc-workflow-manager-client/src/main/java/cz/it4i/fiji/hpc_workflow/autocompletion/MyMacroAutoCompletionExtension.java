
package cz.it4i.fiji.hpc_workflow.autocompletion;

import net.imagej.legacy.plugin.MacroExtensionAutoCompletionPlugin;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@Plugin(type = MacroExtensionAutoCompletionPlugin.class)
public class MyMacroAutoCompletionExtension implements
	MacroExtensionAutoCompletionPlugin
{

	private static final String STANDARD_DOCUMENTATION_INFORMATION =
		"For more help visit the <a href=\"https://github.com/fiji-hpc/parallel-macro/wiki/Available-Functions\">wiki page</a>.";

	@Override
	public List<BasicCompletion> getCompletions(
		CompletionProvider completionProvider)
	{

		ArrayList<BasicCompletion> completions = new ArrayList<>();

		// Get the list of function information:
		FunctionsInformationLoader functionInformationLoader =
			new FunctionsInformationLoader();
		functionInformationLoader.load();

		List<FunctionInformation> functionInformationList =
			functionInformationLoader.getFunctionInformationList();

		// Go through function information and provide an auto-complete entry for
		// each one:
		for (FunctionInformation plugin : functionInformationList) {
			String commandName = "par" + plugin.getName() + "(" + plugin
				.getParameters() + ")";
			String description = "<b>" + commandName + "</b><br>" + plugin
				.getDescription() + "<hr/>" + STANDARD_DOCUMENTATION_INFORMATION;
			completions.add(new BasicCompletion(completionProvider, commandName, null,
				description));
		}

		return completions;
	}
}
