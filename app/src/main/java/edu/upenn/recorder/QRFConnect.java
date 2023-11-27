package edu.upenn.recorder;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.UUID;

import edu.upenn.qrf.QRFMessageGenerator;
import edu.upenn.qrf.QRFMessageParser;
import edu.upenn.qrf.comm.QRFCommBase;
import edu.upenn.recorder.R;

/**
 * 
 */
public class QRFConnect extends QRFActivityBase implements OnClickListener {
  public QRFApplicationData appData = null;
  public QRFAppLinkData linkData = null;

  private TextView versionLabel = null;
  private TextView netstat = null;
  private EditText hostnameInput = null;
  private EditText usernameInput = null;
  private EditText passwordInput = null;
  private StringBuffer status = new StringBuffer();

  private Button okButton;
  private Button checkButton;
  
  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    debug("QRF:onCreate ()");

    linkData = new QRFAppLinkData();
    QRFAppLinkData.inMain=false;

    QRFAppLinkData.parser = new QRFMessageParser();

    QRFAppLinkData.generator = new QRFMessageGenerator();

    UUID rawSession = UUID.randomUUID();
    QRFAppLinkData.session = String.valueOf(rawSession);

    setContentView(R.layout.start);

    checkButton = (Button) findViewById(R.id.check);
    checkButton.setOnClickListener(this);

    okButton = (Button) findViewById(R.id.start_app);
    okButton.setOnClickListener(this);

    versionLabel = (TextView) findViewById(R.id.version);
    netstat = (TextView) findViewById(R.id.netstat);
    hostnameInput = (EditText) findViewById(R.id.qrf_host);
    usernameInput = (EditText) findViewById(R.id.qrf_username);
    passwordInput = (EditText) findViewById(R.id.qrf_password);

    appData = (QRFApplicationData) this.getApplication();

    versionLabel.setText("Version: " + QRFAppLinkData.version);
    
    if (QRFAppLinkData.connected==false) {
      checkButton.setEnabled(true);
      okButton.setEnabled(false);
      
      hostnameInput.setEnabled(true);
      usernameInput.setEnabled(true);
      passwordInput.setEnabled(true);   
    } else {
      checkButton.setEnabled(false);
      okButton.setEnabled(true);
      
      hostnameInput.setEnabled(false);
      usernameInput.setEnabled(false);
      passwordInput.setEnabled(false);         
    }
  }

  /**
   * 
   */
  private void updateStatus(String aMessage) {
    debug(aMessage);

    status.append(aMessage);
    status.append("\n");

    if (netstat != null) {
      netstat.setText(status.toString());
    }
  }

  /**
   * 
   */
  @Override
  public void onClick(View arg) {
    switch (arg.getId()) {
    case R.id.check:
      
      if (QRFAppLinkData.connected==false) {
        checkButton.setText("Disconnect");
        updateStatus("Initializing ...");
        systemCheck();
      } else {
        checkButton.setText("Connect");
        QRFAppLinkData.commManager.shutdown();
        okButton.setEnabled(false);
      }
      break;
    case R.id.start_app:
      Intent QRFMainIntent = new Intent(this, QRFMain.class);
      startActivity(QRFMainIntent);
      finish();
      break;
    }
  }

  /**
   * 
   */
  protected Boolean systemCheck() {
    debug("systemCheck ()");
    
    // >-------------------------------------------------------------------------

    updateStatus("Mounting data directory ...");

    QRFAppLinkData.direct = new File(Environment.getExternalStorageDirectory() + "/QRFData");

    debug("Setting data directory to: " + QRFAppLinkData.direct.getAbsolutePath());

    if (QRFAppLinkData.direct.exists() == false) {
      debug("Directory " + QRFAppLinkData.direct.getAbsolutePath() + " does not exist, creating ...");

      if (!QRFAppLinkData.direct.mkdir()) {
        updateStatus("Error, unable to make QRF data directory");
        // disable ();
        return (false);
      } else {
        updateStatus("Created QRF data directory");
      }
    } else {
      updateStatus("QRF data directory exists, good");
    }

    // >-------------------------------------------------------------------------

    updateStatus("Connecting to server ...");

    debug("Setting server host to: " + hostnameInput.getText().toString());

    QRFAppLinkData.currentListener = this; // Make sure we set this before anything else
    QRFAppLinkData.commManager = new QRFCommBase();
    QRFAppLinkData.commManager.setServerHost(hostnameInput.getText().toString());
    QRFAppLinkData.commManager.setServerUsername(usernameInput.getText().toString());
    QRFAppLinkData.commManager.setServerPassword(passwordInput.getText().toString());
    if (QRFAppLinkData.commManager.clientInit() == true) {
      updateStatus("Connected to broker (" + QRFAppLinkData.commManager.getServerHost()  + "), you can now click 'start' to register a class and observer");
      send(QRFAppLinkData.generator.createActionMessage("init"));
    } else {
      updateStatus("Error connecting to broker, please check your settings");
      return (false);
    }

    return (true);
  }

  /**
   * 
   */
  @Override
  public void ready() {
    debug("ready ()");

    //checkButton.setEnabled(false);
    okButton.setEnabled(true);
    
    QRFAppLinkData.connected=true;

    updateStatus("Connection established, app ready");

    debug("ready () Done");
  }
}
