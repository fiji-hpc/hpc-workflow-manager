package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class RemoteFilesInfoControl extends BorderPane implements CloseableControl, InitiableControl {

	@SuppressWarnings("unused")
	private Window root;

	@FXML
	private TableView<ObservableValue<RemoteFileInfo>> files;

	public RemoteFilesInfoControl(List<ObservableValue< RemoteFileInfo>> files) {
		JavaFXRoutines.initRootAndController("RemoteFilesInfo.fxml", this);
		files.forEach(file->this.files.getItems().add(file));
		
	}
	
	@Override
	public void init(Window parameter) {
		this.root = parameter;
		initTable();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	private void initTable() {
		JavaFXRoutines.setCellValueFactory(files, 0, file -> file.getName());
		JavaFXRoutines.setCellValueFactory(files, 1,
				(Function<RemoteFileInfo, String>) file -> file.getSize() >= 0 ? formatSize(file.getSize()) : "Not exists");
		
	}

	private String formatSize(long size) {
		return FileUtils.byteCountToDisplaySize(size);
	}

}
