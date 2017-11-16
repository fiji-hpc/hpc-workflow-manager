package cz.it4i.fiji.haas.ui;



import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import cz.it4i.fiji.haas.JobManager.JobInfo;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ContextMenuEvent;
import javafx.util.Callback;

public class CheckStatusOfHaaSController {
	
	
	@Parameter
	private LogService logService;
	
	@FXML
	private TableView<JobInfo> jobs;
	
	public CheckStatusOfHaaSController() {
		
	}
	
	public void addJob(JobInfo job) {
		jobs.getItems().add(job);
	}
	
	@SuppressWarnings("unchecked")
	public void init() {
		ContextMenu cm = new ContextMenu();
		MenuItem download = new MenuItem("Download");
		cm.getItems().add(download);
		((TableColumn<JobInfo, String>)jobs.getColumns().get(0)).setCellValueFactory(new PropertyValueFactory<JobInfo,String>("id"));
		((TableColumn<JobInfo, String>)jobs.getColumns().get(1)).setCellValueFactory(new P_Factory());
		jobs.setContextMenu(cm);
		jobs.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

			@Override
			public void handle(ContextMenuEvent event) {
				if(jobs.getSelectionModel().getSelectedCells().size() < 1) {
					return;
				}
				int row = jobs.getSelectionModel().getSelectedCells().get(0).getRow();
				
				if(0 >= row && row < jobs.getItems().size() && jobs.getItems().get(row).needsDownload()) {
					download.setDisable(false);
				} else {
					download.setDisable(true);
				}
				
				
			}
		});
		logService.info("init");
	}
	
	
	
	private class P_Factory implements Callback<CellDataFeatures<JobInfo, String>, ObservableValue<String>> {

		@Override
		public ObservableValue<String> call(final CellDataFeatures<JobInfo, String> param) {
			return new ObservableValue<String>() {

				@Override
				public void addListener(InvalidationListener listener) {
					
					
				}

				@Override
				public void removeListener(InvalidationListener listener) {
					
					
				}

				@Override
				public void addListener(ChangeListener<? super String> listener) {
					
					
				}

				@Override
				public void removeListener(ChangeListener<? super String> listener) {
					
					
				}

				@Override
				public String getValue() {
					 JobInfo ji = param.getValue();
					 return ji.getState().toString() + (ji.needsDownload()?" - needs download":"");
				}
			};
		}
		
	}
}
