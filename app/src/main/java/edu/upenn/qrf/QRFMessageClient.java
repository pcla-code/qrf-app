package edu.upenn.qrf;

/** 
 * @author vvelsen
 */
public interface QRFMessageClient {
  
  /** 
   * @param aMessage
   * @return
   */
  public Boolean processData(QRFMessageParser aMessage);
  
  /** 
   * @param aMessage
   * @return
   */
  //public Boolean processMessage(String foundSession,String foundIdentifier,QRFMessageParser aMessage);

  /**
  *
  */
  public void ready ();

  /** 
   * @param parser
   */
  public void parseXML(QRFMessageParser parser);

  /**
   * 
   */
  public void processDisconnect();
}
