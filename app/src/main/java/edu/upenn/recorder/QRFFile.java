package edu.upenn.recorder;

import java.util.ArrayList;

/**
 * @author vvelsen
 */
public class QRFFile extends QRFBase {
  
  public String filename="";
  public Boolean encoded=false;
  public ArrayList<QRFFilePart> fileParts=new ArrayList<QRFFilePart> ();

  /**
   * @return
   */
  public Boolean allUploaded () {
    for (int i=0;i<fileParts.size();i++) {
      QRFFilePart aPart=fileParts.get(i);
      if (aPart.uploaded==false) {
        return (false);
      }
    }
    return(true);
  }

  /**
   *
   */
  public Boolean markUploaded (Integer anIndex) {
    anIndex--; // To make it 0 based
    
    if (anIndex<0) {
      debug ("Index is out of range: " + anIndex);
      return (false);
    }
    
    debug ("markUploaded ("+anIndex+")");
    
    if (anIndex>=fileParts.size ()) {
      debug ("Index is out of range: " + anIndex + " >= " + fileParts.size());
      return (false);
    }

    fileParts.get (anIndex).uploaded=true;

    return (true);
  }

  /**
   *
   */
  public QRFFilePart getNextPart () {
     
    for (int i=0;i<fileParts.size();i++) {
      QRFFilePart aPart=fileParts.get(i);
      if (aPart.uploaded==false) {
        return (aPart);
      }
    }

    return (null);
  }
}
