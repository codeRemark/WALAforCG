package com.ibm.wala.cast.java.ecj.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.util.config.FileOfClasses;

public class ExampleUtil {
	
	// more aggressive exclusions to avoid library blowup
	  // in interprocedural tests
private static final String EXCLUSIONS = "java\\/awt\\/.*\n" + 
		"javax\\/swing\\/.*\n" + 
		"sun\\/awt\\/.*\n" + 
		"sun\\/swing\\/.*\n" + 
		"com\\/sun\\/.*\n" + 
		"sun\\/.*\n" + 
		"org\\/netbeans\\/.*\n" + 
		"org\\/openide\\/.*\n" + 
		"com\\/ibm\\/crypto\\/.*\n" + 
		"com\\/ibm\\/security\\/.*\n" + 
		"org\\/apache\\/xerces\\/.*\n" + 
		"java\\/security\\/.*\n" +
			//"java\\/util\\/.*\n" +	//added by me
			//"java\\/io\\/.*\n" + //added by me
			//"java\\/lang\\/.*\n" + //added by me
		"";

private static final String TAILEXCLUSIONS = "java\\/awt\\/.*\n" + 
		"javax\\/swing\\/.*\n" + 
		"sun\\/awt\\/.*\n" + 
		"sun\\/swing\\/.*\n" + 
		"com\\/sun\\/.*\n" + 
		"sun\\/.*\n" + 
		"org\\/netbeans\\/.*\n" + 
		"org\\/openide\\/.*\n" + 
		"com\\/ibm\\/crypto\\/.*\n" + 
		"com\\/ibm\\/security\\/.*\n" + 
		"org\\/apache\\/xerces\\/.*\n" + 
		"java\\/security\\/.*\n" +
		"java\\/util\\/.*\n" +	//added by me
		"java\\/io\\/.*\n" + //added by me
		"java\\/lang\\/.*\n" + //added by me
		"";

public static void addDefaultExclusions(AnalysisScope scope) throws UnsupportedEncodingException, IOException {
	    scope.setExclusions(new FileOfClasses(new ByteArrayInputStream(ExampleUtil.EXCLUSIONS.getBytes("UTF-8"))));
}
public static void addTailExclusions(AnalysisScope scope) throws UnsupportedEncodingException, IOException {
    scope.setTailExclusions(new FileOfClasses(new ByteArrayInputStream(ExampleUtil.TAILEXCLUSIONS.getBytes("UTF-8"))));
}
}
