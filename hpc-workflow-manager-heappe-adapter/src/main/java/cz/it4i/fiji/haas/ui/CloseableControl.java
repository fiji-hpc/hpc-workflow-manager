package cz.it4i.fiji.haas.ui;

import java.io.Closeable;

public interface CloseableControl extends Closeable {

	@Override
	void close();

}
