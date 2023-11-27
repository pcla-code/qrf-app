package edu.upenn.recorder;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

/** 
 * @author vvelsen
 */
public class QRFRejectionDialog extends Dialog implements android.view.View.OnClickListener {

  public Activity c;
  public Dialog d;
  public Button ok;
  public Spinner justificationSpinner;
  
  private QRFDialogNotifier notificationObject = null;
  
  /** 
   * @param a
   */
  public QRFRejectionDialog(Activity a) {
   super(a);
   this.c = a;
  }
  
  /** 
   * @param aNotifier
   */
  public void setQRFDialogNotifier (QRFDialogNotifier aNotifier) {
    notificationObject = aNotifier;
  }

  /**
   * 
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.inputdialog);
    ok = (Button) findViewById(R.id.accept_reason_button);
    justificationSpinner = (Spinner) findViewById(R.id.justification);    
    ok.setOnClickListener(this);
    
    List<String> affects = new ArrayList<String>(); 
    
    affects.add("Select a Rejection");
    
    for (int i=0;i<QRFAppLinkData.responses.size();i++) {     
      affects.add(QRFAppLinkData.responses.get(i));
    }
    
    ArrayAdapter <String>affectAdapter = new ArrayAdapter<String>(c,android.R.layout.simple_spinner_dropdown_item, affects);
    justificationSpinner.setAdapter(affectAdapter);    
  }

  /**
   * 
   */
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.accept_reason_button:
      String reason = (String) justificationSpinner.getSelectedItem();      
      
      if (reason.equalsIgnoreCase("Select a Rejection")==true) {
        showWarning ("Please make a selection first");        
      } else {
        QRFAppLinkData.justification=reason;
      
        if (notificationObject!=null) {
          notificationObject.processDialogInput ();
        }
        
        dismiss();
      }
      break;
    default:
      break;
    }
  }
  
  /**
   * 
   */
  private void showWarning (String aMessage) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle("QRF Info");
    alertDialog.setMessage(aMessage);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
        new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    alertDialog.show();      
  }
}
