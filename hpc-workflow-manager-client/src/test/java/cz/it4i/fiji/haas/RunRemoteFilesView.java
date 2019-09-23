
package cz.it4i.fiji.haas;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cz.it4i.fiji.hpc_workflow.ui.RemoteFileInfo;
import cz.it4i.fiji.hpc_workflow.ui.RemoteFilesInfoControl;
import cz.it4i.swing_javafx_ui.JavaFXRoutines;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ObservableValueBase;

public class RunRemoteFilesView {

	public static void main(String[] args) {
		List<ObservableValue<RemoteFileInfo>> files = new LinkedList<>();
		add(files, "Some file.txt", 100025456);

		RemoteFilesInfoControl remoteFilesInfoControl =
			new RemoteFilesInfoControl();

		JavaFXRoutines.runOnFxThread(() -> {
			remoteFilesInfoControl.setFiles(files);
			remoteFilesInfoControl.openWindow(null);
		});
	}

	static void add(Collection<ObservableValue<RemoteFileInfo>> files,
		String name, long size)
	{
		RemoteFileInfo file = new RemoteFileInfo() {

			@Override
			public Long getSize() {
				return size;
			}

			@Override
			public String getName() {
				return name;
			}

		};

		ObservableValue<RemoteFileInfo> value =
			new ObservableValueBase<RemoteFileInfo>()
			{

				@Override
				public RemoteFileInfo getValue() {
					return file;
				}
			};

		files.add(value);
	}

}
