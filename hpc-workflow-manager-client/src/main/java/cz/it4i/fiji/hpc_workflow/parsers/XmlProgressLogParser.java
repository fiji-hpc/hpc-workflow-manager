
package cz.it4i.fiji.hpc_workflow.parsers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import cz.it4i.fiji.hpc_workflow.core.MacroTask;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;

public class XmlProgressLogParser implements ProgressLogParser {

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
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder = null;

		try {
			builder = factory.newDocumentBuilder();
			builder.setErrorHandler(null);
			return builder.parse(new InputSource(new StringReader(xmlString)));
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
			// Do nothing.
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
		for (int rank = 0; rank < progressLogs.size(); rank++) {
			// Ignore old progress files:
			if (jobStartedTimestamp > getLastUpdatedTimestamp(rank, progressLogs)) {
				return true;
			}

			// Find all task elements in the XML document and get their id and
			// progress:
			NodeList taskXmlNodeList = null;
			try {
				taskXmlNodeList = convertStringToXMLDocument(progressLogs.get(rank))
					.getElementsByTagName("task");
			}
			catch (NullPointerException exc) {
				return true;
			}

			for (int counter = 0; counter < taskXmlNodeList.getLength(); counter++) {
				Node currentNode = taskXmlNodeList.item(counter);

				String description = currentNode.getChildNodes().item(0)
					.getTextContent();

				// Add the new task if a task with the same description is not present
				// in the list:
				if (!descriptionToTaskId.containsKey(description)) {
					descriptionToTaskId.put(description, taskIdCounter);
					// Set "indeterminate" progress indicator: state -1.
					tableData.add(new MacroTask(description, rank));
					taskIdCounter++;
				}

				int taskId = descriptionToTaskId.get(description);

				// Set the new progress if it exists:
				Node progressXmlNode = currentNode.getChildNodes().item(1);
				if (progressXmlNode != null) {
					long progress = Long.parseLong(progressXmlNode.getTextContent());
					tableData.get(taskId).setProgress(rank, progress);
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

	public static boolean fileIsValidXML(String xmlSourceFile) {
		Document document = convertStringToXMLDocument(xmlSourceFile);
		return document != null;
	}

}
