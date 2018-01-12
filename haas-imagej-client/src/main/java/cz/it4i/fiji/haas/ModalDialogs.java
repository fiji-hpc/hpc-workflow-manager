package cz.it4i.fiji.haas;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

public class ModalDialogs {
	public static <T extends JDialog>T doModal(T dialog) {
		dialog.setModal(true);
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		new Thread(()->dialog.setVisible(true)).start();
		return dialog;
	}
}
