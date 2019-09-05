
package cz.it4i.fiji.hpc_workflow.ui;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ShellRoutines;
import cz.it4i.fiji.hpc_workflow.core.ObservableHPCWorkflowJob;
import cz.it4i.fiji.hpc_workflow.core.SimpleObservableValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class JobPropertiesControl extends BorderPane implements Closeable {

	private static final String FXML_FILE_NAME = "JobProperties.fxml";
	public static final Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.hpc_workflow.ui.JobPropertiesControl.class);

	@FXML
	private TableView<ObservableValue<PValue>> properties;

	private ObservableHPCWorkflowJob job;

	private final ExecutorService executorServiceUI;

	public JobPropertiesControl() {
		JavaFXRoutines.initRootAndController(FXML_FILE_NAME, this);
		executorServiceUI = Executors.newSingleThreadExecutor();
		initTable();
	}

	public void setJob(ObservableHPCWorkflowJob job) {
		this.job = job;
		fillTable();
	}

	@Override
	public void close() {
		executorServiceUI.shutdown();
	}

	private void initTable() {
		setCellValueFactory(0, PValue::getName);
		setCellValueFactory(1, PValue::getValueAsString);
		JavaFXRoutines.setOnDoubleClickAction(properties, executorServiceUI,
			rowData -> rowData.getValue().isOpenAllowed(), rowData -> {
				try {
					ShellRoutines.openDirectoryInBrowser(rowData.getValue().getPath());
				}
				catch (UnsupportedOperationException | IOException e) {
					// TODO: Escalate an error to the end user
					log.error(e.getMessage(), e);
				}
			});
	}

	private void fillTable() {
		properties.getItems().add(new SimpleObservableValue<>(new PValue("Input",
			job.getValue().getInputDirectory(),
			"Demo data on the Salomon IT4I cluster")));
		properties.getItems().add(new SimpleObservableValue<>(new PValue("Output",
			job.getValue().getOutputDirectory(), "N/A")));
		properties.getItems().add(new SimpleObservableValue<>(new PValue("Working",
			job.getValue().getDirectory(), "N/A")));
	}

	private void setCellValueFactory(int i, Function<PValue, String> mapper) {
		JavaFXRoutines.setCellValueFactory(properties, i, mapper);
	}

	private class PValue {

		private final String name;
		private final Path path;
		private final String textIfNull;

		public PValue(String name, Path path, String textIfNull) {
			this.name = name;
			this.path = path;
			this.textIfNull = textIfNull;
		}

		public String getName() {
			return name;
		}

		public String getValueAsString() {
			return Optional.ofNullable(path).map(Path::toString).orElse(textIfNull);
		}

		public boolean isOpenAllowed() {
			return path != null;
		}

		public Path getPath() {
			return path;
		}
	}
}
