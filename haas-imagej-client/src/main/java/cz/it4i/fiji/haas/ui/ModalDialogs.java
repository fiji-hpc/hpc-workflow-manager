package cz.it4i.fiji.haas.ui;

import java.awt.Dialog.ModalityType;
import java.util.concurrent.CompletableFuture;

import javax.swing.JDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModalDialogs {
	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.ui.ModalDialogs.class);
	public static <T extends JDialog>T doModal(T dialog, int operation) {
		dialog.setModal(true);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.setDefaultCloseOperation(operation);
		CompletableFuture.runAsync(()->dialog.setVisible(true));
		return dialog;
	}
}
