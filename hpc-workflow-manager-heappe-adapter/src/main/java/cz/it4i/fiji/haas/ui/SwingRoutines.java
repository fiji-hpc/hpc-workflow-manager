package cz.it4i.fiji.haas.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

public interface SwingRoutines {

	static void centerOnScreen(Component component) {
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
	    int x = (int) ((dimension.getWidth() - component.getWidth()) / 2);
	    int y = (int) ((dimension.getHeight() - component.getHeight()) / 2);
	    component.setLocation(x, y);
	}

}
