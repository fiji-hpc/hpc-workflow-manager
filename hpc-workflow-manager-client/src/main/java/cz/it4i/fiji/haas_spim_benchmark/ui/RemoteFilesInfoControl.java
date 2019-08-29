package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class RemoteFilesInfoControl extends BorderPane implements CloseableControl, InitiableControl {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory
			.getLogger(cz.it4i.fiji.haas_spim_benchmark.ui.RemoteFilesInfoControl.class);

	@SuppressWarnings("unused")
	private Window root;

	@FXML
	private TableView<ObservableValue<RemoteFileInfo>> files;

	public RemoteFilesInfoControl() {
		JavaFXRoutines.initRootAndController("RemoteFilesInfo.fxml", this);
	}

	public void setFiles(List<ObservableValue<RemoteFileInfo>> files) {
		files.forEach(file -> this.files.getItems().add(file));
	}

	@Override
	public void init(Window parameter) {
		this.root = parameter;
		initTable();
	}

	@Override
	public void close() {

	}

	private void initTable() {
		JavaFXRoutines.setCellValueFactory(files, 0, RemoteFileInfo::getName);
		JavaFXRoutines.setCellValueFactory(files, 1,
				(Function<RemoteFileInfo, String>) file -> file.getSize() >= 0 ? formatSize(file.getSize())
						: "Not exists");

	}

	private String formatSize(long size) {
		return FileUtils.byteCountToDisplaySize(size);
	}

}
