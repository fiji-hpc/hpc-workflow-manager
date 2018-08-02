
package cz.it4i.fiji.commons;

import java.io.Closeable;
import java.util.Timer;
import java.util.TimerTask;

public class DoActionEventualy implements Closeable {

	private final Timer timer;

	public DoActionEventualy(final long timeout, final Runnable runnable) {
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				runnable.run();
			}
		}, timeout);
	}

	@Override
	public void close() {
		timer.cancel();
	}

}
