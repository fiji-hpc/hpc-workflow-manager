package cz.it4i.fiji.haas_spim_benchmark.ui;

public class RemainingTimeFormater {

	public static String format(long durationInMiliseconds) {
		long s = durationInMiliseconds / 1000;
		return String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
	}
}
