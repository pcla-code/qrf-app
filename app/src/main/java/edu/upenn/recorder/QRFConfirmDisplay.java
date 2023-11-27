package edu.upenn.recorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import edu.upenn.recorder.R;
import android.app.Activity;

/*
 * @Author: Aatish
 * 
 * This class creates a confirmation dialog with a choice to "Cancel" or "Commit." The calling 
 * activity provides the intent for the activity to be executed if the user chooses to commit. If the 
 * intent provided is null, the dialog treats a commit as an instruction to exit/finish the calling 
 * activity
 */
public class QRFConfirmDisplay extends QRFBase implements DialogInterface.OnClickListener
{	
	Intent nextIntent = null;
	Activity activity = null;
	Boolean okDialog = false;
	
	/**
	 * 
	 * @param confirmString
	 * @param currActivity
	 * @param nextIntent
	 */
	public QRFConfirmDisplay(String confirmString, 
							  Activity currActivity, 
							  Intent nextIntent,
							  Boolean aOkDialog)
	{	
		this.nextIntent = nextIntent;
		this.activity = currActivity;
		this.okDialog = aOkDialog;
		
		if (okDialog==false)
		{
			new AlertDialog.Builder(activity)
			.setTitle(R.string.confirm_dialog_label)
			.setMessage(confirmString)
			.setPositiveButton("Cancel", this)
			.setNeutralButton("Confirm", this)
			.show();
		}
		else
		{
			new AlertDialog.Builder(activity)
			.setTitle(R.string.confirm_dialog_label)
			.setMessage(confirmString)
			.setPositiveButton("Ok", this)
			.show();			
		}
	}
	/**
	 * 
	 */
	public void onClick(DialogInterface dialog, int which)
	{
		if (okDialog==true)
		{
			if(nextIntent == null)
				activity.finish();
			else
			{
				activity.startActivity(nextIntent);
				activity.finish();
			}			
		}
		
		switch(which)
		{
			case DialogInterface.BUTTON_POSITIVE:
													dialog.dismiss();
													break;
			case DialogInterface.BUTTON_NEUTRAL:
													dialog.dismiss();
													
													if(nextIntent == null)
														activity.finish();
													else
													{
														activity.startActivity(nextIntent);
														activity.finish();
													}
													break;
		}
	}
}
