package com.jinguduo.spider.spider.fiction;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.IOException;

public class NamedFont extends Font {
	/**
	 * 
	 */
	private static final long serialVersionUID = 183923919267001133L;
	private String alias;
	
	protected NamedFont(String alias,Font font) {
		super(font);
		this.alias = alias;
	}
	
	public static NamedFont createFont(String alias,int fontFormat, File fontFile) throws FontFormatException, IOException {
		return new NamedFont(alias,Font.createFont(fontFormat, fontFile));
	}
	
	public NamedFont deriveFont(int style, float size){
		return new NamedFont(alias,super.deriveFont(style, size));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedFont other = (NamedFont) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return super.toString()+"Alias="+alias;
	}
    
	
}
