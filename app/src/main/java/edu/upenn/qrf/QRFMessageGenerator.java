package edu.upenn.qrf;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import edu.upenn.qrf.QRFUser;
import edu.upenn.recorder.QRFAppLinkData;
import edu.upenn.recorder.QRFBase;

/**
 * @author Martin van Velsen <vvelsen@knossys.com>
 */
public class QRFMessageGenerator extends QRFBase {

	public static String MacID = "-1";

	/**
	 * 
	 */
	public QRFMessageGenerator() {
		if (MacID.equals("-1") == true) {
			initID();
		}
	}
	
	/**
	 * 
	 * @param str
	 * @param subStr
	 * @return
	 */
	private int countMatches (String str, String subStr) {
	  /*
	  Pattern p = Pattern.compile(subStr);
	  Matcher m = p.matcher(str);
	  int count = 0;
	  while (m.find()){
	    count++;
	  }
	  
	  return (count);
	  */
	  
	  int lastIndex = 0;
	  int count = 0;

	  while(lastIndex != -1) {
	    lastIndex = str.indexOf(subStr,lastIndex);

	    if(lastIndex != -1) {
	      count++;
	      lastIndex += subStr.length();
	    }
	  }	  
	  
	  return (count);
	}

	/**
	 * 
	 */
	protected void listIPs() {
		debug("listIPs ()");

		Enumeration<NetworkInterface> e;

		try {
			e = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();

			Enumeration<InetAddress> ee = n.getInetAddresses();

			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				String hostAddress = i.getHostAddress();
				debug("Found possible ip/mac: " + hostAddress);

				int matches=countMatches(hostAddress, ":");
				
				//debug ("MAC address test: " + matches);
				
				if (matches>2) {
				  MacID = hostAddress;
				} else {
				  if ((!hostAddress.equals("127.0.0.1") && (hostAddress.indexOf(".") != -1))) {
					  MacID = hostAddress;
				  }
				}  
			}
		}
	}

	/**
	 * 
	 */
	public void initID() {
		debug("initID ()");

		listIPs();

		debug("Fixed ID to be: " + MacID);
	}

	/**
	 * @param aMessage
	 * @return
	 */
	public String createTextMessage(String aMessage) {
		return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><message>" + aMessage + "</message></msg>"));
	}

	/**
	 * <msg> <session>28a92ba6-80f7-11e7-bb31-be2e44b06b34</session>
	 * <acknowledgement_to_server>MESSAGE ACCEPTED</acknowledgement_to_server>
	 * </msg>
	 * 
	 * @return
	 */
	public String createAcceptMessage() {
		return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><device_identifier>" + MacID + "</device_identifier><id>"+QRFAppLinkData.currentInfo.studentID+"</id><acknowledgement_to_server>MESSAGE ACCEPTED</acknowledgement_to_server></msg>"));
	}

	/**
	 * <msg> <session>28a92ba6-80f7-11e7-bb31-be2e44b06b34</session>
	 * <acknowledgement_to_server>MESSAGE DROPPED</acknowledgement_to_server>
	 * </msg>
	 * 
	 * @return
	 */
	public String createRejectedMessage() {
		return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><device_identifier>" + MacID + "</device_identifier><id>"+QRFAppLinkData.currentInfo.studentID+"</id><acknowledgement_to_server>MESSAGE DROPPED</acknowledgement_to_server></msg>"));
	}

	/**
	 * <msg> <session>28a92ba6-80f7-11e7-bb31-be2e44b06b34</session>
	 * <acknowledgement_to_server>ready</acknowledgement_to_server> </msg>
	 * 
	 * @return
	 */
	/*
	public String createNextMessage() {
		return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><device_identifier>" + MacID
		    + "</device_identifier><acknowledgement_to_server>READY</acknowledgement_to_server></msg>"));
	}
	*/

	/**
	 * @param aMessage
	 * @return
	 */
	public String createActionMessage(String anAction) {
		return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><device_identifier>" + MacID
		    + "</device_identifier><action>" + anAction + "</action></msg>"));
	}

	/**
	 * @param aMessage
	 * @return
	 */
	public String createClassnameMessage(String aClassname) {
		return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><device_identifier>" + MacID
		    + "</device_identifier><class_name>" + aClassname + "</class_name></msg>"));
	}
	
	/**
	 * @param aMessage
	 * @return
	 */
	public String createAdviceMessage(String aMessage) {
		return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><device_identifier>" + MacID
		    + "</device_identifier><advice><![CDATA[" + aMessage + "]]></advice></msg>"));
	}	
	
  /** 
   * @return
   */
  public String createFilepartMessage(String aFilePartXML) {
    return (wrapInXML("<msg><session>" + QRFLinkData.session + "</session><device_identifier>" + MacID + "</device_identifier><id>"+QRFAppLinkData.currentInfo.studentID+"</id>"+aFilePartXML+"</msg>"));
  }	
	
	/**
	 * @param aMessage
	 * @return
	 */
	public String createMessage(String aMessage) {
		return (wrapInXML(aMessage));
	}

	/**
	 * @param aMessage
	 * @return
	 */
	public String wrapInXML(String aMessage) {
		debug("wrapInXML ()");

		StringBuffer formatted = new StringBuffer();
		formatted.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>");
		formatted.append(aMessage);
		return (formatted.toString());
	}

	/**
	 * @param aName
	 * @param aValue
	 * @return
	 */
	public static Element createElement(String aName, String aValue) {
		Element element = new Element(aName);
		element.addContent(aValue);
		return element;
	}

	/**
	 * <msg> <List> <Item1> <Name>John</Name> <ID>A01</ID> </Item1> <Item2>
	 * <Name>Mary</Name> <ID>A02</ID> </Item2> . . . </List> </msg>
	 * 
	 */
	public static Element createStudentList() {

		// QRFBase.staticdebug ("createStudentList ()");

		Element root = new Element("msg");

		Element list = new Element("List");

		Element item1 = new Element("Item");

		item1.addContent(createElement("Name", "John"));
		item1.addContent(createElement("ID", "A01"));

		Element item2 = new Element("Item");

		item2.addContent(createElement("Name", "Mary"));
		item2.addContent(createElement("ID", "A02"));

		list.addContent(item1);
		list.addContent(item2);

		root.addContent(createElement("session", QRFLinkData.session));
		root.addContent(list);

		return (root);
	}

	/**
	 * Example from the specs:
	 * 
	 * <configuration> <list> <item>Model wrong</item> <item>Can't observe</item>
	 * <item>Same student too recent</item> <item>Old information</item>
	 * <item>Other</item> </list> </configuration>
	 * 
	 * @return
	 */
	public static Element createConfiguration() {

		// QRFBase.staticdebug ("createConfiguration ()");

		Element root = new Element("configuration");

		Element list = new Element("list");

		list.addContent(createElement("item", "Model wrong"));
		list.addContent(createElement("item", "Can't observe"));
		list.addContent(createElement("item", "Same student too recent"));
		list.addContent(createElement("item", "Old information"));
		list.addContent(createElement("item", "Other"));

		root.addContent(list);
		root.addContent(createElement("session", QRFLinkData.session));

		return (root);
	}

	/**
	 * Example from the specs:
	 * 
	 * <msg> <name> Mary </name> <ID> A02 </ID> <Location>?</Location>
	 * <time_when_occurred> 10:40:58 AM </time_when_occurred> <was> Bored </was>
	 * <is> Confused </is> <occurrence_type> Change in affect </occurrence_type>
	 * </msg>
	 * 
	 * @return
	 */
	public static Element createMainMessage(String aName, String anID, String aLocation, String aTime, String aState,
	    String anAffect, String anOccurrence) {

		// QRFBase.staticdebug ("createMainMessage ()");

		Element root = new Element("msg");

		root.addContent(createElement("name", aName));
		root.addContent(createElement("ID", anID));
		root.addContent(createElement("Location", aLocation));
		root.addContent(createElement("time_when_occurred", aTime));
		root.addContent(createElement("emotion-pattern", aState));
		root.addContent(createElement("behavior-pattern", anAffect));
		root.addContent(createElement("occurrence_type", anOccurrence));

		root.addContent(createElement("session", QRFLinkData.session));

		return (root);
	}

	/**
	 * Example from the specs:
	 * 
	 * <msg> <name> Mary </name> <ID> A02 </ID> <Location>?</Location>
	 * <time_when_occurred> 10:40:58 AM </time_when_occurred> <was> Bored </was>
	 * <is> Confused </is> <occurrence_type> Change in affect </occurrence_type>
	 * </msg>
	 * 
	 * @return
	 */
	public static Element createMainMessage(QRFUser aUser, String aLocation, String aTime, String aState, String anAffect,
	    String anOccurrence) {

		// QRFBase.staticdebug ("createMainMessage ()");

		Element root = new Element("msg");

		root.addContent(createElement("name", aUser.name));
		root.addContent(createElement("ID", aUser.id));
		root.addContent(createElement("Location", aLocation));
		root.addContent(createElement("time_when_occurred", aTime));
		root.addContent(createElement("was", aState));
		root.addContent(createElement("is", anAffect));
		root.addContent(createElement("occurrence_type", anOccurrence));

		root.addContent(createElement("session", QRFLinkData.session));

		return (root);
	}

	/**
	 * Example from the specs:
	 * 
	 * <msg> <string>no<string> </msg>
	 * 
	 * @return
	 */
	public static Element createYesNoMessage(String aYesNo) {

		QRFBase.staticdebug("createYesNoMessage (" + aYesNo + ")");

		Element root = new Element("msg");

		root.addContent(createElement("string", aYesNo.toLowerCase()));

		root.addContent(createElement("session", QRFLinkData.session));

		return (root);
	}

	/**
	 * 
	 * @param root
	 * @return
	 */
	public static String XMLFragmentToString(Element root) {
		Document doc = new Document();

		doc.setRootElement(root);

		XMLOutputter outter = new XMLOutputter(Format.getCompactFormat().setOmitDeclaration(true));
		// outter.setFormat(Format.getPrettyFormat());

		String userString = null;

		try {
			userString = outter.outputString(doc);
		} catch (Exception e) {
			QRFBase.staticdebug("Error generating XML string: " + e.getMessage());
			return (null);
		}

		return (userString);
	}

	/**
	 * 
	 * @return
	 */
	public String sendLogs(String aCodername) {
		debug ("sendLogs ("+aCodername+")");

		Element root = new Element("msg");
		
		Element loglist = new Element("list");
		
		root.addContent(loglist);

		root.addContent(createElement("username_of_researcher",aCodername));

    String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		
		root.addContent(createElement("current_date_and_time", date));

		return (XMLFragmentToString (root));
	}
}
