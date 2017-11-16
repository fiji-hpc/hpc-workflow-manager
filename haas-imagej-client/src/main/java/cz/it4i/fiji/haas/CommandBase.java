package cz.it4i.fiji.haas;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

class CommandBase {
	@Parameter
	private LogService _log;

	protected ImageJGate getGate() {
		return gate;
	}

	private ImageJGate gate = new ImageJGate() {

		@Override
		public LogService getLog() {
			return _log;
		}
	};
}
