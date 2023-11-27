package edu.upenn.recorder;

//import org.slf4j.Logger;

import android.app.Activity;
import android.util.Log;

/**
 * 
 */
public class QRFBase 
{
	public static Activity currentActivity=null;
  //public static Logger logOutput = null;
  
  public static String TAG="QRF";
  public static int lineCounter=0;
  
  public static Boolean useInternalLogging=false;
  
  public static Boolean isOnAndroid=true;
	
	/**
	 * 
	 */
	protected void debug (String aMessage)
	{
		Log.d(TAG,"["+QRFBase.lineCounter + "] "+ aMessage+"\n");
		
		QRFBase.lineCounter++;
	}
	/**
	 * 
	 */
	public static void extDebug (String aMessage)
	{
		Log.d(TAG,"["+QRFBase.lineCounter + "] "+ aMessage+"\n");
		
		QRFBase.lineCounter++;
	}
	
	/**
	 * 
	 * @param string
	 */
  public static void staticdebug(String string) {
    
    Log.d(TAG,"["+QRFBase.lineCounter + "] "+ string+"\n");
    
    QRFBase.lineCounter++;    
  }   
  
	/**
	 * 
	 */
	protected void error (String aMessage)
	{
		new QRFConfirmDisplay(aMessage, QRFBase.currentActivity, null, false);
	}
	/**
	 * 
	 */
	public static void extError (String aMessage)
	{
		new QRFConfirmDisplay(aMessage, QRFBase.currentActivity, null, false);
	}
}	