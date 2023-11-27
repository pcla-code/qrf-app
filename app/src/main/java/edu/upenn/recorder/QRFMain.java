package edu.upenn.recorder;

//import java.io.File;
//import java.io.IOException;

import android.content.Intent;
//import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
//import edu.upenn.qrf.QRFMessageParser;

/** 
 * @author vvelsen
 */
public class QRFMain extends QRFActivityBase implements OnClickListener 
{  
  private Button okButton;
  
  /** 
   * Called when the activity is first created. 
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {    	
    super.onCreate(savedInstanceState);
    
    debug("QRFMain:onCreate ()");
                
    setContentView(R.layout.configure);
    
    okButton=(Button) findViewById(R.id.ok_button);
    okButton.setOnClickListener(this);     
    
    QRFAppLinkData.currentListener=this;
  }
    
  /**
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  public void onClick(View v) { 	   
    switch(v.getId()) {
      case R.id.ok_button:
        EditText classnameInput = (EditText) findViewById(R.id.classname);
        EditText observerInput = (EditText) findViewById(R.id.observername);
        
        if (classnameInput.getText()==null) {
          showAlert ("Please enter a class name");
          return;
        }
        
        if (observerInput.getText()==null) {
          showAlert ("Please enter an observer name");
          return;
        }
        
        String classname=classnameInput.getText().toString();
        QRFAppLinkData.observerName=observerInput.getText().toString();
        
        if (classname.matches("")) {
          showAlert ("Please enter a class name");
          return;
        }
        
        if (QRFAppLinkData.observerName.matches("")) {
          showAlert ("Please enter an observer name");
          return;
        }
        
        debug ("Using observer name: " + QRFAppLinkData.observerName);

        // Now we're ready for logging;
        QRFAppLinkData.inMain=true;
        
        checkLogFile (); 
                
        send (QRFAppLinkData.generator.createClassnameMessage (classname));
        
        classnameInput.setText("");
        observerInput.setText("");
        
        Intent inputIntent = new Intent(this, QRFInput.class);
        startActivity(inputIntent);
              
      break;
    }
  } 
  
  /**
   * This should never be called anymore since we're not processing
   * a list of users. Let's keep it for now so that we can put it
   * back in if we need it.
   */
  protected void displayUserList() {
    debug ("displayUserList ()");
    
    Intent QRFStudentsIntent = new Intent(this, QRFStudents.class);
    startActivity(QRFStudentsIntent);
    finish();   
  }      
}
