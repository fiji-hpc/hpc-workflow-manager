
package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue;
import cz.it4i.fiji.haas.ui.UpdatableObservableValue.UpdateStatus;
import cz.it4i.fiji.haas_java_client.FileTransferInfo;
import cz.it4i.fiji.haas_spim_benchmark.core.BenchmarkJobManager.BenchmarkJob;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class DataTransferController extends BorderPane implements
	CloseableControl
{

	private static final String FXML_FILE_NAME = "DataTransfer.fxml";

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.haas_spim_benchmark.ui.DataTransferController.class);

	@FXML
	private TableView<ObservableValue<FileTransferInfo>> filesToUpload;

	private BenchmarkJob job;

	public DataTransferController() {
		JavaFXRoutines.initRootAndController(FXML_FILE_NAME, this);
		initTable();
	}

	public void setJob(final BenchmarkJob job) {
		this.job = job;
		fillTable();
	}

	// -- CloseableControl methods --

	@Override
	public void close() {
		// DO NOTHING
	}

	// -- Helper methods --

	private void initTable() {
		setCellValueFactory(0, f -> f.getPathAsString());
		setCellValueFactory(1, f -> f.getState().toString());
	}

	private void setCellValueFactory(final int columnIndex,
		final Function<FileTransferInfo, String> mapper)
	{
		JavaFXRoutines.setCellValueFactory(filesToUpload, columnIndex, mapper);
	}

	private void fillTable() {

		final List<ObservableValue<FileTransferInfo>> tempList = new LinkedList<>();

		job.getFileTransferInfo().forEach(i -> {
			tempList.add(new UpdatableObservableValue<>(i,
				x -> UpdateStatus.NotUpdated, x -> x));
		});

		filesToUpload.getItems().addAll(tempList);
	}

}
