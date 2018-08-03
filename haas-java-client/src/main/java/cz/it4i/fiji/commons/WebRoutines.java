
package cz.it4i.fiji.commons;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final public class WebRoutines {

	private static final Logger log = LoggerFactory.getLogger(
		cz.it4i.fiji.commons.WebRoutines.class);

	public static boolean doesURLExist(final URL url) {
		// We want to check the current URL
		HttpURLConnection.setFollowRedirects(false);
		// We don't need to get data
		try {
			final HttpURLConnection httpURLConnection = (HttpURLConnection) url
				.openConnection();
			httpURLConnection.setRequestMethod("HEAD");
			// Some websites don't like programmatic access so pretend to be a browser
			httpURLConnection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
			final int responseCode = httpURLConnection.getResponseCode();
			return responseCode == HttpURLConnection.HTTP_OK;
			// We only accept response code 200
		}
		catch (final IOException exc) {
			log.error(exc.getMessage(), exc);
			return false;
		}
	}

	private WebRoutines() {}
}
