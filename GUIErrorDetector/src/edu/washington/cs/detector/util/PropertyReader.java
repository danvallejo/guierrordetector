package edu.washington.cs.detector.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class PropertyReader {

	private final Properties props;

	private PropertyReader(String propertyFilePath) {
		this.props = this.loadProperties(propertyFilePath);
	}
	
	public static PropertyReader createInstance(String propertyFilePath) {
		return new PropertyReader(propertyFilePath);
	}

	private Properties loadProperties(String fPath) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(fPath));
			return props;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Set<Object> getKeys() {
		return props.keySet();
	}
	
	public String getProperty(String name) {
		return props.getProperty(name);
	}
}