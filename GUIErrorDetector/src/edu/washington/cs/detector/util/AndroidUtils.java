package edu.washington.cs.detector.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class AndroidUtils {
	
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