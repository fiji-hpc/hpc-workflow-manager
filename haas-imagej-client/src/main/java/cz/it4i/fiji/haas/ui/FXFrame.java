package cz.it4i.fiji.haas.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.im.InputMethodRequests;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class FXFrame<C extends FXFrame.Controller> extends JDialog {

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.FXFrame.class);

	static public void runOnFxThread(Runnable runnable) {
		if (Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			Platform.runLater(runnable);
		}
	}

	public interface Controller {
		void init(Window frame);
		static public <V> void executeAsync(Executor executor, Callable<V> action, Consumer<V> postAction) {
			executor.execute(() -> {
				V result;
				try {
					result = action.call();
					postAction.accept(result);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
				
			});
		}
		
		@SuppressWarnings("unchecked")
		static public <U,T extends ObservableValue<U>,V> void setCellValueFactory(TableView<T> tableView,int index, Function<U, V> mapper) {
			((TableColumn<T, V>) tableView.getColumns().get(index))
					.setCellValueFactory(f -> new ObservableValueAdapter<U, V>(f.getValue(), mapper));
			//((TableColumn<T, String>) tableView.getColumns().get(index)).setCellFactory(f->f.set);

		}
	}

	private static final long serialVersionUID = 1L;
	private JFXPanel fxPanel;
	private String fxmlFile;
	private Consumer<C> controlerInit;
	private C controller;

	public FXFrame(String fxmlFile) {
		this(null, fxmlFile);
	}

	public FXFrame(Window applicationFrame, String string) {
		super(applicationFrame,ModalityType.MODELESS);
		fxmlFile = string;
	}

	/**
	 * Create the JFXPanel that make the link between Swing (IJ) and JavaFX plugin.
	 */
	protected void init(Consumer<C> controlerInit) {
		this.controlerInit = controlerInit;
		this.fxPanel = new P_JFXPanel();
		Platform.setImplicitExit(false);
		this.setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(this.fxPanel);
		
		this.add(scrollPane, BorderLayout.CENTER);

		// The call to runLater() avoid a mix between JavaFX thread and Swing thread.
		runOnFxThread(() -> initFX(fxPanel));

	}

	protected C getController() {
		return controller;
	}

	private void initFX(JFXPanel fxPanel) {
		// Init the root layout
		try {
			FXMLLoader loader = new FXMLLoader();
			URL res = FXFrame.class.getResource(fxmlFile);
			loader.setLocation(res);
			Parent rootLayout = (Parent) loader.load();
			
			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			this.fxPanel.setScene(scene);
			this.fxPanel.setVisible(true);

			// Resize the JFrame to the JavaFX scene
			Dimension dim = new Dimension((int) scene.getWidth(), (int) scene.getHeight());
			this.fxPanel.setMinimumSize(dim);
			this.fxPanel.setMaximumSize(dim);
			this.fxPanel.setPreferredSize(dim);
			// this.setSize((int) scene.getWidth(), (int) scene.getHeight());
			this.pack();
			// Get the controller and add an ImageJ context to it.
			controller = loader.<C>getController();
			controlerInit.accept(controller);
			controller.init(this);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static class P_JFXPanel extends JFXPanel {
		private static final long serialVersionUID = 1L;

		
		@Override
		public synchronized InputMethodRequests getInputMethodRequests() {
			try {
				return super.getInputMethodRequests();
			} catch(NullPointerException e) {
				//IGNORE FIX ISSUE https://bugs.openjdk.java.net/browse/JDK-8098836
				return null;
			}
		}
		
		
			
	};

}
