package cz.it4i.fiji.haas;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cz.it4i.fiji.haas_spim_benchmark.ui.RemoteFileInfo;
import cz.it4i.fiji.haas_spim_benchmark.ui.RemoteFilesInfoControl;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;

public class RunRemoteFilesView {

	
	public static void main(String[] args) {
		List<ObservableValue<RemoteFileInfo>> files = new LinkedList<>();
		add(files, "Some file.txt", 100025456);
		
		class Window extends cz.it4i.fiji.haas.ui.FXFrame<RemoteFilesInfoControl>{
			public Window() {
				super(()-> new RemoteFilesInfoControl(files));
			}
		}
		
		new Window().setVisible(true);
	}
	
	static void add(Collection<ObservableValue<RemoteFileInfo>> files, String name, long size) {
		RemoteFileInfo file = new RemoteFileInfo() {
			
			@Override
			public long getSize() {
				return size;
			}
			
			@Override
			public String getName() {
				return name;
			}
			
		};
		ObservableValue<RemoteFileInfo> value = new ObservableValueBase<RemoteFileInfo>() {

			@Override
			public RemoteFileInfo getValue() {
				return file;
			}
		};
		
		files.add(value);
	}

}
