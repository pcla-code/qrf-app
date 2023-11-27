package edu.upenn.qrf;

/** 
 * @author vvelsen
 */
public interface QRFMessageProcessor {
  
  /** 
   * @param aMessage
   * @return
   */
  public Boolean processMessage (QRFMessageParser aMessage);
  
}
