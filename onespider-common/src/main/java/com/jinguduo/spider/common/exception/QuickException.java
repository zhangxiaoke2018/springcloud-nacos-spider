package com.jinguduo.spider.common.exception;

//
public class QuickException extends RuntimeException {

	private static final long serialVersionUID = -8859482351310163801L;

	public synchronized Throwable fillInStackTrace() {
		// nothing
		return this;
	}
	
	public QuickException() {
        super();
    }
    
    public QuickException(String message) {
        super(cut(message));
    }
    
    public QuickException(String message, Throwable cause) {
        super(cut(message), cause);
    }
    
    public QuickException(Throwable cause) {
        super(cause);
    }
    
    private static final int MAX_LEGTH = 1024;
    
    private static String cut(String message) {
    	if (message != null && message.length() > MAX_LEGTH) {
    		message = message.substring(0, MAX_LEGTH);
    	}
    	return message;
    }
}
