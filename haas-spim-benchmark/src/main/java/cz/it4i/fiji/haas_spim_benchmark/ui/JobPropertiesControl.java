package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.ShellRoutines;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class JobPropertiesControl extends BorderPane implements Closeable {
	private static final String FXML_FILE_NAME = "JobProperties.fxml";
	public static final Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.JobPropertiesControl.class);

	@FXML
	private TableView<ObservableValue<P_Value>> properties;

	private BenchmarkJob job;

	private final ExecutorService executorServiceUI;

	public JobPropertiesControl() {
		JavaFXRoutines.initRootAndController(FXML_FILE_NAME, this);
		executorServiceUI = Executors.newSingleThreadExecutor();
		initTable();
	}

	public void setJob(BenchmarkJob job) {
		this.job = job;
		fillTable();
	}

	@Override
	public void close() {
		executorServiceUI.shutdown();
	}

	private void initTable() {
		setCellValueFactory(0, s -> s.getName());
		setCellValueFactory(1, s -> s.getValueAsString());
		JavaFXRoutines.setOnDoubleClickAction(properties, executorServiceUI, rowData -> rowData.isOpenAllowed(),
				rowData -> ShellRoutines.openDirectoryInBrowser(rowData.getPath()));
	}

	private void fillTable() {
		properties.getItems()
				.add(new UpdatableObservableValue<>(
						new P_Value("Input", job.getInputDirectory(), "Demo data on the Salomon IT4I cluster"),
						x -> UpdateStatus.NotUpdated, x -> x));
		properties.getItems().add(new UpdatableObservableValue<>(
				new P_Value("Output", job.getOutputDirectory(), "N/A"), x -> UpdateStatus.NotUpdated, x -> x));

		properties.getItems().add(new UpdatableObservableValue<>(
				new P_Value("Working", job.getDirectory(), "N/A"), x -> UpdateStatus.NotUpdated, x -> x));

	}

	private void setCellValueFactory(int i, Function<P_Value, String> mapper) {
		JavaFXRoutines.setCellValueFactory(properties, i, mapper);
	}

	private class P_Value {
		private final String name;
		private final Path path;
		private final String textIfNull;

		public P_Value(String name, Path path, String textIfNull) {
			this.name = name;
			this.path = path;
			this.textIfNull = textIfNull;
		}

		public String getName() {
			return name;
		}

		public String getValueAsString() {
			return Optional.ofNullable(path).map(p -> p.toString()).orElse(textIfNull);
		}

		public boolean isOpenAllowed() {
			return path != null;
		}

		public Path getPath() {
			return path;
		}
	}
}
