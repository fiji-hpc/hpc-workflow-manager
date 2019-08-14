package cz.it4i.fiji.haas_spim_benchmark.ui;

import java.awt.Window;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.JavaFXRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class MPITaskProgressViewController extends BorderPane implements CloseableControl, InitiableControl {
	@FXML
	private TableView tasksTableView;
	
	private Window root;
	
	public MPITaskProgressViewController() {
		init();
	}

	private void init() {
		JavaFXRoutines.initRootAndController("MPITaskProgressView.fxml", this);
	}

	@Override
	public void init(Window parameter) {
		this.root = parameter;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
}
