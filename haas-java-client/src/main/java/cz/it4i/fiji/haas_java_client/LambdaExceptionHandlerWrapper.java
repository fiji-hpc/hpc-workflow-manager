package cz.it4i.fiji.haas_java_client;

public class LambdaExceptionHandlerWrapper {

	public interface Runnable {
		void run() throws Exception;
	}

	public static void wrap(Runnable r) {
		try {
			r.run();
		} catch (Exception e) {
			//ignore
		}
	}
}
