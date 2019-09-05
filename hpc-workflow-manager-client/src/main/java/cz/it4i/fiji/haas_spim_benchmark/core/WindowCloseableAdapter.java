
package cz.it4i.fiji.haas_spim_benchmark.core;

import java.awt.Window;
import java.io.Closeable;

public class WindowCloseableAdapter implements Closeable {

	private Window window;
	private boolean closed;

	public WindowCloseableAdapter() {}

	public WindowCloseableAdapter(final Window window) {
		this.window = window;
	}

	public synchronized void setWindowAndShowIt(final Window window) {
		if (!closed) {
			this.window = window;
			this.window.setVisible(true);
		}
		else {
			window.dispose();
		}
	}

	@Override
	public synchronized void close() {
		if (!closed) {
			if (window != null) {
				window.dispose();
			}
			closed = true;
		}
	}
}
