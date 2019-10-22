
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
		ObservableList<MacroTask> tableData,
		Map<String, Map<Integer, SimpleLongProperty>> descriptionToProperty)
	{
		for (int nodeId = 0; nodeId < progressLogs.size(); nodeId++) {
			// Find all task elements in the XML document and get their id and
			// progress:
			NodeList taskNodeList = null;
			try {
				taskNodeList = convertStringToXMLDocument(progressLogs.get(nodeId))
					.getElementsByTagName("task");
			}
			catch (NullPointerException exc) {
				return true;
			}

			for (int counter = 0; counter < taskNodeList.getLength(); counter++) {
				Node currentNode = taskNodeList.item(counter);

				String description = currentNode.getChildNodes().item(0)
					.getTextContent();

				// Add the new task if a task with the same description is not present
				// in the list:
				if (!descriptionToTaskId.containsKey(description)) {
					descriptionToTaskId.put(description, taskIdCounter++);
					tableData.add(new MacroTask(description));
				}

				int taskId = descriptionToTaskId.get(description);

				// Set the new progress if it exists:
				Node progressNode = currentNode.getChildNodes().item(1);
				if (progressNode != null) {
					long progress = Long.parseLong(progressNode.getTextContent());
					tableData.get(taskId).setProgress(nodeId, progress);
				}

				setDescriptionToPropertyIfPossible(descriptionToProperty, description,
					nodeId, tableData.get(taskId).getProgress(nodeId));
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
