package edu.upenn.qrf;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import edu.upenn.recorder.QRFBase;

/** 
 * 
 */
public class QRFMessageParser extends QRFBase {
  
  private Document document=null;
  private String original="";
  private ArrayList<QRFUser> userList=null;
  
  /**
   * @return
   */
  public ArrayList<QRFUser> getUserList () {
    return (userList);
  }
  
  /**
   * 
   * @param xml
   * @return
   * @throws Exception
   */
  public Document loadXMLFromString(String xml)
  { 
    debug ("loadXMLFromString ()");
    
    Document doc = null;
    original = xml;
    
    SAXBuilder sxBuild = new SAXBuilder();
    
    try {
      doc = sxBuild.build(new StringReader(xml));
    } catch (JDOMException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    document=doc;
    
    /*
    Boolean processed=false;
    
    Element node = document.getRootElement();

    if (node!=null) {
      String foundSession = node.getChildText ("session");
      String foundAction = node.getChildText ("action");
      
      // Message detectors 
      Element foundList = node.getChild ("List");
      Element was = node.getChild ("was");
      Element is = node.getChild ("is");
      
      if (foundAction!=null) {
        if (foundAction.equalsIgnoreCase("broadcast")==true ) {
          //debug ("No need to do anything, everyone should already have received this message");
          processed=true;
        }
          
        if (foundAction.equalsIgnoreCase("register")==true ) {
          //debug ("No need to do anything, the base code takes care of automatic registration");
          processed=true;
        }
        
        if (foundAction.equalsIgnoreCase("pong")==true ) {
          debug ("Success! Pong received!");
          processed=true;
        } 
      }   
      
      if (foundList!=null) {
        debug ("Processing user list ...");
        userList=parseUserList (foundList);
        processed=true;
      }
      
      if ((was!=null) && (is!=null)) {
        debug ("Received affect update, processing ...");
        processed=true;
      }
      
    } else {
      debug ("Error: no msg element found!");
    }
    
    if (processed==false) {
      debug ("Error: no appropriate action found in client message");
    } 
    */     
        
    return (document);
  }

  /**
   * 
   * @return
   */
  public Document getDocument() {
    return document;
  }

  public String getOriginal () {
    return (original);
  }
  
  /**
   * 
   * @param document
   */
  public void setDocument(Document document) {
    this.document = document;
  }
  
  /**
   * 
   */
  public ArrayList<QRFUser> parseUserList (Element foundList) {
    debug ("parseUserList ("+foundList.getName()+")");
    
    ArrayList<QRFUser> newList=new ArrayList<QRFUser> ();
    
    List<Element> items=foundList.getChildren();
    
    for (int i=0;i<items.size();i++) {
      Element listElement=items.get(i);
      
      if (listElement.getName().equalsIgnoreCase("item")==true) {
        QRFUser newUser=new QRFUser ();
        newUser.name=listElement.getChildText("Name");
        newUser.id=listElement.getChildText("ID");
        
        newList.add(newUser);
      }
    }
    
    return (newList);
  }
}
