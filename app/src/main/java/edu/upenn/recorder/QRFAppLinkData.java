package edu.upenn.recorder;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import com.rabbitmq.client.Consumer;

import edu.upenn.qrf.QRFLinkData;
import edu.upenn.qrf.QRFUser;

/** 
 * @author vvelsen
 */
public class QRFAppLinkData extends QRFLinkData {
  
  public static String version="0.16.0";
  
  public static String QUEUE_NAME = "QRF"; 
  public Boolean queueDeclared = false;  
  public static Consumer consumer = null;
  
  public static QRFFileManager logManager = null;    
  
  public static File direct = null;
    
  public static ArrayList<QRFUser> userList=null;
  
  public static int observationIndex = 0;
  public static String observerName="Unassigned";  
  
  public static QRFStudentInfo currentInfo=null;
  
  public static String justification="";
  
  public static int runCount=0;
  
  public static Boolean connected=false;
  
  /**
   * 
   */
  public static void reset () {
    QRFAppLinkData.observationIndex = 0;
    
    UUID rawSession = UUID.randomUUID();
    QRFAppLinkData.session = String.valueOf(rawSession);    
  }  
}
