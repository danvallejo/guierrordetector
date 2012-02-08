package edu.washington.cs.detector.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.ClassHierarchy;

public class AndroidLayoutParser {

	static String id_prefix = "@+id/";
	
	static Set<String> androidWidgetShortName = AndroidUtils.getLibWidgets();
	static Set<String> otherAndroidUIFullName = AndroidUtils.getOtherUIs();

	public static Map<String, String> extractAndroidUIMapping(ClassHierarchy cha, String dir) {
		try {
		    List<Reader> xmlFileReaders = AndroidUtils.getAllLayoutXMLFromDir(new File(dir));
		    return extractAndroidUIMapping(cha, xmlFileReaders);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Map<String, String> extractAndroidUIMapping(ClassHierarchy cha, List<Reader> readers) throws IOException {
		Map<String, String> map = new LinkedHashMap<String, String>();
		for(Reader reader : readers) {
			String xmlContent = Files.getFileContents(reader);
			map.putAll(extractAndroidUIMapping(xmlContent, cha));
		}
		return map;
	}
	
	// mapping from id => full class names
	public static Map<String, String> extractAndroidUIMapping(String xmlContent, ClassHierarchy cha) {
		Map<String, String> extractedMap = new LinkedHashMap<String, String>();
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			AndroidLayoutParser.LayoutIDExtractor handler = new AndroidLayoutParser.LayoutIDExtractor();
			// start parsing
			byte[] bytes = xmlContent.getBytes("UTF8");
			InputStream inputStream = new ByteArrayInputStream(bytes);
			InputSource source = new InputSource(inputStream);
			saxParser.parse(source, handler);

			extractedMap.putAll(handler.idMap);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return validateFullNames(extractedMap, cha);
	}
	
	static Map<String, String> validateFullNames(Map<String, String> extractedMap, ClassHierarchy cha) {
		Map<String, String> validatedMap = new LinkedHashMap<String, String>();
		
		for(String idKey : extractedMap.keySet()) {
			String value = extractedMap.get(idKey);
			if(androidWidgetShortName.contains(value)) {
				validatedMap.put(idKey,  AndroidUtils.WIDGET_PACKAGE + "." + value);
			} else if (value.startsWith(AndroidUtils.WIDGET_PACKAGE)) {
				Utils.checkTrue(androidWidgetShortName.contains(value.substring(AndroidUtils.WIDGET_PACKAGE.length())));
				validatedMap.put(idKey, value);
			} else if (otherAndroidUIFullName.contains(value)) {
				validatedMap.put(idKey, value);
			} else {
				Utils.checkTrue(cha != null);
				IClass VIEW = AndroidUtils.getWidgetView(cha);
				IClass c = WALAUtils.lookupClass(cha, value); //it must be full name
				if(c != null) {
					if(cha.isAssignableFrom(VIEW, c)) {
						validatedMap.put(idKey, value);
					} else {
						System.err.println("Not a view?: " + c);
					}
				} else {
					System.err.println("c is null? " + value);
				}
			}
		}
		
		return validatedMap;
	}

	static class LayoutIDExtractor extends DefaultHandler {
		Map<String, String> idMap = new LinkedHashMap<String, String>();
		public LayoutIDExtractor() {
			idMap.clear();
		}
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (attributes != null) {
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getQName(i).equals("android:id")) {
						String idValue = attributes.getValue(i);
						Utils.checkTrue(idValue.startsWith(id_prefix));
						String id = idValue.substring(id_prefix.length());
						Utils.checkTrue(!idMap.containsKey(id));
						idMap.put(id, qName);
					}
				}
			}
		}
	}
}