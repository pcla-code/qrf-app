package edu.upenn.qrf;

import java.util.ArrayList;
//import java.util.UUID;

//import com.rabbitmq.client.Consumer;

import edu.upenn.qrf.comm.QRFCommBase;
//import edu.upenn.recorder.QRFAppLinkData;
import edu.upenn.recorder.QRFFile;

/**
 * @author Martin van Velsen <vvelsen@knossys.com>
 */
public class QRFLinkData
{	
  public static String EXCHANGE_NAME = "QRF";
  
  public static QRFMessageGenerator generator=null;
  public static QRFMessageParser parser=null;
  public static String session="-1";
  
  public static ArrayList<String> responses=null;
  
  public static QRFCommBase commManager = null;
  
  public static QRFMessageClient currentListener = null;
  
  public static Boolean isUploading = false;  
  public static ArrayList<QRFFile> uploadFiles = null;
  
  public static Boolean inMain=false;
}
