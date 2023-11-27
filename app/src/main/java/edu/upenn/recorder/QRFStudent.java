package edu.upenn.recorder;

/*
 * @Author:asalvi
 * A very simple class representing a student. I chose to make this a class of its own for extensibility.
 */
public class QRFStudent extends QRFBase 
{	
	private String studentID = null;
	
	public QRFStudent(String id)
	{
		this.studentID = id;
	}

	@Override
	public String toString()
	{
		return studentID;
	}
	
	public String getStudentID() 
	{
		return studentID;
	}

	public void setStudentID(String studentID) 
	{
		this.studentID = studentID;
	}		
}
