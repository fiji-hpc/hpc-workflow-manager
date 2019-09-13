package cz.it4i.fiji.haas;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.haas.ui.CloseableControl;
import cz.it4i.fiji.haas.ui.FXFrame;
import cz.it4i.fiji.haas.ui.InitiableControl;
import cz.it4i.fiji.haas.ui.TableViewContextMenu;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;

public class TableProofControl extends BorderPane implements CloseableControl, InitiableControl {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.TableProofControl.class);

	@FXML
	private TableView<ObservableValue<String[]>> table;
	private TableViewContextMenu<ObservableValue<String[]>> menu;

	public TableProofControl() {
		JavaFXRoutines.initRootAndController("TableProof.fxml", this);
		menu = new TableViewContextMenu<>(table);
		for (int i = 0; i < 4; i++) {
			final int index = i;
			JavaFXRoutines.setCellValueFactory(table, index, vals -> vals[index]);
		}
		menu.addItem("Proof", (val, index) -> proof(index), (val, index) -> index < 4);
		addItem();
		addItem();
		addItem();
		addItem();
	}

	private void addItem() {
		String[] values = new String[] { "0", "1", "2", "3" };
		class Values extends ObservableValueBase<String[]> {

			@Override
			public String[] getValue() {
				return values;
			}
		}
		table.getItems().add(new Values());
	}

	private void proof(int index) {
		System.out.println(index);
	}

	@Override
	public void init(Window parameter) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args) {
		class Window extends FXFrame<TableProofControl> {

			public Window() {
				super(() -> new TableProofControl());
			}

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		}
		
		Window w = new Window();
		w.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		w.setVisible(true);
		w.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}
}
