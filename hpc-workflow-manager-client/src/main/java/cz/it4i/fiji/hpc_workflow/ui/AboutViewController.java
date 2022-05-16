
package cz.it4i.fiji.hpc_workflow.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import cz.it4i.swing_javafx_ui.SimpleDialog;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class AboutViewController extends BorderPane {

	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.hpc_workflow.ui.AboutViewController.class);

	private void init() {
		// Do nothing.
	}

	public AboutViewController() {
		init();
		log.debug("About to open the about window.");
		JavaFXRoutines.initRootAndController("AboutView.fxml", this);
	}

	@FXML
	public void openMainWebSite() {
		clickUrl("https://fiji-hpc.github.io/hpc-parallel-tools/");
	}

	@FXML
	public void openShortGuide() {
		clickUrl("https://github.com/fiji-hpc/parallel-macro/wiki/Short-Guide");
	}

	@FXML
	public void openAvailableFunctions() {
		clickUrl(
			"https://github.com/fiji-hpc/parallel-macro/wiki/Available-Functions");
	}

	@FXML
	public void openExamples() {
		clickUrl(
			"https://github.com/fiji-hpc/parallel-macro/tree/master/src/main/resources/ExampleScripts");
	}

	@FXML
	public void openReportIssue() {
		clickUrl("https://github.com/fiji-hpc/parallel-macro/issues");
	}

	private void clickUrl(String url) {
		boolean confirmation = SimpleDialog.showConfirmation("Confirm open URL ?",
			"Are you sure you want to open this URL with your default web browser?");
		if (confirmation) {
			OpenOsWebBrowser.openUrl(url);
		}
	}
}
