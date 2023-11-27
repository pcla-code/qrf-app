package edu.upenn.recorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import edu.upenn.recorder.R;
import android.content.Context;

/*
 * @Author: asalvi
 * This class provides an error dialog that displays the string passed into its constructor. 
 */
public class QRFErrorDisplay extends QRFBase
{
	public QRFErrorDisplay(String errorString, Context context)
	{
		debug (errorString);
		
		new AlertDialog.Builder(context)
		.setTitle(R.string.error_dialog_label)
		.setMessage(errorString)
		.setPositiveButton("Cancel", new OnClickListener() 
		{
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.dismiss();
			}
		}) // We create an in-line anonymous class for the listener for the button.
		.show();
	}
}
