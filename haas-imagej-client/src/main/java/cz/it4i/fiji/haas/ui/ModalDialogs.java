package cz.it4i.fiji.haas.ui;

import javax.swing.JDialog;

public class ModalDialogs {
	public static <T extends JDialog>T doModal(T dialog, int operation) {
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(operation);
		new Thread(()->dialog.setVisible(true)).start();
		return dialog;
	}
}
