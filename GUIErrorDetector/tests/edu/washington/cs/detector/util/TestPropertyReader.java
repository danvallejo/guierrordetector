package edu.washington.cs.detector.util;

import junit.framework.TestCase;

public class TestPropertyReader extends TestCase {

	public void testDetectorProperty() {
		PropertyReader reader = PropertyReader.createInstance("./src/detector.properties");
		this.checkAndShowEntryNumber(3, reader);
	}
	
	public void testTestProperty() {
		PropertyReader reader = PropertyReader.createInstance("./tests/tests.properties");
		this.checkAndShowEntryNumber(6, reader);
	}
	
	private void checkAndShowEntryNumber(int expectedNum, PropertyReader reader) {
		assertEquals(expectedNum, reader.getKeys().size());
		for(Object key : reader.getKeys()) {
			System.out.println("key: " + key + "; value: " + reader.getProperty((String)key));
		}
	}
}
