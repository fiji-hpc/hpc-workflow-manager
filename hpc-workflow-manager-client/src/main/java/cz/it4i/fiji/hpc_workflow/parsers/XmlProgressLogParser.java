
package cz.it4i.fiji.hpc_workflow.parsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import cz.it4i.fiji.hpc_workflow.core.MacroTask;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;

public class XmlProgressLogParser implements ProgressLogParser {

	// Maps rank to last updated timestamp:
	private Map<Integer, Long> previousTimestamp = new HashMap<>();

	private static Logger logger = LoggerFactory.getLogger(
		XmlProgressLogParser.class);

	@Override
	public int getNumberOfNodes(List<String> progressLogs) {
		Document document = convertStringToXMLDocument(progressLogs.get(0));
		Node sizeNode = findNode(document, "//nodes");
		if (sizeNode == null) {
			return 0;
		}
		return Integer.parseInt(sizeNode.getTextContent());
	}

	@Override
	public long getLastUpdatedTimestamp(int rank, List<String> progressLogs) {
		Document document = convertStringToXMLDocument(progressLogs.get(rank));
		Node timestampNode = findNode(document, "//lastUpdated");
		if (timestampNode == null) {
			return -1;
		}
		return Long.parseLong(timestampNode.getTextContent());
	}

	private static Document convertStringToXMLDocument(String xmlString) {
		// Parser that produces DOM object trees from XML content
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		// When not validating using DTD it should be set to false.
		documentBuilderFactory.setValidating(false);
		// Ignore indentation whitespaces on the XML document
		documentBuilderFactory.setIgnoringElementContentWhitespace(true);
		documentBuilderFactory.setIgnoringComments(true);

		// Attempt to load and set schema:
		Source schemaFile = new StreamSource(XmlProgressLogParser.class
			.getClassLoader().getResourceAsStream("progress.xsd"));
		SchemaFactory schemaFactory = SchemaFactory.newInstance(
			XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = schemaFactory.newSchema(schemaFile);
			documentBuilderFactory.setSchema(schema);
		}
		catch (NullPointerException | SAXException exc) {
			logger.debug("Failed loading schema. exc {} ", exc);
		}

		DocumentBuilder documentBuilder = null;

		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
			return documentBuilder.parse(new InputSource(new StringReader(
				xmlString)));
		}
		catch (Exception exc) {
			return null;
		}
	}

	private Node findNode(Document document, String xpathExpression) {
		XPathFactory xpathFactory = XPathFactory.newInstance();

		XPath xpath = xpathFactory.newXPath();

		Node node = null;
		try {
			XPathExpression expr = xpath.compile(xpathExpression);

			node = (Node) expr.evaluate(document, XPathConstants.NODE);
		}
		catch (Exception exc) {
			logger.debug("Could not find node at xpath: {} ", xpathExpression);
		}
		return node;
	}

	// Maps description to taskId:
	private Map<String, Integer> descriptionToTaskId = new HashMap<>();

	private int taskIdCounter = 0;

	@Override
	public boolean parseProgressLogs(List<String> progressLogs,
		long jobStartedTimestamp, ObservableList<MacroTask> tableData,
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty)
	{
		long progress;
		int size = progressLogs.size();
		long lastUpdatedTimestamp;

		for (int rank = 0; rank < size; rank++) {

			// Ignore old progress files:
			lastUpdatedTimestamp = getLastUpdatedTimestamp(rank, progressLogs);
			this.previousTimestamp.putIfAbsent(rank, -1L);

			if (jobStartedTimestamp > lastUpdatedTimestamp ||
				lastUpdatedTimestamp <= this.previousTimestamp.get(rank))
			{
				logger.debug(
					"XML log is up to date and progress does not need to be updated.");
				return true;
			}

			this.previousTimestamp.put(rank, lastUpdatedTimestamp);

			// Get the XML structure:
			NodeList taskXmlNodeList = null;
			try {
				taskXmlNodeList = convertStringToXMLDocument(progressLogs.get(rank))
					.getElementsByTagName("task");
			}
			catch (NullPointerException exc) {
				logger.debug(
					"Catastrophic error when trying to read XML document from text. Exc: {} ",
					exc);
				return false;
			}

			// Find all task elements in the XML structure and get their id and
			// progress:
			int numberOfTasks = taskXmlNodeList.getLength();
			for (int counter = 0; counter < numberOfTasks; counter++) {
				Node currentNode = taskXmlNodeList.item(counter);

				String description = currentNode.getChildNodes().item(0)
					.getTextContent();

				// Add the new task if a task with the same description is not present
				// in the list:
				if (!descriptionToTaskId.containsKey(description)) {
					descriptionToTaskId.put(description, taskIdCounter);
					// Set "indeterminate" progress indicator: state -1.
					tableData.add(new MacroTask(description));
					taskIdCounter++;
				}

				int taskId = descriptionToTaskId.get(description);

				// Set the new progress if it exists:
				Node progressXmlNode = currentNode.getChildNodes().item(1);
				if (progressXmlNode != null) {
					progress = Long.parseLong(progressXmlNode.getTextContent());
					tableData.get(taskId).setProgress(rank, progress);
				}
				else {
					tableData.get(taskId).setIndeterminateProgress(rank);
				}

				setDescriptionToPropertyIfPossible(descriptionToProperty, description,
					rank, tableData.get(taskId).getProgress(rank));
			}

		}
		return true;
	}

	private void setDescriptionToPropertyIfPossible(
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty,
		String description, int nodeId, long progress)
	{
		try {
			descriptionToProperty.get(description).get(nodeId).set(progress);
		}
		catch (Exception exc) {
			// Do nothing.
		}
	}

	public static boolean isXML(String sourceString) {
		boolean isXML = sourceString.contains("<?xml");
		if (!isXML) {
			logger.debug("Progress log is CSV format.");
		}
		else {
			logger.debug("Progress log is XML file.");
		}
		return isXML;
	}

}
