/*
 * DSTotalConflictException.java
 *
 * Created on July 26, 2007, 1:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package dissprogram.evidence;

/**
 *
 * @author z4x
 */
public class DSTotalConflictException extends Exception{
    
    String message; 
    
    /** Creates a new instance of DSTotalConflictException */
    public DSTotalConflictException() {
        super("Complete conflict between two evidential mass functions--cannot combine them.");    
        message = "Complete conflict between two evidential mass functions--cannot combine them.";
    }
    
    public DSTotalConflictException(String err){
        super(err);
        message = err;
    }
    
    public String getError(){
        return message;
    }
    
}
