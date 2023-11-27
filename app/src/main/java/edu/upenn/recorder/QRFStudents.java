package edu.upenn.recorder;

import java.util.ArrayList;

//import java.util.ArrayList;
//import java.util.List;

//import java.io.File;
//import java.io.IOException;

import android.content.Intent;
//import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
//import android.widget.ArrayAdapter;
import android.widget.Button;
//import android.widget.EditText;
import android.widget.ListView;
import edu.upenn.qrf.QRFUser;
//import edu.upenn.qrf.QRFMessageParser;
//import edu.upenn.qrf.QRFUser;

/** 
 * @author vvelsen
 */
public class QRFStudents extends QRFActivityBase implements OnClickListener 
{  
  private Button okButton;
  private ListView studentlist;
  
  /** 
   * Called when the activity is first created. 
   */
  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    debug ("QRFStudents:onCreate ()");
    	
    super.onCreate(savedInstanceState);
                
    setContentView(R.layout.students);
    
    okButton=(Button) findViewById(R.id.ok_button);
    okButton.setOnClickListener(this);     
    
    studentlist=(ListView) findViewById(R.id.studentlist);
    
    QRFAppLinkData.currentListener=this;
    
    ArrayList<String> userArray = new ArrayList<String>();
    
    // Populate the user list ...
    for (int i=0;i<QRFAppLinkData.userList.size();i++) {
      QRFUser aUser=QRFAppLinkData.userList.get(i);
      userArray.add(aUser.id + " - " + aUser.name);
    }
    
    final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, userArray);   
    
    studentlist.setAdapter(arrayAdapter);
  }
    
  /**
   * @see android.view.View.OnClickListener#onClick(android.view.View)
   */
  public void onClick(View v) { 	   
    switch(v.getId()) {
      case R.id.ok_button:
        logSAI ("APP","BUTTONCLICK","R.id.ok_button","");
        Intent inputIntent = new Intent(this, QRFInput.class);
        startActivity(inputIntent);
        finish();
      break;
    }
  }
}
