package edu.washington.cs.detector.util;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPropertyReader extends TestCase {
	
	public static Test suite() {
		return new TestSuite(TestPropertyReader.class);
	}

	public void testDetectorProperty() {
		PropertyReader reader = PropertyReader.createInstance("./src/detector.properties");
		this.checkAndShowEntryNumber(3, reader);
	}
	
	public void testTestProperty() {
		PropertyReader reader = PropertyReader.createInstance("./tests/tests.properties");
		this.checkAndShowEntryNumber(8, reader);
	}
	
	private void checkAndShowEntryNumber(int expectedNum, PropertyReader reader) {
		assertEquals(expectedNum, reader.getKeys().size());
		for(Object key : reader.getKeys()) {
			System.out.println("key: " + key + "; value: " + reader.getProperty((String)key));
		}
	}
}
