package cz.it4i.fiji.haas;

import javax.swing.WindowConstants;

import cz.it4i.fiji.haas.ui.ProgressDialog;

public class ModalDialogs {
	public static ProgressDialog doModal(ProgressDialog dialog) {
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		new Thread(()->dialog.setVisible(true)).start();
		return dialog;
	}

	public static CheckStatusOfHaaSWindow doModal(CheckStatusOfHaaSWindow window) {
		window.setModal(true);
		window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		new Thread(() -> window.setVisible(true)).start();
		return window;
	}
}
