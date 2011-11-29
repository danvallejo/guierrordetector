package edu.washington.cs.detector.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AndroidUtils {
	
	public static Collection<String> extractAllWidgets(File f) throws ZipException, IOException {
		List<Reader> xmlFileReaders = new LinkedList<Reader>();
		if(f.isDirectory()) {
			//check whether the dir "res" exists
			List<File> allFiles = Files.getFileListing(f);
			String desiredPath = f.getAbsolutePath() + Globals.fileSep + "res" + Globals.fileSep + "layout";
			for(File file : allFiles) {
				if(file.getName().endsWith(".xml") && file.getAbsolutePath().startsWith(desiredPath)) {
					//System.out.println(file);
					BufferedReader in = new BufferedReader(new FileReader(file));
					xmlFileReaders.add(in);
				}
			}
		} else {
			//f must be an apk file
			if(!f.getName().endsWith(".apk")) {
				throw new RuntimeException("It must be an apk file: " + f.getAbsolutePath());
			}
			ZipFile jarFile = new ZipFile(f);
			Enumeration<? extends ZipEntry> e = jarFile.entries();
			while(e.hasMoreElements()) {
				ZipEntry ze = e.nextElement();
				if(ze.toString().indexOf("res/layout") != -1
						&& ze.toString().endsWith(".xml")) {
				    System.out.println(ze);
					BufferedReader in = new BufferedReader(
							new InputStreamReader(jarFile.getInputStream(ze)));
					xmlFileReaders.add(in);
				}
			}
		}
		
		//read each xml file one by one
		Collection<String> widgetClasses = new LinkedList<String>();
		for(Reader reader : xmlFileReaders) {
			String xmlContent = Files.getFileContents(reader);
			//System.out.println(xmlContent);
			widgetClasses.addAll(extractWidgets(xmlContent));
		}
		return widgetClasses;
	}
	
	//f is a single xml file
	public static Collection<String> extractWidgets(File xmlFile) {
		try {
			return extractWidgets(Files.getFileContents(xmlFile));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	//return the class full name like: a.b.c.d
	public static Collection<String> extractWidgets(String xmlContent) {
		final Set<String> declaredWidgets = new LinkedHashSet<String>();
		try {
			//achieve all widgets
			final Set<String> allWidgets = getAllWidgets();
			//init a sax parser factory
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				public void startElement(String uri, String localName,
						String qName, Attributes attributes) throws SAXException {
					//check the short name
					if(allWidgets.contains(qName)) {
						declaredWidgets.add(WIDGET_PACKAGE + "." + qName);
					} else {
						if(qName.startsWith(WIDGET_PACKAGE)) {
							//double check
							if(!allWidgets.contains(qName.substring(WIDGET_PACKAGE.length()))) {
								throw new RuntimeException("The widget list is not complete, it miss: " + qName);
							}
							declaredWidgets.add(qName);
						}
					}
					//check the attributes
					if(attributes != null) {
					    for(int i = 0; i < attributes.getLength(); i++) {
						    if(attributes.getQName(i).equals("class")) {
						    	String clazzValue = attributes.getValue(i);
						    	if(clazzValue.startsWith(".")) { //be aware of ., it can be specified as <class=".clzzName"/>
						    		clazzValue = clazzValue.substring(1);
						    	}
						    	if(allWidgets.contains(clazzValue)) {
						    		declaredWidgets.add(WIDGET_PACKAGE + "." + clazzValue);
						    	} else {
						    		if(clazzValue.startsWith(WIDGET_PACKAGE)) {
										//double check
										if(!allWidgets.contains(clazzValue.substring(WIDGET_PACKAGE.length()))) {
											throw new RuntimeException("The widget list is not complete, it miss: " + clazzValue);
										}
										declaredWidgets.add(clazzValue);
									}
						    	}
						    }
					    }
					}
				}
			};
			byte[] bytes = xmlContent.getBytes("UTF8");
			InputStream inputStream = new ByteArrayInputStream(bytes);
			InputSource source = new InputSource(inputStream);
			saxParser.parse(source, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return declaredWidgets;
	}
	
	public static Set<String> getAllWidgets() {
		Set<String> widgetSet = new HashSet<String>();
		for(String widgetName : widgetShortNames) {
			widgetSet.add(widgetName);
		}
		return widgetSet;
	}
	
	public static String WIDGET_PACKAGE = "android.widget";
	
	//FIXME, should it be ClassA$ClassB or ClassA.ClassB
	private static String[] widgetShortNames = new String[] {
		"AbsListView",
		"AbsListView.LayoutParams",
		"AbsoluteLayout",
		"AbsoluteLayout.LayoutParams",
		"AbsSeekBar ",
		"AbsSpinner",
		"AdapterView",
		"AdapterView.AdapterContextMenuInfo",
		"AdapterViewAnimator",
		"AdapterViewFlipper",
		"AlphabetIndexer",
		"AnalogClock",
		"ArrayAdapter",
		"AutoCompleteTextView",
		"BaseAdapter",
		"BaseExpandableListAdapter",
		"Button",
		"CalendarView",
		"CheckBox",
		"CheckedTextView",
		"Chronometer",
		"CompoundButton",
		"CursorAdapter",
		"CursorTreeAdapter",
		"DatePicker",
		"DialerFilter ",
		"DigitalClock",
		"EdgeEffect",
		"EditText",
		"ExpandableListView",
		"ExpandableListView.ExpandableListContextMenuInfo",
		"Filter",
		"Filter.FilterResults",
		"FrameLayout",
		"FrameLayout.LayoutParams",
		"Gallery",
		"Gallery.LayoutParams",
		"GridLayout",
		"GridLayout.Alignment",
		"GridLayout.LayoutParams",
		"GridLayout.Spec",
		"GridView",
		"HeaderViewListAdapter",
		"HorizontalScrollView",
		"ImageButton",
		"ImageSwitcher ",
		"ImageView",
		"LinearLayout",
		"LinearLayout.LayoutParams",
		"ListPopupWindow",
		"ListView",
		"ListView.FixedViewInfo",
		"MediaController",
		"MultiAutoCompleteTextView",
		"MultiAutoCompleteTextView.CommaTokenizer",
		"NumberPicker",
		"OverScroller",
		"PopupMenu",
		"PopupWindow",
		"ProgressBar",
		"QuickContactBadge",
		"RadioButton",
		"RadioGroup",
		"RadioGroup.LayoutParams",
		"RatingBar",
		"RelativeLayout",
		"RelativeLayout.LayoutParams",
		"RemoteViews",
		"RemoteViewsService",
		"ResourceCursorAdapter",
		"ResourceCursorTreeAdapter",
		"Scroller",
		"ScrollView",
		"SearchView",
		"SeekBar",
		"ShareActionProvider",
		"SimpleAdapter",
		"SimpleCursorAdapter",
		"SimpleCursorTreeAdapter",
		"SimpleExpandableListAdapter",
		"SlidingDrawer",
		"Space",
		"Spinner",
		"StackView ",
		"Switch",
		"TabHost",
		"TabHost.TabSpec",
		"TableLayout",
		"TableLayout.LayoutParams",
		"TableRow",
		"TableRow.LayoutParams",
		"TabWidget",
		"TextSwitcher",
		"TextView",
		"TextView.SavedState",
		"TimePicker",
		"Toast",
		"ToggleButton",
		"TwoLineListItem",
		"VideoView",
		"ViewAnimator",
		"ViewFlipper",
		"ViewSwitcher",
		"ZoomButton ",
		"ZoomButtonsController",
		"ZoomControls"
	};
}