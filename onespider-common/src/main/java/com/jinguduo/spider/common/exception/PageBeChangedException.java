package com.jinguduo.spider.common.exception;

public class PageBeChangedException extends QuickException {

	private static final long serialVersionUID = -9204657326045393439L;

	public PageBeChangedException() {
        super();
    }
    
    public PageBeChangedException(String message) {
        super(message);
    }
    
    public PageBeChangedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PageBeChangedException(Throwable cause) {
        super(cause);
    }
}
