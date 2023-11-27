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
import java.io.IOException;
import java.sql.Timestamp;

import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author vvelsen
 */
public class QRFInput extends QRFActivityBase implements OnClickListener, QRFDialogNotifier {
  private MediaRecorder mediaRecorder = null;
  private File audioFile;

  private Button startButton;
  private Button stopButton;

  private Button nextButton;
  private Button skipButton;

  private TextView studentInfo;
  private TextView notes;

  private Button endButton;

  private Integer index = 0;

  private Context context = null;

  private Long studentTime = 0L;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    debug("QRFInput:onCreate ()");

    super.onCreate(savedInstanceState);

    setContentView(R.layout.input);

    context = this;

    nextButton = (Button) findViewById(R.id.next_button);
    nextButton.setOnClickListener(this);

    skipButton = (Button) findViewById(R.id.skip_button);
    skipButton.setOnClickListener(this);

    endButton = (Button) findViewById(R.id.end_button);
    endButton.setOnClickListener(this);

    startButton = (Button) findViewById(R.id.start);
    startButton.setOnClickListener(this);

    stopButton = (Button) findViewById(R.id.stop);
    stopButton.setOnClickListener(this);

    studentInfo = (TextView) findViewById(R.id.studentinfo);
    notes = (TextView) findViewById(R.id.notes);

    QRFAppLinkData.currentListener = this;

    // Indicate to the broker that we're ready to start receiving
    // student affect messages
    send(QRFAppLinkData.generator.createActionMessage("ready"));

    debug("QRFInput:onCreate () Done");
  }

  /**
   * 
   */
  public void onClick(View v) {

    String noteContent = "";

    switch (v.getId()) {
    case R.id.next_button:

      // Make sure we stop the audio if at this point we were recording
      onRecordingStop();

      noteContent = notes.getText().toString();
      debug("Logging notes text: " + noteContent);

      logSAI("APP", "BUTTONCLICK", "R.id.next_button", "\"" + noteContent + "\"");

      send(QRFAppLinkData.generator.createAcceptMessage());

      // Clear the notes, requested as an ease of use feature
      notes.setText("");

      break;

    case R.id.skip_button:

      // Make sure we stop the audio if at this point we were recording
      onRecordingStop();

      QRFRejectionDialog cdd = new QRFRejectionDialog(this);
      cdd.setQRFDialogNotifier(this);
      cdd.show();

      break;

    case R.id.end_button:
      logSAI("APP", "BUTTONCLICK", "R.id.end_button", "\"" + noteContent + "\"");
      Intent finishIntent = new Intent(this, QRFFinish.class);
      startActivity(finishIntent);
      finish();
      break;

    case R.id.start:
      logSAI("APP", "BUTTONCLICK", "R.id.start", "");
      startAudioRecording();
      break;

    case R.id.stop:
      logSAI("APP", "BUTTONCLICK", "R.id.stop", "");
      onRecordingStop();
      break;
    }
  }

  /**
   * 
   */
  public void startAudioRecording() {
    debug("startAudioRecording ()");

    if (QRFAppLinkData.direct != null) {

      Timestamp timestamp = new Timestamp(System.currentTimeMillis());

      Long raw = timestamp.getTime();

      debug("Storing in: " + QRFAppLinkData.direct.getAbsolutePath());

      index++;
      audioFile = new File(QRFAppLinkData.direct.getAbsolutePath(),
          QRFAppLinkData.observerName + "-" + QRFAppLinkData.session + "-" + sdf.format(studentTime) + "-" + sdf.format(timestamp) + "-audio-index_" + index + "-obs_" + QRFAppLinkData.observationIndex + ".3gp");

      mediaRecorder = new MediaRecorder();
      mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
      // mediaRecorder.setAudioEncodingBitRate(16);
      // mediaRecorder.setAudioSamplingRate(44100);
      mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

      try {
        mediaRecorder.prepare();
      } catch (IllegalStateException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      mediaRecorder.start();
      startButton.setEnabled(false);
      stopButton.setEnabled(true);
    } else {
      debug("Error: QRFAppLinkData.direct is null");
    }
  }

  /**
   * 
   */
  protected void onRecordingStop() {
    debug("onRecordingStop ()");

    if (mediaRecorder != null) {
      debug("Stopping media recorder ...");
      mediaRecorder.stop();
      mediaRecorder.release();
      mediaRecorder = null;
      debug("Media recorder stopped");
    } else {
      debug("Error: media recorder object is null");
    }

    startButton.setEnabled(true);
    stopButton.setEnabled(false);
  }

  /**
   * 
   */
  protected void processObservation() {
    debug("processObservation ()");

    enableInterface();

    String info = ("Displaying user info: \nobservation: " + QRFAppLinkData.observationIndex + "\nid: "
        + QRFAppLinkData.currentInfo.studentID + "\ntime: " + QRFAppLinkData.currentInfo.studentTime + "\nemotion: "
        + QRFAppLinkData.currentInfo.emotionPattern + "\nbehavior: " + QRFAppLinkData.currentInfo.behaviorPattern
        + "\ntype: " + QRFAppLinkData.currentInfo.studentOccurrenceType);

    logSAI("SERVER", "PROCESSOBSERVATION", "OBSERVATION",
        "observer:" + QRFAppLinkData.observerName + ",observation:" + QRFAppLinkData.observationIndex + ",name:"
            + QRFAppLinkData.currentInfo.studentName + ",id:" + QRFAppLinkData.currentInfo.studentID + ",time: "
            + QRFAppLinkData.currentInfo.studentTime + ",emotion: " + QRFAppLinkData.currentInfo.emotionPattern
            + ",behavior: " + QRFAppLinkData.currentInfo.behaviorPattern + ",type: "
            + QRFAppLinkData.currentInfo.studentOccurrenceType + ",location: "
            + QRFAppLinkData.currentInfo.studentLocation);

    studentInfo.setText(info);
  }

  /**
   * 
   */
  @Override
  public void processDialogInput() {
    debug("processDialogInput ()");

    String noteContent = notes.getText().toString();
    debug("Logging notes text: " + noteContent);

    logSAI("APP", "CHOOSEREJECT", "R.id.ok_button", "\"" + QRFAppLinkData.justification + "\"");

    send(QRFAppLinkData.generator.createRejectedMessage());

    logSAI("APP", "BUTTONCLICK", "R.id.skip_button", "\"" + noteContent + "\"");

    // Clear the notes, requested as an ease of use feature
    notes.setText("");
  }

  /**
   * 
   */
  protected void disableInterface() {
    debug("disableInterface ()");
    notes.setEnabled(false);
    notes.setText("Currently no new patterns available, waiting for new data ...");
    studentInfo.setText("");
    nextButton.setEnabled(false);
    skipButton.setEnabled(false);
    // endButton.setEnabled(false);
    startButton.setEnabled(false);
    stopButton.setEnabled(false);
  }

  /**
   * 
   */
  protected void enableInterface() {
    debug("enableInterface ()");
    notes.setEnabled(true);
    notes.setText("");
    studentInfo.setText("");
    nextButton.setEnabled(true);
    skipButton.setEnabled(true);
    // endButton.setEnabled(true);
    startButton.setEnabled(true);
    stopButton.setEnabled(true);

    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    studentTime = timestamp.getTime();
  }
}
