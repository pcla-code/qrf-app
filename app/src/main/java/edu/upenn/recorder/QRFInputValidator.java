package edu.upenn.recorder;

/*
 * @Author: asalvi
 * 
 * This class serves to validate the various input types (enumerated in HartInputType.java). 
 * It checks to see if a given input meets a set of specified criteria (e.g. hasSpaces, 
 * isAlphaNumeric, etc.) and generates an error string if failures occur. The return from 
 * the validation method is the error string. If there are no errors, it is null.
 */

/**
 * 
 */
public class QRFInputValidator extends QRFBase
{
	static String PASSWORD = "ryan";	
	static String ALPHANUMERIC_ERROR = "can only contain letters or numbers.";
	static String INTEGER_ERROR = "must be a positive integer (number)";
	static String NOSPACES_ERROR = "cannot contain spaces.";
	static String PASSWORD_ERROR = "\nIncorrect password.";
	static String BLANK_ERROR = " cannot be left blank.";	
	
	/**
	 * 
	 */
	public QRFInputValidator()
	{
		
	}
	/**
	 * Returned string is null if no errors and contains an error message if there errors exist.
	 */	
	static String validateInput(String input, QRFInputType inputType)
	{	
		String errorString = new String();
		
		switch(inputType)
		{		
			case USERNAME:
								if(isBlank(input))
								{
									errorString += "\nUsername" + BLANK_ERROR;
									break;
								}

								if(hasSpaces(input))
									errorString += "\nUsername " + NOSPACES_ERROR;
							
								if(!isAlphanumeric(input))
									errorString += "\nUsername " + ALPHANUMERIC_ERROR;
							
								break;
								
			case PASSWORD:
								if(!PASSWORD.equals(input))
									errorString += PASSWORD_ERROR;
								
								break;
								
			case SOFTWARE:
								if(isBlank(input))
								{
									errorString += "\nSoftware " + BLANK_ERROR;
									break;
								}
							
								if(!isAlphanumeric(input))
									errorString += "\nSoftware " + ALPHANUMERIC_ERROR;
							
								break;
								
			case SCHOOLNAME:
								if(isBlank(input))
								{
									errorString += "\nSchool Name " + BLANK_ERROR;
									break;
								}
							
								if(!isAlphanumeric(input))
									errorString += "\nSchool Name " + ALPHANUMERIC_ERROR;
							
								break;			
			case CLASSNAME:
								if(isBlank(input))
								{
									errorString += "\nClass Name " + BLANK_ERROR;
									break;
								}
							
								if(!isAlphanumeric(input))
									errorString += "\nClass Name " + ALPHANUMERIC_ERROR;
							
								break;
		
			case NUMSTUDENTS:
								if(!isNumber(input))
									errorString += "\nNumber of Students " + INTEGER_ERROR;
							
								break;
			
			case STUDENT:
								if(isBlank(input))
								{
									errorString += "\nStudent ID " + BLANK_ERROR;
									break;
								}
							
								if(hasSpaces(input))
									errorString += "\nStudent ID " + NOSPACES_ERROR;
							
								break;
								
			case AFFECT:
								if(input.equalsIgnoreCase("Choose an Affect")==true)
									errorString += "\nPlease choose an Affect";
								break;
								
			case BEHAVIOR:
								if(input.equalsIgnoreCase("Choose a Behavior")==true)
									errorString += "\nPlease select a behavior";
								break;
								
			case INTERVENTION:
								if(input.equalsIgnoreCase("Choose an Intervention")==true)
									errorString += "\nPlease select an intervention";
								break;							
		}

		return errorString;
	}
	/**
	 * 
	 */	
	static boolean isBlank(String input)
	{
		if (input.length() == 0)
			return true;
		
		return false;
	}
	/**
	 * 
	 */	
	static boolean isAlphanumeric(String input)
	{
		if(input != null)
			return input.matches("[\\w\\s]+");
		
		return false;
	}
	/**
	 * 
	 */	
	static boolean hasSpaces(String input)
	{
		if(input!=null)
			return !input.matches("\\S+");
		return false;
	}
	/**
	 * 
	 */	
	static boolean isNumber(String input)
	{
		if( input == null || input.length() == 0 || Integer.parseInt(input) == 0)
			return false;
		
		return input.matches("\\d+");
	}
}
