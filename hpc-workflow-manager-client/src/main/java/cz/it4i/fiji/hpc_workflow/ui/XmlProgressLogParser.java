
package cz.it4i.fiji.hpc_workflow.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
		}
		catch (ParserConfigurationException exc) {
			return null;
		}

		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();

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
		for (String log : progressLogs) {
			// Find all task elements in the XML document and get their id and
			// progress:
			NodeList taskNodeList = null;
			try {
				taskNodeList = convertStringToXMLDocument(log).getElementsByTagName(
					"task");
			}
			catch (NullPointerException exc) {
				return true;
			}

			for (int nodeId = 0; nodeId < taskNodeList.getLength(); nodeId++) {
				Node currentNode = taskNodeList.item(nodeId);

				String description = currentNode.getTextContent();

				// Add the new task if a task with the same description is not present
				// in the list:
				if (!descriptionToTaskId.containsKey(description)) {
					descriptionToTaskId.put(description, taskIdCounter++);
					tableData.add(new MacroTask(description));
				}

				// Get the progress:
				long progress = Long.parseLong(currentNode.getChildNodes().item(1)
					.getNodeValue());

				int taskId = descriptionToTaskId.get(description);

				tableData.get(taskId).setProgress(nodeId, progress);
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

}
