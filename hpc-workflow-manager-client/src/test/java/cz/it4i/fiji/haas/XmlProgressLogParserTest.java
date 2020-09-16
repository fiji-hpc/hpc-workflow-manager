
package cz.it4i.fiji.haas;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import cz.it4i.fiji.hpc_workflow.parsers.ProgressLogParser;
import cz.it4i.fiji.hpc_workflow.parsers.XmlProgressLogParser;

public class XmlProgressLogParserTest {

	private ProgressLogParser parser;

	@Before
	public void initializeProgressLogParser() {
		parser = new XmlProgressLogParser();
	}

	@Test
	public void
		getLastUpdatedTimestampShouldReturnNegativeOneIfTheTimestampDoesNotExist()
	{
		List<String> progressLogs = new ArrayList<>();
		progressLogs.add("<job>\n" + "	<nodes>1</nodes>\n" +
			"	<task id=\"0\">\n" +
			"		<description>Sample task one</description>\n" +
			"		<progress>100</progress>\n" + "	</task>\n" +
			"	<task id=\"1\">\n" +
			"		<description>Sample task two</description>\n" +
			"		<progress>75</progress>\n" + "	</task>\n" + "</job>");

		long realTimestamp = -1;
		long readTimestamp = parser.getLastUpdatedTimestamp(0, progressLogs);
		System.out.println("The read timestamp is: " + readTimestamp);
		assertEquals(realTimestamp, readTimestamp);
	}

	@Test
	public void getLastUpdatedTimestampShouldRetrunCorrectTimestamp() {
		List<String> progressLogs = new ArrayList<>();
		progressLogs.add("<job>\n" + "	<nodes>1</nodes>\n" +
			"	<task id=\"0\">\n" +
			"		<description>Sample task one</description>\n" +
			"		<progress>100</progress>\n" + "	</task>\n" +
			"	<task id=\"1\">\n" +
			"		<description>Sample task two</description>\n" +
			"		<progress>75</progress>\n" + "	</task>\n" +
			"<lastUpdated>100</lastUpdated>\n " + "</job>");

		long realTimestamp = 100;
		long readTimestamp = parser.getLastUpdatedTimestamp(0, progressLogs);
		assertEquals(realTimestamp, readTimestamp);
	}
}
