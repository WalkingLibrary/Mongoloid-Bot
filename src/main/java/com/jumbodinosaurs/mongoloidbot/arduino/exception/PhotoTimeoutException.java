package com.jumbodinosaurs.mongoloidbot.arduino.exception;

public class PhotoTimeoutException extends Exception
{
    public PhotoTimeoutException()
    {
    }
    
    public PhotoTimeoutException(String message)
    {
        super(message);
    }
    
    public PhotoTimeoutException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public PhotoTimeoutException(Throwable cause)
    {
        super(cause);
    }
    
    public PhotoTimeoutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
