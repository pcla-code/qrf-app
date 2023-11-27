package edu.upenn.qrf.comm;

import com.rabbitmq.client.*;
import com.rabbitmq.client.ConnectionFactory;

//import Networking.util.Base64;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.ArrayList;
//import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.upenn.qrf.QRFClientProxy;
import edu.upenn.qrf.QRFLinkData;
import edu.upenn.qrf.QRFMessageGenerator;
import edu.upenn.qrf.QRFMessageParser;
//import edu.upenn.qrf.tools.QRFDataTools;
import edu.upenn.qrf.tools.QRFXMLTools;
import edu.upenn.recorder.QRFAppLinkData;
import edu.upenn.recorder.QRFBase;

/**
 * @author Martin van Velsen <vvelsen@knossys.com>
 */
public class QRFCommBase extends QRFBase {

	//private String serverHost = "45.79.152.186";
  private String serverHost = "192.168.0.100";
	private String serverUsername = "qrf";
	private String serverPassword = "qrf";

	HashMap<String, QRFClientProxy> sessions = new HashMap<String, QRFClientProxy>();

	protected QRFMessageGenerator messageGenerator = new QRFMessageGenerator();
	protected QRFMessageParser messageParser = new QRFMessageParser();

	protected ConnectionFactory factory = new ConnectionFactory();
	protected Connection connection = null;
	protected Channel channel = null;
	protected Consumer consumer = null;
	protected String queueName = "-1";

	public static QRFLinkData linkData = null;
	private Logger logger = null;

	protected Boolean isBroker = false;

	/**
	 * 
	 * @param channel
	 */
	public QRFCommBase() {

		logger = LoggerFactory.getLogger(QRFCommBase.class);
		
		QRFBase.useInternalLogging=true;

		if (linkData == null) {
			UUID uuid = UUID.randomUUID();

			linkData = new QRFLinkData();
			QRFLinkData.session = ("S" + uuid.toString().replaceAll("\\D", ""));
			QRFLinkData.generator = new QRFMessageGenerator();
			QRFLinkData.parser = new QRFMessageParser();
			QRFLinkData.responses = new ArrayList<String>();
		}
	}

	/**
	 * 
	 */
	/*
	protected void debug(String aMessage) {
		logger.info(aMessage);
		System.out.println(aMessage);
	}
	*/

	/**
	 * @return
	 */
	public String getServerHost() {
		return serverHost;	  
	}

	/**
	 * @param serverHost
	 */
	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}
	
	/** 
	 * @param aUsername
	 */
	public void setServerUsername (String aUsername) {
	  this.serverUsername=aUsername;
	}
	
	/** 
	 * @return
	 */
	public String getServerUsername () {
	  return (this.serverUsername);
	}
	
	/** 
	 * @param aPassword
	 */
	public void setServerPassword (String aPassword) {
	  this.serverPassword=aPassword;
	}
	
	/** 
	 * @return
	 */
	public String getServerPassword () {
	  return (this.serverPassword);
	}

	/**
	 * 
	 */
	protected Boolean addSubject(String aSubject) {
		// debug ("addSubject ("+aSubject+")");

		try {
			channel.queueBind(queueName, QRFLinkData.EXCHANGE_NAME, aSubject + ".*");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			debug("Error adding topic/subject: " + e.getMessage());
			return (false);
		}

		return (true);
	}

	/**
	 * 
	 * @param aSession
	 */
	protected QRFClientProxy getSession(String aSession) {
		// debug ("getSession ("+aSession+")");

		if (sessions.containsKey(aSession) == false) {
			return (null);
		}

		return (sessions.get(aSession));
	}
	
  /**
   * 
   * @return
   */
  public Boolean clientInit () {
    debug ("clientInit ()");        
    
    if (init(false)==false) {
      return (false);
    }
      
    // Make sure there is a way where we can be specifically addressed. By default
    // both the server and the client both subscribe to the 'broadcast' key.
    addSubject (QRFLinkData.session);
      
    broadcast (messageGenerator.createActionMessage("init"));
      
    return (true);
  }	

	/**
	 * 
	 * @param aSession
	 */
	protected QRFClientProxy registerSession(String aSession) {
		// debug ("registerSession ("+aSession+")");

		if (sessions.containsKey(aSession) == false) {
			// debug ("We don't have this session yet, registering ...");
			QRFClientProxy newProxy = new QRFClientProxy();
			newProxy.setSessionID(aSession);
			newProxy.setState(QRFClientProxy.CLIENTSTATE_IDLE);
			sessions.put(aSession, newProxy);
			addSubject(aSession);
			return (newProxy);
		}

		return (sessions.get(aSession));
	}

	/**
	 * 
	 * @return
	 */
	protected Boolean serverInit() {
    debug ("serverInit ("+getServerHost()+" -> "+getServerUsername ()+","+getServerPassword ()+")");
    
		factory.setHost(getServerHost());
    factory.setUsername(getServerUsername ());
    factory.setPassword(getServerPassword ());

		try {
			connection = factory.newConnection();
		} catch (Exception e) {
			debug("Error connecting to server: " + e.getMessage());
			e.printStackTrace();
			return (false);
		}

		try {
			channel = connection.createChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (false);
		}

		try {
			channel.exchangeDeclare(QRFLinkData.EXCHANGE_NAME, "topic");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (false);
		}

		try {
			queueName = channel.queueDeclare().getQueue();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (false);
		}

		// addSubject ("broadcast");

		// Signal to the client that we're connected and ready to start
		// executing the protocol
			
		if (QRFLinkData.currentListener != null) {
			//debug ("We've got a configured client, calling ready () on it ...");
			QRFLinkData.currentListener.ready();
		} else {
			//debug ("Calling ready() on self ...");
			ready();
		}

		return (true);
	}

	/**
	 * 
	 * @return
	 */
	public Boolean init(Boolean configureAsBroker) {
	  debug ("init ("+configureAsBroker+")");
	  
		isBroker = configureAsBroker;

		if (serverInit() == false) {
			return (false);
		}

		// debug("Using dynamically declared queue: " + queueName);

		addSubject("broadcast");

		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery (
			  String consumerTag, 
			  Envelope envelope, 
			  AMQP.BasicProperties properties, 
			  byte[] body) throws IOException {
			    debug ("handleDelivery ()");
				  String message = new String(body, "UTF-8");
				  processMessage(envelope, message);
			}
			
	    @Override
			public void handleShutdownSignal(java.lang.String consumerTag, ShutdownSignalException sig) {
	      debug ("handleDelivery ()");
	      processDisconnect ();
	    }
		};

		try {
			channel.basicConsume(queueName, 
			    false, // Should we send an ack?
			    consumer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (false);
		}

		debug("Waiting for messages. To exit press CTRL+C");

		return (true);
	}

	/**
	 * Via the all knowing StackOverflow:
	 * 
	 * We use RabbitMQ and file transfer. It's working a little slow, but for
	 * updating far away customers it is duable. I would recommend you following
	 * guidelines: Create a message structure for each block you send with
	 * command, offset and CRC32, data length, maximum 60kByte per block of data,
	 * give blocks a counter, do a sha256 at the end to make sure data is correct,
	 * make tar optional so the data can be much smaller and transmission is
	 * faster.. make a directory thing, to see which files need to update... use a
	 * broadcast event to see who's available and update 1 customer at a time with
	 * client.company.update orso, where a customer listens to .company. have fun!
	 * p.s. we also created a linux-service for this, which starts automatic at
	 * the customer.
	 * 
	 * @param aKey
	 * @param aData
	 * @return
	 * 
	 *         byte[] encoded = Base64.getEncoder().encode("Hello".getBytes());
	 *         println(new String(encoded)); // Outputs "SGVsbG8="
	 * 
	 *         byte[] decoded = Base64.getDecoder().decode(encoded); println(new
	 *         String(decoded)) // Outputs "Hello"
	 * 
	 */
	protected Boolean sendBinaryData(String aKey, byte[] aData) {

		debug("sendBinaryData (" + aData.length + "," + aData.length + ")");

		/*
		// Split the data into chunks of 1k bytes
		byte[][] chunks = QRFDataTools.divideArray(aData, 1024);

		debug("Sending " + chunks.length + " chunks ...");

		for (int i = 0; i < chunks.length; i++) {
			byte[] chunk = chunks[i];

			//byte[] encoded = Base64.getEncoder().encode(chunk);
			//String encoded = new BASE64Encoder().encode(chunk);
			//String encodedDuke = DatatypeConverter.printBase64Binary("duke".getBytes("UTF-8"));
			//String encodedChunk = (new String(encoded))
			Base64.encode (chunk);
			String encodedChunk = Base64.encode (chunk);

			debug("Sending encoded chunk: " + encodedChunk);

			Element root = new Element("msg");

			Integer encodedIndex = i;

			root.addContent(QRFMessageGenerator.createElement("chunk", encodedChunk));
			root.addContent(QRFMessageGenerator.createElement("index", encodedIndex.toString()));
		}
		*/

		return (true);
	}

	/**
	 * 
	 * @param aKey
	 * @param aFile
	 * @return
	 */
	protected Boolean sendBinaryFile(String aKey, String aFile) {

		debug("sendBinaryData (" + aFile + ")");

		/*
		byte[] aData = QRFDataTools.readFileToByteArray(aFile);

		if (aData == null) {
			return (false);
		}

		// Split the data into chunks of 1k bytes
		byte[][] chunks = QRFDataTools.divideArray(aData, 1024);

		debug("Sending " + chunks.length + " chunks ...");

		for (int i = 0; i < chunks.length; i++) {
			byte[] chunk = chunks[i];

			//byte[] encoded = Base64.getEncoder().encode(chunk);
			//String encodedChunk = (new String(encoded));

	    Base64.encode (chunk);
	    String encodedChunk = Base64.encode (chunk);

			debug("Sending encoded chunk: " + encodedChunk);

			Element root = new Element("msg");

			Integer encodedIndex = i;

			root.addContent(QRFMessageGenerator.createElement("chunk", encodedChunk));
			root.addContent(QRFMessageGenerator.createElement("index", encodedIndex.toString()));
			root.addContent(QRFMessageGenerator.createElement("file", aFile));

		}
	  */

		return (true);
	}

	/**
	 * 
	 * @param aKey
	 * @param aMessage
	 * @return
	 */
	public Boolean publish(String aKey, String aMessage) {
		debug("publish ("+aKey+")");
		//debug ("Message: " + aMessage);

		try {
			channel.basicPublish(QRFLinkData.EXCHANGE_NAME, aKey + ".*", null, aMessage.getBytes());
		} catch (Exception e) {
			debug("Error: " + e.getMessage());
			return (false);
		}

		return (true);
	}
	
  /**
   * Convenience method for quick XML sending functionality
   * @param aMessage
   */
  public void publishXML (String aMessage) {
    publish (QRFLinkData.session,aMessage);
  }	
	
	/**
	 * Convenience method for quick XML sending functionality
	 * @param aMessage
	 */
	public void publishXML(String aReceiver, String aMessage) {
		publish(aReceiver, aMessage);
	}	

	/**
	 * 
	 * @param aKey
	 * @param aMessage
	 * @return
	 */
	public Boolean broadcast(String aMessage) {
		debug ("broadcast ()");
		// debug ("Message: " + aMessage);

		try {
			channel.basicPublish(QRFLinkData.EXCHANGE_NAME, "broadcast.*", null, aMessage.getBytes());
			// debug ("Sent '" + "broadcast" + "':'" + aMessage + "'");
		} catch (Exception e) {
			debug("Error: " + e.getMessage());
			return (false);
		}

		return (true);
	}

	/**
	 * 
	 * @param clientProxy
	 */
	public void sendStateTransitionError(QRFClientProxy clientProxy) {
		debug ("Error: invalid state transition");
		debug ("Sending advice: " + clientProxy.getStateAdvice());
		
		String confMessage = QRFLinkData.generator.createAdviceMessage(clientProxy.getStateAdvice());
		
		this.publish(clientProxy.getSessionID(), confMessage);
	}	
	
	/**
	 * Generate and send a single student info message. Right now the data is hard-coded
	 * but everything else is as it should be according to the specifications.
	 * 
	 * @param aSession
	 */
	public void sendStudentInfo (String aSession) {
		debug ("sendStudentInfo ()");
		
		Element root = QRFMessageGenerator.createMainMessage("Mary","A02","?","10:40:58 AM","Bored","Confused","Change in affect");
	
		String userString=QRFMessageGenerator.XMLFragmentToString(root);
		 
		//String finalMessage = QRFLinkData.generator.createAdviceMessage (userString);
		//this.publish (aSession,finalMessage);
		this.publish (aSession,userString);
	}	
	
	/**
	 * 
	 */
	public void shutdown() {
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			connection = null;
			QRFAppLinkData.connected=false;
		}
	}
	
	/**
	 * 
	 */
	public void processDisconnect () {
	  if (QRFLinkData.currentListener!=null) {
	    QRFLinkData.currentListener.processDisconnect();
	  }
	}

	/**
	 * This is the lowest level processing code and is called almost directly by the
	 * RabbitMQ framework. So be careful not to override this.
	 * 
	 * @param envelope
	 * @param aMessage
	 */
	public void processMessage(Envelope envelope, String aMessage) {
		//debug ("(CommBase) processMessage ()");
		//debug ("Incoming (raw): " +aMessage);
		
		Document xmlDocument = messageParser.loadXMLFromString(aMessage);
		
		Element node = xmlDocument.getRootElement();

		if (node != null) {
			String foundSession = node.getChildText("session");
			if (foundSession != null) {
				if (foundSession.equals(QRFLinkData.session) == true) {
				  debug ("Detected message from ourself or our own channel, bump");
				} else {
				  //debug ("Incoming (raw): " +aMessage);
				  
					debug("Handling message with routing key " + envelope.getRoutingKey() + " to client ...");

					debug (">>>");
					
					try {
						debug(QRFXMLTools.prettyPrint(xmlDocument));
					} catch (TransformerException e) {
						debug("Message pretty-print went wrong. Who cares");
					}
					
					if (processInternal(xmlDocument) == false) {
						if (QRFLinkData.currentListener != null) {
							//debug ("Info: we have an external listener, calling appropriate method ...");
							QRFMessageParser parser = new QRFMessageParser();
							parser.loadXMLFromString(aMessage);
							QRFLinkData.currentListener.parseXML(parser);
						} else {
							debug ("Info: we don't have an external listener, calling local method ...");
							parseXML(xmlDocument);
						}
					}
					
					debug ("<<<");
				}
			}
		}
	}

	/**
	 * 
	 * @param document
	 */
	private Boolean processInternal(Document document) {
		// debug ("processInternal ()");

		Element root = document.getRootElement();

		// >----------------------------------------------------------------------------------------

		if (root.getName().equalsIgnoreCase("configuration") == true) {
			debug("Processing backchannel configuration, indicating to client(s) we're ready");

			Element listElement = root.getChild("list");

			// In case the parser distinguishes between lower and upper case
			if (listElement == null) {
				listElement = root.getChild("List");
			}

			// Since we're getting new values from the server we should discard
			// whatever we already had
			QRFLinkData.responses=new ArrayList<String> ();
						
			if (listElement != null) {
				List<Element> items = listElement.getChildren();

				for (int i = 0; i < items.size(); i++) {
					Element anItem = items.get(i);

					String itemContent = anItem.getText();

					QRFLinkData.responses.add(itemContent);
				}
			}

			return (true);
		}

		// >----------------------------------------------------------------------------------------

		return (false);
	}

	/**
	 * @param aMessage
	 * @return
	 */
	protected String createMessage(String aMessage) {
		return (wrapInXML("<qrf>" + aMessage + "</qrf>"));
	}

	/**
	 * @param aMessage
	 * @return
	 */
	protected String wrapInXML(String aMessage) {
		StringBuffer formatted = new StringBuffer();
		formatted.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
		formatted.append(aMessage);
		return (formatted.toString());
	}

	/**
	 * 
	 */
	protected void sendUsers(String aReceiver) {
		debug("sendUsers (" + aReceiver + ")");

		Element root = QRFMessageGenerator.createStudentList();

		String userString = QRFMessageGenerator.XMLFragmentToString(root);

		String finalMessage = QRFLinkData.generator.createMessage(userString);

		this.publish(aReceiver, finalMessage);
	}

	/**
	 * 
	 * @param document
	 */
	protected void parseXML(Document document) {
		//debug("parseXML (): Error: this method should have been implemented in child class");
	}

	/**
	 * 
	 * @param document
	 */
	protected void ready() {
		debug ("ready () (interna)");
		//broadcast (messageGenerator.createActionMessage("init"));
	}
}
