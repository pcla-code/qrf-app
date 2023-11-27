package edu.upenn.recorder;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.TransformerException;

import org.jdom2.Document;
import org.jdom2.Element;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import edu.upenn.qrf.QRFMessageClient;
import edu.upenn.qrf.QRFMessageGenerator;
import edu.upenn.qrf.QRFMessageParser;
import edu.upenn.qrf.tools.QRFXMLTools;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * 
 */
public class QRFActivityBase extends Activity implements QRFMessageClient {
  
  protected class CachedEntry {
    public String behavior = "nop";
    public String affect = "nop";
    public String intervention = "nop";
    public String notes = "";
    public String studentID = "";
    public Integer studentIndex = 1;
  }

  protected QRFMessageGenerator messageGenerator = new QRFMessageGenerator();

  protected String postHost = "";

  protected StringBuffer notes = new StringBuffer();

  protected int cacheIndex = 0;
  protected ArrayList<CachedEntry> entries = new ArrayList<CachedEntry>();

  private Boolean useBroker = true;

  // As per:
  // https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
  protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss.S");

  /**
   * 
   */
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    QRFBase.currentActivity = this;
    QRFAppLinkData.currentListener = this;
  }

  /**
   * 
   */
  public void onStart() {
    super.onStart();
    debug("onStart ()");
  }

  /**
   * 
   */
  public void onPause() {
    super.onPause();
    debug("onPause ()");
  }

  /**
   * 
   */
  public void onResume() {
    super.onResume();
    debug("onResume ()");
  }

  /**
   * Because we disabled the history stack in the manifest, we shouldn't allow
   * users to go back because it would exit the app.
   */
  @Override
  public void onBackPressed() {
    // super.onBackPressed();
  }

  /**
   *
   */
  protected void generateSession() {
    UUID rawSession = UUID.randomUUID();
    QRFAppLinkData.session = String.valueOf(rawSession);
  }

  /**
   * 
   * @param aTest
   * @param aSelection
   * @return
   */
  protected int getIndexFromList(List<String> aTest, String aSelection) {
    for (int i = 0; i < aTest.size(); i++) {
      if (aTest.get(i).equals(aSelection) == true) {
        return (i);
      }
    }

    return (-1);
  }

  /**
   * 
   */
  protected void debug(String aMessage) {
    QRFBase.extDebug(aMessage);
  }

  /**
   * 
   */
  protected void error(String aMessage) {
    QRFBase.extError(aMessage);
  }

  /**
   * 
   * @param aURL
   */
  protected void setPostHost(String aURL) {
    postHost = aURL;
  }

  /**
   * 
   * @param aMessage
   */
  protected String send(String aMessage) {
    debug("send ()");

    logSAI("APP", "SEND", "XML", aMessage);

    if (useBroker == true) {
      return (sendToBroker(aMessage));
    }

    return (executePost(aMessage));
  }

  /**
   * @param targetURL
   * @param urlParameters
   * @return
   */
  protected String executePost(String urlParameters) {
    HttpURLConnection connection = null;

    try {
      // Create connection
      URL url = new URL(this.postHost);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

      connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));
      connection.setRequestProperty("Content-Language", "en-US");

      connection.setUseCaches(false);
      connection.setDoOutput(true);

      // Send request
      DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
      wr.writeBytes(urlParameters);
      wr.close();

      debug("Data sent, waiting for input ...");

      // Get Response
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
      String line;

      while ((line = rd.readLine()) != null) {
        response.append(line);
      }

      rd.close();

      debug("We have data: " + response.toString());

      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  /**
   * 
   */
  protected void shutdownBrokerLink() {
    QRFAppLinkData.commManager.shutdown();
  }

  /**
   * 
   * @param aMessage
   */
  protected String sendToBroker(String aMessage) {
    debug("sendToBroker ()");

    // QRFAppLinkData.commManager.publish ("QRF",aMessage);
    QRFAppLinkData.commManager.publishXML(aMessage);

    return ("OK");
  }

  /**
   * 
   * https://medium.com/@yossisegev/understanding-activity-runonuithread-e102d388fe93
   * https://stackoverflow.com/questions/11140285/how-do-we-use-runonuithread-in-android
   */
  public void parseXML(QRFMessageParser msg) {
    debug("parseXML ()");

    final QRFMessageParser incomingMsg = msg;

    debug("We have a proper message, attempting to push it into the UI thread ...");

    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        debug("run ()");
        parseXMLInUIThread(incomingMsg);
      }
    });
  }

  /**
   * 
   */
  public void parseXMLInUIThread(QRFMessageParser aMessage) {
    debug("parseXMLInUIThread ()");

    logSAI("SERVER", "SEND", "XML", aMessage.getOriginal());

    Document document = aMessage.getDocument();

    try {
      debug(QRFXMLTools.prettyPrint(document));
    } catch (TransformerException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
    }

    Boolean processed = false;

    Element node = document.getRootElement();

    if (node != null) {
      // String foundSession = node.getChildText ("session");
      String foundAction = node.getChildText("action");
      String foundYesNo = node.getChildText("string");
      String foundAdvice = node.getChildText("advice");
      String foundAckPart = node.getAttributeValue("part");

      String locationCheck = node.getChildText("Location");

      // Message detectors
      Element foundList = node.getChild("List");

      String was = node.getChildText("was");
      String is = node.getChildText("is");

      String emotion = node.getChildText("emotion-pattern");
      String behavior = node.getChildText("behavior-pattern");

      if (emotion != null) {
        was = emotion;
      }

      if (behavior != null) {
        is = behavior;
      }

      // >-----------------------------------------------------------------------------

      if (foundAdvice != null) {
        debug("Received a warning from the broker: " + foundAdvice);
        processed = true;
      }

      // >-----------------------------------------------------------------------------

      if (foundYesNo != null) {
        if (foundYesNo.equalsIgnoreCase("yes") == true) {
          debug("The server indicated that the class we want to register for exists. Navigating to student list ...");
        }
        processed = true;
      }

      // >-----------------------------------------------------------------------------

      if (foundAction != null) {
        if (foundAction.equalsIgnoreCase("broadcast") == true) {
          // debug ("No need to do anything, everyone should already have received this
          // message");
          processed = true;
        }

        if (foundAction.equalsIgnoreCase("pong") == true) {
          debug("Success! Pong received!");
          processed = true;
        }
      }

      // >-----------------------------------------------------------------------------

      if (foundAdvice != null) {
        debug("Received a warning from the broker: " + foundAdvice);
        processed = true;
      }

      // >-----------------------------------------------------------------------------

      if (foundList != null) {
        debug("Processing user list ...");
        // ArrayList<QRFUser> userList=aMessage.parseUserList (foundList);
        QRFAppLinkData.userList = aMessage.parseUserList(foundList);
        displayUserList();
        processed = true;
      }

      if (locationCheck != null) {
        if (locationCheck.equalsIgnoreCase("EMPTY_QUEUE") == true) {
          disableInterface();
          processed = true;
        }
      }

      if (foundAckPart != null) {
        processAck(node.getAttributeValue("part"), node.getAttributeValue("of"), node.getChildText("name"));
        processed = true;
      }

      // >-----------------------------------------------------------------------------

      if ((was != null) && (is != null) && (processed == false)) {
        debug("Received affect update, processing ...");

        QRFAppLinkData.observationIndex++;

        QRFAppLinkData.currentInfo = new QRFStudentInfo();
        QRFAppLinkData.currentInfo.studentName = node.getChildText("name");
        QRFAppLinkData.currentInfo.studentID = node.getChildText("ID");
        QRFAppLinkData.currentInfo.studentTime = node.getChildText("time_when_occurred");
        QRFAppLinkData.currentInfo.studentOccurrenceType = node.getChildText("occurrence_type");
        QRFAppLinkData.currentInfo.studentLocation = node.getChildText("Location");
        QRFAppLinkData.currentInfo.observation = QRFAppLinkData.observationIndex;

        QRFAppLinkData.currentInfo.emotionPattern = was;
        QRFAppLinkData.currentInfo.behaviorPattern = is;

        debug("Displaying user info (for observation: " + QRFAppLinkData.observationIndex + "): "
            + QRFAppLinkData.currentInfo.studentID + ",\n " + QRFAppLinkData.currentInfo.studentTime + ",\n "
            + QRFAppLinkData.currentInfo.studentOccurrenceType + ",\n emotion pattern:"
            + QRFAppLinkData.currentInfo.emotionPattern + ",\n emotion pattern:"
            + QRFAppLinkData.currentInfo.behaviorPattern);

        processObservation();

        processed = true;
      }

      // >-----------------------------------------------------------------------------

    } else {
      debug("Error: no msg element found!");
    }

    if (processed == false) {
      debug("Error: no appropriate action found in client message");
    }
  }

  /**
   * 
   */
  protected void disableInterface() {
    debug("disableInterface ()");

    debug("Error: this should be overloaded and processed in a child class");
  }

  /**
   * 
   */
  protected void processObservation() {
    debug("processObservation ()");

    debug("Error: this should be overloaded and processed in a child class");
  }

  /**
   * 
   */
  protected void displayUserList() {
    debug("displayUserList ()");

    debug("WRONG METHOD, OVERRIDE THIS!");
  }

  /**
   * 
   */
  protected void checkLogFile() {
    debug("checkLogFile (" + QRFAppLinkData.observerName + ")");

    // closeLogFile ();

    if (QRFAppLinkData.logManager != null) {
      return;
    }

    QRFAppLinkData.logManager = new QRFFileManager(
        QRFAppLinkData.observerName + "." + QRFAppLinkData.session + "." + QRFAppLinkData.runCount + ".csv.log");

    if (QRFAppLinkData.logManager == null) {
      debug("Internal error: no log manager available!");
      return;
    }

    logHeader();
  }

  /**
   * 
   */
  protected void closeLogFile() {
    debug("checkLogFile ()");

    if (QRFAppLinkData.logManager != null) {
      QRFAppLinkData.logManager.close();
      QRFAppLinkData.logManager = null;
    }
  }

  /*
   * 1) Even though the file seems to be in a csv format, excel had issue opening
   * it on my end. This might be due to the xml content within the csv file.
   * Especially, I've noticed that there appear to be line breaks sometimes within
   * some of the filed. I've noticed quite a few of them after the </msg> tag (but
   * before the following comma). Do you know if there is a way to remove those?
   * 
   * 2) It seems like the "time_when_occurred" field, that will help us identify
   * at what exact time the transition occurred as a second level precision. Do
   * you know if it would be possible to have precision up to milliseconds?
   * 
   * 3) The message format has 2 component: "was" and "is". Would that format
   * still work if the transition involves more than 2 components? We have a few
   * sequences that are more than 2 for affect, and I think the Vanderbilt team
   * also has patters that include more than 2 actions.
   * 
   * 4) My understanding is that the notes that observers enter in the text box
   * are included in the "supplement" column for "R.id.next_button" events. Is
   * that the case? I'm also wondering if it would make sense to put that text
   * between quotes. Mostly, because, if an observer includes a comma in their
   * notes, that might mess with the csv format.
   * 
   * 5) I see a "class_name" field in the data, but haven't seen any field for the
   * observer's name. Would it be possible to also have that field?
   */
  protected void logHeader() {
    debug("logHeader ()");

    if (QRFAppLinkData.logManager != null) {
      QRFAppLinkData.logManager.writeLine(
          "Observer,TimestampRaw,Timestamp,Selection,Action,Input,Supplement1,Supplement2,Supplement3,Supplement4,Supplement5,Supplement6,Supplement7,Supplement8");

      QRFAppLinkData.runCount++;
    }
  }

  /**
  *
  */
  protected void logSAI(String aSelection, String anAction, String anInput, String aSupplement) {
    debug("logSAI ()");

    if (QRFAppLinkData.isUploading == true) {
      debug("Not logging upload data SAI to disk");
      return;
    }

    if (QRFAppLinkData.inMain == false) {
      debug("Not in main part of application yet");
      return;
    }

    // Make sure we have something to log to!
    checkLogFile();

    if (QRFAppLinkData.logManager == null) {
      debug("Internal error: no log manager available!");
      return;
    }

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    Long raw = timestamp.getTime();

    String inputSafe = "";

    String supplement = "";

    if (anInput != null) {
      inputSafe = anInput;
    }

    if ((anInput.indexOf("<xml") != -1) || (anInput.indexOf("<XML") != -1)) {
      debug("Fixing XML ...");
      inputSafe = anInput.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
    }

    if (aSupplement != null) {
      supplement = aSupplement;
    }

    String supplementSafe = supplement.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");

    QRFAppLinkData.logManager.writeLine(QRFAppLinkData.observerName + "," + raw + "," + sdf.format(timestamp) + ","
        + aSelection + "," + anAction + "," + inputSafe + "," + supplementSafe);
  }

  /**
   * 
   */
  @Override
  public void ready() {
    debug("ready ()");

    debug("Compilation Error: this method needs to be overridden");
  }

  /**
   * 
   */
  public void showAlert(String aMessage) {
    debug("showAlert (" + aMessage + ")");

    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    alertDialog.setTitle("QRF Info");
    alertDialog.setMessage(aMessage);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    alertDialog.show();
  }

  /**
   *
   */
  @Override
  public Boolean processData(QRFMessageParser aMessage) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @param attribute
   * @param attribute2
   * @param childText
   */
  protected void processAck(String part, String of, String name) {
    debug("processAck ()");

    debug("Compilation Error: this method needs to be overridden");
  }

  /**
   * 
   */
  @Override
  public void processDisconnect() {
    debug("processDisconnect ()");
    QRFAppLinkData.connected=false;    
  }
}
