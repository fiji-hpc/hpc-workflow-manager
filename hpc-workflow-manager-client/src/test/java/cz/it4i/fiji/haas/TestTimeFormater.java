package cz.it4i.fiji.haas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.it4i.fiji.hpc_workflow.ui.RemainingTimeFormater;


public class TestTimeFormater {
	
	public static final Logger log = LoggerFactory.getLogger(cz.it4i.fiji.haas.TestTimeFormater.class);
	
	public static void main(String[] args) {
		log.info( RemainingTimeFormater.format((25*3600 + 5*60 + 6) * 1000 + 200));
	}
}
