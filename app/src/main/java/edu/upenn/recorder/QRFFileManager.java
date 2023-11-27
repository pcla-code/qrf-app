package edu.upenn.recorder;

//import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import android.os.Environment;

/**
 *
 */
public class QRFFileManager extends QRFBase
{
	private File sdCard = null;
	private File QRFDataDir = null;
	private File QRFDataFile = null;
	private String filePath = null;
	private String fileName = null;
	private FileWriter fileWriter = null;
	private BufferedWriter QRFWriter = null;
	private String delimiter = ",";
	
	/**
	 * 
	 */
	public QRFFileManager (String filename)
	{
		try 
		{		
			//This gets the path of the SDCard
			sdCard = Environment.getExternalStorageDirectory();
			
			//The HARTData directory was manually created on the SDCard.
			filePath = sdCard.getPath() + "/QRFData";
			
			debug ("SD CARD FILE PATH IS: " + filePath);
			
			QRFDataDir = new File(filePath);

      // if the directory does not exist, create it
      if (!QRFDataDir.exists()) {
        debug ("creating directory: " + QRFDataDir.getName());

        boolean result = false;

        try {
          QRFDataDir.mkdir();
          result = true;
        } catch(SecurityException se) {
          //handle it
        }

        if (result) {    
          debug ("Log directory created: " + QRFDataDir.getPath ());  
        }
      }

			this.fileName = filename;

      // We may have a directory but can we write to it?
			if (QRFDataDir.canWrite()) {
				QRFDataFile = new File (QRFDataDir, fileName);
				fileWriter = new FileWriter (QRFDataFile);
				QRFWriter = new BufferedWriter (fileWriter);
			}
		} 
		catch (IOException ioe) 
		{
			ioe.printStackTrace();
		}
	}
	/**
	 * 
	 */
  public void writeLine(String stringToWrite)
	{
	  /*
		if (data.length == 0)
			return;

		String stringToWrite = data[0];

		//Adds the delimiter after each element
		for (int i = 1; i < data.length ; i++)
		{
			stringToWrite += delimiter + data[i];
		}
		*/

		stringToWrite += "\r\n";
		
		try
		{
			debug ("Writing data to log file: " + stringToWrite);
			
			if (QRFWriter==null)
			{
				error ("QRF writer is null!");
				return;
			}
			
			QRFWriter.write(stringToWrite);
			QRFWriter.flush(); //Forces the write to go through and not wait
		} 
		catch (IOException ioe)
		{
			debug ("Got write exceptions " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
	/**
	 * 
	 */
	public void close()
	{
		try
		{
			QRFWriter.close();
		} 
		catch (IOException ioe)
		{
			debug ("Got write exceptions " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
	/**
	 * 
	 */	
	@Override
	public void finalize()
	{
		try
		{
			QRFWriter.close();
		} 
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	/**
	 * 
	 */
	public String getDelimiter() 
	{
		return delimiter;
	}
	/**
	 * 
	 */
	public void setDelimiter(String delimiter) 
	{
		this.delimiter = delimiter;
	}
	/**
	 * 
	 */
	public String getFilePath()
	{
		return (filePath+"/"+fileName);
	}
}
