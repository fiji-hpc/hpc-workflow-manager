
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
		progressLogs.add("<job>\r\n" + "	<nodes>1</nodes>\r\n" +
			"	<task id=\"0\">\r\n" +
			"		<description>Sample task one</description>\r\n" +
			"		<progress>100</progress>\r\n" + "	</task>\r\n" +
			"	<task id=\"1\">\r\n" +
			"		<description>Sample task two</description>\r\n" +
			"		<progress>75</progress>\r\n" + "	</task>\r\n" + "</job>");

		long realTimestamp = -1;
		long readTimestamp = parser.getLastUpdatedTimestamp(progressLogs);
		System.out.println("The read timestamp is: " + readTimestamp);
		assertEquals(realTimestamp, readTimestamp);
	}

	@Test
	public void getLastUpdatedTimestampShouldRetrunCorrectTimestamp() {
		List<String> progressLogs = new ArrayList<>();
		progressLogs.add("<job>\r\n" + "	<nodes>1</nodes>\r\n" +
			"	<lastUpdated>100</lastUpdated>\r\n <task id=\"0\">\r\n" +
			"		<description>Sample task one</description>\r\n" +
			"		<progress>100</progress>\r\n" + "	</task>\r\n" +
			"	<task id=\"1\">\r\n" +
			"		<description>Sample task two</description>\r\n" +
			"		<progress>75</progress>\r\n" + "	</task>\r\n" + "</job>");

		long realTimestamp = 100;
		long readTimestamp = parser.getLastUpdatedTimestamp(progressLogs);
		assertEquals(realTimestamp, readTimestamp);
	}
}
