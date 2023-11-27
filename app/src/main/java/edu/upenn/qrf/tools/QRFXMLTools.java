package edu.upenn.qrf.tools;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom2.JDOMException;
import org.jdom2.output.DOMOutputter;

/**
 * @author vvelsen
 */
public class QRFXMLTools {
		
	/**
	 * @param document
	 * @return
	 * @throws TransformerException
	 */
  public static String prettyPrint(org.jdom2.Document document) throws TransformerException {
  	
  	org.w3c.dom.Document outputDom=null;
  	
		try {
			outputDom = new DOMOutputter().output(document);		
		} catch (JDOMException e) {
			e.printStackTrace();
			return ("");
		}
  	
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    DOMSource source = new DOMSource(outputDom);
    StringWriter strWriter = new StringWriter();
    StreamResult result = new StreamResult(strWriter);
 
    transformer.transform(source, result);
 
    return strWriter.getBuffer().toString();
  }	
}
