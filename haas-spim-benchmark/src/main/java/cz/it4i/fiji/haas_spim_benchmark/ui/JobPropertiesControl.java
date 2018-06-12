package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.io.Closeable;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class JobPropertiesControl extends BorderPane implements Closeable{
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
		setOnDoubleClickAction(rowData -> ShellRoutines.openDirectoryInBrowser(rowData.getPath()));
	}
	
	private void setOnDoubleClickAction(Consumer<P_Value> r) {
		properties.setRowFactory( tv -> {
		    TableRow<ObservableValue<P_Value>> row = new TableRow<>();
		    row.setOnMouseClicked(event -> {
		        if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
		        	P_Value rowData = row.getItem().getValue();
		            if(rowData.isOpenAllowed()) {
						executorServiceUI.execute(()->r.accept(rowData));
		            }
		        }
		    });
		    return row ;
		});		
	}

	private void fillTable() {
		properties.getItems().add(new UpdatableObservableValue<JobPropertiesControl.P_Value>(
				new P_Value("Input", job.getInputDirectory(), "Demo data on server"), x->UpdateStatus.NotUpdated, x->x));
		properties.getItems().add(new UpdatableObservableValue<JobPropertiesControl.P_Value>(
				new P_Value("Output", job.getOutputDirectory(), "N/A"), x->UpdateStatus.NotUpdated, x->x));

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
			return Optional.ofNullable(path).map(p->p.toString()).orElse(textIfNull);
		}
		
		public boolean isOpenAllowed() {
			return path != null;
		}
		
		public Path getPath() {
			return path;
		}
	}
}

