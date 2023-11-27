/**
 * So, the idea is, when the coder clicks "skip", a menu comes up.
 * 
 * The menu's options are
 * 
 * "Model wrong"
 * "Can't observe"
 * "Same student too recent"
 * "Old information"
 * "Other"
 * 
 */
package edu.upenn.recorder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.io.PrintWriter;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.upenn.qrf.Base64;
import edu.upenn.qrf.QRFLinkData;

/** 
 * @author vvelsen
 */
public class QRFFinish extends QRFActivityBase implements OnClickListener 
{	
  private Button uploadButton;
  private Button finishButton;
  private Button classButton;
  //private Button restartButton;
  
  private TextView endMessage;
  
  //private StringBuffer uploadInfo = new StringBuffer ();
    
  private int uploadCounter=0;
  
  private Context thisContext=null;
  
  /** 
   * Called when the activity is first created. 
   */
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    debug ("QRFFinish:onCreate ()");
    	
    super.onCreate(savedInstanceState);
                
    setContentView(R.layout.finish);
    
    finishButton=(Button) findViewById(R.id.finish_app);
    finishButton.setOnClickListener(this);
    finishButton.setEnabled(true);
    
    uploadButton=(Button) findViewById(R.id.upload_data);
    uploadButton.setOnClickListener(this);
    uploadButton.setEnabled(true);
    
    classButton=(Button) findViewById(R.id.goto_class);
    classButton.setOnClickListener(this);
    classButton.setEnabled(true);
    
    /*
    restartButton=(Button) findViewById(R.id.restart);
    restartButton.setOnClickListener(this);
    restartButton.setEnabled(true);
    */
    
    endMessage = (TextView) findViewById(R.id.endmessage);
                
    QRFAppLinkData.currentListener=this;
  }
    
  /**
   * 
   */
  public void onClick(View v) {
    Intent QRFInputIntent = null;
        
    switch(v.getId()) {
      case R.id.upload_data:
        
        if (QRFLinkData.isUploading==true) {
          uploadButton.setText("Upload");
          QRFLinkData.isUploading=false;
          return;
        } else {
          uploadButton.setText("Stop Upload");
        }
                        
        logSAI ("APP","BUTTONCLICK","R.id.upload_data","");
        
        closeLogFile ();
    
        debug ("Scanning log data directory: " + QRFAppLinkData.direct.getAbsolutePath());
  
        QRFLinkData.uploadFiles = listFiles(QRFAppLinkData.direct.getAbsolutePath());
  
        debug ("Found " + QRFLinkData.uploadFiles.size () + " files, encoding ...");

        transformToXML (QRFLinkData.uploadFiles,QRFAppLinkData.direct.getAbsolutePath());
        
        debug ("Encoded " + QRFLinkData.uploadFiles.size () + " files, sending ...");
        
        endMessage.setText("");
        
        uploadCounter=0;
        
        uploadData (null,-1);
    
      case R.id.finish_app:
        
        if (QRFLinkData.isUploading==true) {
          return;
        }
        
        logSAI ("APP","BUTTONCLICK","R.id.finish_app","");
        
        send(QRFAppLinkData.generator.createActionMessage("unregister"));
        
        finishButton.setEnabled(false);
        uploadButton.setEnabled(false);
        classButton.setEnabled(false);
        //restartButton.setEnabled(false);        
        endMessage.append("Please wait, restarting ...\n");
        
        closeLogFile ();
        
        QRFAppLinkData.inMain=false;
        
        thisContext=this;
        
        new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                  /*
                  Intent intent = new Intent(Intent.ACTION_MAIN);                  
                  intent.addCategory(Intent.CATEGORY_HOME);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                  startActivity(intent);
                  finish ();
                  */
                  
                  //View mLayout = findViewById(R.layout.finish);
                  //mLayout.setVisibility(View.GONE);                  
                  
                  Intent anIntent = new Intent(thisContext, QRFConnect.class);                  
                  startActivity(anIntent);
                  finish();
                  
                  //ExitActivity.exit(thisContext);
                }                
        }, 2000);
            
      break;
      
      case R.id.goto_class:
        logSAI ("APP","BUTTONCLICK","R.id.goto_class","");
        
        closeLogFile ();
        
        QRFAppLinkData.inMain=false;        
        
        QRFInputIntent = new Intent(this, QRFMain.class);
        startActivity(QRFInputIntent);
        finish();
      break;
      
      /*
      case R.id.restart:
        logSAI ("APP","BUTTONCLICK","R.id.restart","");
        send(QRFAppLinkData.generator.createActionMessage("unregister"));
        
        QRFInputIntent = new Intent(this, QRFConnect.class);
        QRFAppLinkData.reset ();
                
        startActivity(QRFInputIntent);
        finish();         
      break;
      */      
    }
  }
  
  /**
   * @param aPath
   * @return
   */
  public ArrayList<QRFFile> listFiles(String aPath) {
    debug ("listFiles ("+aPath+")");
    File folder = new File(aPath);
    File[] listOfFiles = folder.listFiles();
    
    ArrayList<QRFFile> results = new ArrayList<QRFFile>();

    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        QRFFile newFile=new QRFFile ();
        debug ("Adding: " + listOfFiles[i].getName());
        newFile.filename=listOfFiles[i].getName();
        results.add(newFile);
      }
    }

    return results;
  }

  /**
   * @param files
   */
  public void debugFileList(ArrayList<String> files) {
    for (int i = 0; i < files.size(); i++) {
      debug("File [" + i + "]: " + files.get(i));
    }
  }

  /**
   * @param aFile
   * @return
   */
  public byte[] fileToByte(String aFile) {
    File file = new File(aFile);

    FileInputStream fileInputStream = null;

    byte[] b = new byte[(int) file.length()];
    try {
      fileInputStream = new FileInputStream(file);
      fileInputStream.read(b);
      /*
       * for (int i = 0; i < b.length; i++) { System.out.print((char) b[i]); }
       */
    } catch (FileNotFoundException e) {
      debug("File Not Found.");
      // e.printStackTrace();
      return (null);
    } catch (IOException e1) {
      System.out.println("Error Reading The File.");
      e1.printStackTrace();
      return (null);
    }

    try {
      fileInputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
      return (null);
    }

    return b;
  }

  /**
   * @param string
   * @return
   */
  public ArrayList<String> encodeFile(QRFFile aFile,String aFileName) {
    debug ("encodeFile ("+aFile.filename+","+aFileName+")");

    ArrayList<String> parts = new ArrayList<String>();

    byte[] rawData = fileToByte(aFileName);

    if (rawData == null) {
      return null;
    }

    int blockSize = 1024;

    int count = 0;
    //int index=0;

    int pieces = rawData.length / blockSize;
    int partial = rawData.length % blockSize;
    
    debug("Encoding file of raw length: " + rawData.length + " into " + pieces + " pieces and " + partial + " bytes");
          
    for (int i=0;i<pieces;i++) {
      byte[] block = new byte[blockSize];
      
      System.arraycopy(rawData, count, block, 0, blockSize);
      
      String encoded = Base64.encodeToString(block, false);
      
      parts.add(encoded);

      count+=blockSize;
    }
    
    if (partial>0) {
      byte[] block = new byte[partial];
      
      System.arraycopy(rawData, count, block, 0, partial);
      
      String encoded = Base64.encodeToString(block, false);
      
      parts.add(encoded);
    }
    
    debug ("Created " + parts.size() + " parts for this file");
    
    for (int j=0;j<parts.size();j++) {
      QRFFilePart aPart=new QRFFilePart ();
      aPart.data=parts.get(j);
      aFile.fileParts.add(aPart);
    }

    return parts;
  } 
  
  /**
   * @param parts
   * @param aFile
   * @return
   */
  public void partsToXML (ArrayList<QRFFilePart> parts,String aFile) {
    debug ("partsToXML ("+aFile+")");

    for (int i=0;i<parts.size();i++) {
      StringBuffer formatted=new StringBuffer ();
      formatted.append("<file part=\""+(i+1)+"\" of=\""+parts.size()+"\">");
      formatted.append("<name>"+aFile+"</name>");
      formatted.append("<data><![CDATA[");
      formatted.append(parts.get(i).data);
      formatted.append("]]></data>");
      formatted.append("</file>\n");
      parts.get(i).dataEncoded=formatted.toString ();
    }    
  } 
  
  /**
   * @param files
   */
  public void transformToXML(ArrayList<QRFFile> files, String aBasePath) {
    debug ("transformToXML ()");

    for (int i = 0; i < files.size(); i++) {
      QRFFile aFile=files.get(i);
      encodeFile(aFile,aBasePath + "//" + files.get(i).filename);
    }
  }
  
  /**
   * 
   */
  private void uploadData (QRFFile aFile, Integer aPart) {
    debug ("uploadData ()");

    Boolean newUpload=false;

    // No file provided, just grab the very first in the list since we 
    // assume we start from scratch
    if (aFile==null) {
      debug ("No ack file provided, probably a fres upload");
      newUpload=true;
    } else {
      // Find the current state and see if we need to upload something next
      if (aFile.markUploaded (aPart)==true) {
        QRFFilePart newPart=aFile.getNextPart ();

        if (newPart!=null) {
          send (QRFLinkData.generator.createFilepartMessage(newPart.dataEncoded)); 
        } else {
          debug ("File uploaded, asking for new upload");
          newUpload=true;
        }
      } else {
        debug ("Internal conflict!");
        QRFLinkData.isUploading=false;
        return;
      }
    }

    if (newUpload==true) {
      debug ("newUpload is true, finding new file to upload ...");
      
      //uploadButton.setEnabled(false);
      
      uploadCounter++;
      
      //endMessage.append("Uploading file " + (uploadCounter+1)+"\n");      
      
      QRFFile uploadFile=findNewUpload ();
      if (uploadFile!=null) {
        if (uploadFile.encoded==false) {
          partsToXML (uploadFile.fileParts,uploadFile.filename);
          uploadFile.encoded=true;
        }
  
        QRFFilePart startPart=uploadFile.getNextPart ();
  
        if (startPart!=null) {
          if (startPart.dataEncoded!=null) {
            send (QRFLinkData.generator.createFilepartMessage(startPart.dataEncoded));
          } else {
            debug ("All done, or file encoding error");
            QRFLinkData.isUploading=false;
            endMessage.append("All done, or file encoding error\n");
            return;
          }
        } else {
          debug ("File uploaded or ");
          QRFLinkData.isUploading=false;
          endMessage.append("All done!\n");
          return;
        }
  
        QRFLinkData.isUploading=true;    
      } else {
        debug ("All files uploaded");
        QRFLinkData.isUploading=false;
        endMessage.append("All done!\n");
        //uploadButton.setEnabled(true);
      }
    }
  }

  /**
   *
   */
  private QRFFile findNewUpload () {
    debug ("findNewUpload ()");

    for (int i = 0; i < QRFLinkData.uploadFiles.size(); i++) {
      QRFFile aFile=QRFLinkData.uploadFiles.get(i);
      if (aFile.allUploaded ()==false) {
        return (aFile);
      }
    }

    return (null);
  }
  
  /**
   * @param part
   * @param of
   * @param aName
   */
  protected void processAck(String part, String of, String aName) {
    debug ("processAck ("+part+","+of+","+aName+")");
    
    endMessage.append("Sucessfully part " + part + " of " + of + " for file " + uploadCounter + " out of " + QRFLinkData.uploadFiles.size() + "\n");
    
    if (QRFLinkData.isUploading==false) {    
      endMessage.append("Upload interrupted\n");      
      return;
    }
    
    Integer index=Integer.parseInt(part);
    
    QRFFile targetFile=findFile (aName);
    
    if (targetFile==null) {
      debug ("Internal error, can't find file: " + aName);
      return;
    }

    uploadData (targetFile,index);
  }
  
  /**
   * 
   */
  private QRFFile findFile (String aName) {
    debug ("findFile ("+aName+")");

    String filenameCleaned=aName.replace("\n", "").replace("\r", "").trim();
    
    for (int i=0;i<QRFLinkData.uploadFiles.size();i++) {
      QRFFile testFile=QRFLinkData.uploadFiles.get(i);
      debug ("Comparing " + filenameCleaned + ", to: " + testFile.filename);
      //if (testFile.filename.equalsIgnoreCase(aName)==true) {
      if (testFile.filename.equals(filenameCleaned)==true) {
        return (testFile);
      }
    }
    return (null);
  }
}

