package org.eclipse.jgit.errors;

import java.util.regex.PatternSyntaxException;

public class InvalidPatternException extends Exception {

	public InvalidPatternException(String string, String pattern) {
		throw new RuntimeException("NOT IMPLEMENTED YET");
	}

	public InvalidPatternException(String format, String pattern, PatternSyntaxException e) {
		throw new RuntimeException("NOT IMPLEMENTED YET");
	}

}
