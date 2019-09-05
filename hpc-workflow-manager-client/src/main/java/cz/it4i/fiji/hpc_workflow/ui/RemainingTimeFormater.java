package cz.it4i.fiji.hpc_workflow.ui;

public class RemainingTimeFormater {

	private RemainingTimeFormater() {
		// Private constructor.
	}
	
	public static String format(long durationInMiliseconds) {
		long s = durationInMiliseconds / 1000;
		return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
	}
}
