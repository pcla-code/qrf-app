package edu.upenn.qrf;

import edu.upenn.recorder.QRFBase;

/**
 * @author Martin van Velsen <vvelsen@knossys.com>
 * 
 * Here we have a class that represents a connected client/app. We maintain state here for
 * the class and we track variables that allow the broker to communicate to a client directly.
 * Ideally we refactor the code such that the broker calls methods on an object of this
 * class directly but that will have to wait a bit after we're at least stable. Currently
 * it is essentially just a proxy object for the client without active communication code
 * in it.
 */
public class QRFClientProxy extends QRFBase {
  
	public static int CLIENTSTATE_IDLE=0; // Nothing has happened yet, we're not even connected potentially
	public static int CLIENTSTATE_INITIALIZED=1; // We've received the init action and we've went back the configuration object
	public static int CLIENTSTATE_REGISTERED=2; // We've received the class name from the app and it checks out
	public static int CLIENTSTATE_READY=3; // The interface is showing the list of students
	public static int CLIENTSTATE_INPUT=4; // The client is processing input in this state
	public static int CLIENTSTATE_FINISHING=5; // We're at the finish/wrapup screen and we're probably transmitting logs
	
  private Boolean ready=false;
  private int state=QRFClientProxy.CLIENTSTATE_IDLE;
  
  private String stateAdvice = "No advice";
  private String sessionID = "-1";

  /**
   * 
   * @param aValue
   */
  public void setReady(boolean aValue) {
    ready=aValue;    
  }

  /**
   * 
   * @return
   */
  public Boolean getReady () {
    return (ready);
  }

  /**
   * 
   * @return
   */
	public int getState() {
		return state;
	}

	/**
	 * This method is essentially a mini finite state machine written as code. We can use it
	 * to figure out if the app is doing something that's not the right step according to the
	 * accept specs. It will produce advice for a wrong state transition, which is then
	 * available to the broker to send to the client to display.
	 * 
	 * @param state
	 */
	public Boolean setState(int aState) {
		debug ("setState (" + getStateString (state) + "," + getStateString (aState) + ")");
		
		if (state==CLIENTSTATE_INPUT) {
			/*
			if (aState==state) {
				setStateAdvice ("Error: You're trying to navigate to a state you're already in ("+getStateString (aState)+")");
				return (false);
			}
			*/
			
			// This is allowed because in the input state we can take actions that keep us in this state
			
			return (true);
		}
		
		if ((aState==CLIENTSTATE_INITIALIZED) && (state!=CLIENTSTATE_IDLE)) {
			setStateAdvice ("Error: You're trying to initialize (CLIENTSTATE_INITIALIZED) the client while it is in an active state, not idling (CLIENTSTATE_IDLE)");
			return (false);
		}
		
		if ((aState==CLIENTSTATE_REGISTERED) && (state!=CLIENTSTATE_INITIALIZED)) {
			setStateAdvice ("Error: You're trying to register a class (CLIENTSTATE_REGISTERED) while the client hasn't been initialized and configured yet (CLIENTSTATE_INITIALIZED)");
			return (false);
		}
		
		if ((aState==CLIENTSTATE_READY) && (state!=CLIENTSTATE_REGISTERED)) {
			setStateAdvice ("Error: You're trying to navigate to the student input screen (CLIENTSTATE_READY) while the class name hasn't been registered yet (CLIENTSTATE_REGISTERED)");
			return (false);
		}		
		
		if ((aState==CLIENTSTATE_INPUT) && (state!=CLIENTSTATE_READY)) {
			setStateAdvice ("Error: You're trying to process student observation information (CLIENTSTATE_INPUT) while you haven't navigated away from the student list screen yet (CLIENTSTATE_READY)");
			return (false);
		}		
		
		if ((aState==CLIENTSTATE_FINISHING) && (state!=CLIENTSTATE_INPUT)) {
			setStateAdvice ("Error: You're trying to wrap up a session (CLIENTSTATE_FINISHING) while you're not coming from the student observation input screen (CLIENTSTATE_INPUT)");
			return (false);
		}		
		
		state = aState;
		
		return (true);
	}

	/**
	 * 
	 * @param aState
	 * @return
	 */
	private String getStateString(int aState) {
    switch (aState) {
      case 0:
      	    return ("CLIENTSTATE_IDLE");
      case 1:	  	
      	    return ("CLIENTSTATE_INITIALIZED");
      case 2: 	  	
      	    return ("CLIENTSTATE_REGISTERED");
      case 3:	  	
      	    return ("CLIENTSTATE_READY");
      case 4:	  	
      	    return ("CLIENTSTATE_INPUT");
      case 5: 	  	
      	    return ("CLIENTSTATE_FINISHING");
    }

		return "Unknown State";
	}

	/**
	 * 
	 * @return
	 */
	public String getStateAdvice() {
		return stateAdvice;
	}

	/**
	 * 
	 * @param stateAdvice
	 */
	public void setStateAdvice(String stateAdvice) {
		this.stateAdvice = stateAdvice;
	}

	/**
	 * @return
	 */
	public String getSessionID() {
		return sessionID;
	}

	/**
	 * @param sessionID
	 */
	public void setSessionID(String sessionID) {
		this.sessionID = sessionID;
	}
}
