package edu.upenn.recorder;

import android.app.Application;
import android.widget.ArrayAdapter;
import edu.upenn.recorder.R;
import android.content.res.Resources;

/*
 * @Author: asalvi
 * 
 * This class represents the application context for all the activities 
 * (i.e. the various forms and screens) in HART. Since activities are separate
 * processes and cannot share data, the application serves as the global variable space.
 *  
 * PITFALL - Adding a new schema requires an update to the getAffectAdapter and the
 * getBehaviorAdapter methods.
 */

public class QRFApplicationData extends Application {	
	private String username = null;
	private String schoolname = null;
	private String classname = null;
	private String software = null;
	private int numStudents = 0;
	private int completedStInfos = 0; // Number of student IDs that have been entered
	
	//private HartSchemas.BehaviorSchemas behaviorTemplate; //The template for behavior categories
	//private HartSchemas.AffectSchemas affectTemplate; //The template for affect categories
	//private HartSchemas.InterventionSchemas interventionTemplate; //The template for intervention categories
	
	private ArrayAdapter<String> behaviorAdapter = null; //The list of behaviors used by the drop-down when recording
	private ArrayAdapter<String> affectAdapter = null; //The list of affects used by the drop-down when recording
	private ArrayAdapter<String> interventionAdapter = null; //The list of interventions used by the drop-down when recording
	
	private QRFStudent[] students = null; //The array of students
	private long localStartTime = 0; // The system time in milliseconds when the recording was started
	private long ntpStartTime = 0; // The localStarTime plus the offset between the local and the ntp clocks
	private QRFFileManager fileManager = null; // A reference to the data file for i/o
	
	// Data for the GetBehaviorData screen
	
	private int currStudentIndex;
	private int obsCount;
	private long obsCountdownLength;
	
	private Resources resources;
	
	// Global mode variables
	
	public static int STUDENT=0;
	public static int TEACHER=1;
	public static int AFFECTONLY=2;
	
	private int applicationMode=QRFApplicationData.STUDENT;
	
	/**
	 * 
	 */
	public void onCreate()
	{
		super.onCreate();
		initialize();
	}	
	/**
	 * 
	 */
	protected void debug (String aMessage)
	{
		QRFBase.extDebug(aMessage);
	}
	/**
	 * 
	 */
	public void initialize()
	{
		debug ("initialize()");
		
		resources = getResources();
		
		username = null;
		schoolname = null;
		classname = null;
		software = null;
		numStudents = 0;
		//behaviorTemplate = null;
		behaviorAdapter = null;
		affectAdapter = null;
		students = null;
		localStartTime = 0;
		ntpStartTime = 0;
		fileManager = null;
		
		currStudentIndex = 1;
		obsCount = 0;
		resetObsCountdownLength();

	}
	/**
	 * 
	 */	
	public int getApplicationMode() 
	{
		return applicationMode;
	}
	/**
	 * 
	 */	
	public void setApplicationMode(int applicationMode) 
	{
		this.applicationMode = applicationMode;
	}	
	/**
	 * 
	 */
	public void resetRecordingInfo()
	{
		debug ("initialize()");
		
		currStudentIndex = 1;
		obsCount = 0;
		localStartTime = 0;
		ntpStartTime = 0;
		resetObsCountdownLength();
	}
	/**
	 * 
	 */
	public void resetClassInfo()
	{
		debug ("initialize()");
		
		classname = null;
		completedStInfos = 0;
		behaviorAdapter = null;
		affectAdapter = null;
		students = null;
		localStartTime = 0;
		ntpStartTime = 0;
		fileManager = null;
		resetRecordingInfo();
	}
	/**
	 * 
	 */
	public int getCurrStudentIndex()
	{
		return currStudentIndex;
	}
	/**
	 * 
	 */	
	public void setCurrStudentIndex(int currIndex)
	{
		this.currStudentIndex = currIndex;
	}
	/**
	 * 
	 */	
	public int getObsCount()
	{
		return obsCount;
	}
	/**
	 * 
	 */	
	public void setObsCount(int obsCount)
	{
		this.obsCount = obsCount;
	}
	/**
	 * 
	 */	
	public long getObsCountdownLength()
	{
		return obsCountdownLength;
	}
	/**
	 * 
	 */	
	public void setObsCountdownLength(long obsCountdownLength)
	{
		this.obsCountdownLength = obsCountdownLength;
	}
	/**
	 * 
	 */	
	public void resetObsCountdownLength()
	{
		 int length = resources.getInteger(R.integer.observation_countdown_milliseconds);
		 
		 this.obsCountdownLength = length;
	}
	/**
	 * 
	 */	
	public String getUsername() 
	{
		return username;
	}
	/**
	 * 
	 */	
	public void setUsername(String username) 
	{
		this.username = username;
	}
	/**
	 * 
	 */
	public String getSchoolname() 
	{
		return schoolname;
	}
	/**
	 * 
	 */
	public void setSchoolname(String schoolname) 
	{
		this.schoolname = schoolname;
	}
	/**
	 * 
	 */
	public String getClassname() 
	{
		return classname;
	}
	/**
	 * 
	 */
	public void setClassname(String classname) 
	{
		this.classname = classname;
	}
	/**
	 * 
	 */
	public String getSoftware() 
	{
		return software;
	}
	/**
	 * 
	 */
	public void setSoftware(String software) 
	{
		this.software = software;
	}
	/**
	 * 
	 */
	public int getNumStudents() 
	{
		return numStudents;
	}
	/**
	 * 
	 */
	public void setNumStudents(int numStudents) 
	{
		debug ("setNumStudents("+numStudents+")");
		
		this.numStudents = numStudents;
		students = new QRFStudent[numStudents];
	}	
	/**
	 * 
	 */	
	public void setStudent(int index, QRFStudent student)
	{
		if(numStudents == 0)
			return;
		
		if(students.length == 0)
		{
			students = new QRFStudent[numStudents];
			completedStInfos = 0;
		}
		
		students[index] = student;
	}
	/**
	 * 
	 */	
	public QRFStudent getStudent(int index)
	{
		debug ("getStudent("+index+")");
		
		if(index < 0 || index >= numStudents)
			return null;
		
		if(students == null)
		{
			return null;
		}
		
		return students[index];
	}
	/**
	 * 
	 */	
	public QRFStudent getStudentByID(String aName)
	{
		debug ("getStudentByID("+aName+" => " + students.length +")");
				
		if(students == null)
		{
			return null;
		}
		
		for (int i=0;i<students.length;i++)
		{
			QRFStudent test=students [i];
			
			if (test!=null)
			{			
				String testID=test.getStudentID();
				
				debug ("TestID: " + testID);
				
				if (test.getStudentID().equalsIgnoreCase(aName)==true) {
					return (test);
				}
			}
		}
		
		return null;
	}	
	/**
	 * 
	 */
	public long getLocalStartTime() 
	{
		return localStartTime;
	}
	/**
	 * 
	 */
	public void setLocalStartTime(long startTime) 
	{
		this.localStartTime = startTime;
	}
	/**
	 * 
	 */
	public long getNtpStartTime() 
	{
		return ntpStartTime;
	}
	/**
	 * 
	 */
	public void setNtpStartTime(long ntpStartTime) 
	{
		this.ntpStartTime = ntpStartTime;
	}
	/**
	 * 
	 */
	public QRFFileManager getFileManager() 
	{
		return fileManager;
	}
	/**
	 * 
	 */
	public void setFileManager(QRFFileManager fileManager) 
	{
		this.fileManager = fileManager;
	}
	/**
	 * 
	 */
	public String getFilePath() 
	{
		return fileManager.getFilePath();
	}
	/**
	 * 
	 */	
	public int getCompletedStInfos()
	{
		return completedStInfos;
	}
	/**
	 * 
	 */
	public void setCompletedStInfos(int completed)
	{
		completedStInfos = completed;
	}
}
