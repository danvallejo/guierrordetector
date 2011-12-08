package edu.washington.cs.detector.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.ClassHierarchy;

public class AndroidUtils {
	
	public static Collection<String> extractAllUIs(final ClassHierarchy cha, File dir) throws ZipException, IOException {
		//need 2 copies, since a reader will be closed in extractAndroidUI / extractCustomizedUIs
		List<Reader> xmlFileReaders1 = getAllLayoutXMLFromDir(dir);
		List<Reader> xmlFileReaders2 = getAllLayoutXMLFromDir(dir);
		
		//read each xml file one by one
		Collection<String> widgetClasses = new LinkedHashSet<String>();

		widgetClasses.addAll(extractAndroidUIs(xmlFileReaders1));
		widgetClasses.addAll(extractCustomizedUIs(cha, xmlFileReaders2));
		
		return widgetClasses;
	}
	
	public static List<Reader> getAllLayoutXMLFromDir(File dir) throws FileNotFoundException {
		Utils.checkDirExistence(dir.getAbsolutePath());
		File f = dir;
		
		List<Reader> xmlFileReaders = new LinkedList<Reader>();
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
		return xmlFileReaders;
	}
	
	public static Collection<String> extractCustomizedUIs(final ClassHierarchy cha, List<Reader> readers) throws IOException {
		Collection<String> uis = new LinkedHashSet<String>();
		for(Reader reader : readers) {
			String xmlContent = Files.getFileContents(reader);
			uis.addAll(extractCustomizedUIs(cha, xmlContent));
		}
		return uis;
	}
	
	//XXX FIXME not consider short form with included package name, like <MyButton />
	public static Collection<String> extractCustomizedUIs(final ClassHierarchy cha, String xmlContent) {
		final Set<String> customizedUIs = new LinkedHashSet<String>();
		final IClass view = getWidgetView(cha);
		try {
			//init a sax parser factory
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				public void startElement(String uri, String localName,
						String qName, Attributes attributes) throws SAXException {
					//FIXME it assume to use full class name
					if(qName.indexOf(".") != -1) {
						IClass c = WALAUtils.lookupClass(cha, qName);
						if(c != null) {
							if(cha.isAssignableFrom(view, c)) {
								customizedUIs.add(qName);
							}
						}
					}
					//check the attributes
					if(attributes != null) {
					    for(int i = 0; i < attributes.getLength(); i++) {
						    if(attributes.getQName(i).equals("class")) {
						    	String clazzValue = attributes.getValue(i);
						    	//FIXME here
						    	if(clazzValue.indexOf(".") != -1) {
						    		IClass c = WALAUtils.lookupClass(cha, clazzValue);
						    		if(c != null) {
						    			if(cha.isAssignableFrom(view, c)) {
						    				customizedUIs.add(clazzValue);
						    			}
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
		return customizedUIs;
	}
	
	public static Collection<String> extractAndroidUIs(List<Reader> readers) throws IOException {
		Collection<String> uis = new LinkedHashSet<String>();
		for(Reader reader : readers) {
			String xmlContent = Files.getFileContents(reader);
			uis.addAll(extractAndroidUIs(xmlContent));
		}
		return uis;
	}
	
	//return the class full name like: a.b.c.d
	public static Collection<String> extractAndroidUIs(String xmlContent) {
		final Set<String> declaredUIComponents = new LinkedHashSet<String>();
		try {
			//achieve all widgets
			final Set<String> androidWidgetShortName = getLibWidgets();
			final Set<String> otherAndroidUIFullName = getOtherUIs();
			//init a sax parser factory
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = new DefaultHandler() {
				public void startElement(String uri, String localName,
						String qName, Attributes attributes) throws SAXException {
					//check the short name, like <Button attributes = ""/>
					if(androidWidgetShortName.contains(qName)) {
						declaredUIComponents.add(WIDGET_PACKAGE + "." + qName);
					} else {
						//check the long name, like <android.widget.Button  attributes = ""/>
						if(qName.startsWith(WIDGET_PACKAGE)) {
							//double check
							if(!androidWidgetShortName.contains(qName.substring(WIDGET_PACKAGE.length()))) {
								throw new RuntimeException("The widget list is not complete, it miss: " + qName);
							}
							declaredUIComponents.add(qName);
						} else {
							if(otherAndroidUIFullName.contains(qName)) {
								declaredUIComponents.add(qName);
							} else {
								// check short name
								for (String uiName : otherAndroidUIFullName) {
									if (uiName.endsWith("." + qName)) {
										declaredUIComponents.add(uiName);
										break;
									}
								}
							}
							
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
					    		//can not specify like  <View class="Button"/>, so just check the short name
						    	if(androidWidgetShortName.contains(clazzValue) /*possible?*/ || otherAndroidUIFullName.contains(clazzValue)) {
						    		declaredUIComponents.add(WIDGET_PACKAGE + "." + clazzValue);
						    	} else {
						    		if(clazzValue.startsWith(WIDGET_PACKAGE)) {
										//double check
										if(!androidWidgetShortName.contains(clazzValue.substring(WIDGET_PACKAGE.length()))) {
											throw new RuntimeException("The widget list is not complete, it miss: " + clazzValue);
										}
										declaredUIComponents.add(clazzValue);
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
		return declaredUIComponents;
	}
	
	private static Set<String> getLibWidgets() {
		Set<String> widgetSet = new HashSet<String>();
		for(String widgetName : widgetShortNames) {
			widgetSet.add(widgetName);
		}
		return widgetSet;
	}
	
	public static Collection<IClass> getAppActivityClasses(ClassHierarchy cha) {
		Collection<IClass> colls = new LinkedHashSet<IClass>();
		for(IClass c : cha) {
			if(isAppActivity(cha, c)) {
				colls.add(c);
			}
		}
		return colls;
	}
	
	/**
	 * Is the class an instance of activity
	 * */
	public static boolean isAppActivity(ClassHierarchy cha, IClass c) {
		String className = WALAUtils.getJavaFullClassName(c);
		if(isInAndroidLib(className)) {
			return false;
		}
		IClass activity = getActivity(cha);
		return cha.isAssignableFrom(activity, c);
	}
	
	public static boolean isInAndroidLib(String className) {
		return className.startsWith("android.") || className.startsWith("com.android.");
	}
	
	private static String ACTIVITY_CLASS = "android.app.Activity";
	
	private static IClass ACTIVITY = null;
	
	public static IClass getActivity(ClassHierarchy cha) {
		if(ACTIVITY != null) {
			return ACTIVITY;
		}
		ACTIVITY = WALAUtils.lookupClass(cha, ACTIVITY_CLASS);
		if(ACTIVITY == null) {
			throw new RuntimeException("Can not load: " + ACTIVITY_CLASS);
		}
		return ACTIVITY;
	}
	
	private static String WIDGET_PACKAGE = "android.widget";
	
	private static IClass VIEW = null;
	
	private static String VIEW_CLASS = "android.view.View";
	
	public static IClass getWidgetView(ClassHierarchy cha) {
		if(VIEW != null) {
			return VIEW;
		}
		VIEW = WALAUtils.lookupClass(cha, VIEW_CLASS);
		if(VIEW == null) {
			throw new RuntimeException("Can not load: android.view.View");
		}
		return VIEW;
	}
	
	//FIXME, should it be ClassA$ClassB or ClassA.ClassB? need to confirm?
	//no it should be as follows, since users will not write A$B in their code
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
	
	private static Set<String> getOtherUIs() {
		Set<String> uiSet = new HashSet<String>();
		for(String uiName : otherUIs) {
			uiSet.add(uiName);
		}
		return uiSet;
	}
	
	private static String[] otherUIs = new String[] {
		"android.view.View", 
		"android.view.ViewGroup",
		"android.view.ViewStub",
		"android.view.SurfaceView",
		"android.webkit.WebView",
		"android.gesture.GestureOverlayView",
		"android.appwidget.AppWidgetHostView",
		"android.inputmethodservice.ExtractEditText",
		"android.inputmethodservice.KeyboardView",
		"android.opengl.GLSurfaceView"
	};
	
	
	/***
	 * All Android listener classes
	 * */
	public static List<IClass> getAndroidListenerClasses(ClassHierarchy cha) {
		if(listener_classes != null) {
			return listener_classes;
		}
		listener_classes = new LinkedList<IClass>();
		for(String l : listeners) {
			IClass c = WALAUtils.lookupClass(cha, l);
			if(c == null) {
				throw new RuntimeException("No class loaded for: " + l);
			}
			listener_classes.add(c);
		}
		return listener_classes;
	}
	public static boolean isAndroidListener(String fullClassName) {
		for(String l : listeners) {
			if(l.equals(fullClassName)) {
				return true;
			}
		}
		return false;
	}
	

	public static List<IClass> getAppListenerClasses(ClassHierarchy cha) {
		List<IClass> appListeners = new LinkedList<IClass>();
		
		for(IClass c : cha) {
			if(isCustomizedListener(cha, WALAUtils.getJavaFullClassName(c))) {
				appListeners.add(c);
			}
		}
		
		return appListeners;
	}
	
	public static boolean isCustomizedListener(ClassHierarchy cha, String classFullName) {
		IClass cL = WALAUtils.lookupClass(cha, classFullName);
		if(cL == null) {
			throw new RuntimeException("No class for: " + classFullName);
		}
		if(AndroidUtils.isInAndroidLib(classFullName)) {
			return false;
		}
		List<IClass> androidListeners = getAndroidListenerClasses(cha);
		for(IClass listener : androidListeners) {
			if(cha.isAssignableFrom(listener, cL)) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * This is dumped by searching all classes ending with "Listener" in Android's lib
	 * */
	private static List<IClass> listener_classes = null;
	private static String[] listeners = new String[] {
		"android.view.animation.Animation$AnimationListener",
		"android.view.ViewGroup$OnHierarchyChangeListener",
		"android.view.GestureDetector$OnGestureListener",
		"android.view.ViewTreeObserver$OnGlobalFocusChangeListener",
		"android.view.ViewTreeObserver$OnPreDrawListener",
		"android.view.GestureDetector$SimpleOnGestureListener",
		"android.view.OrientationListener",
		"android.view.View$OnFocusChangeListener",
		"android.view.ScaleGestureDetector$SimpleOnScaleGestureListener",
		"android.view.MenuItem$OnMenuItemClickListener",
		"android.view.ViewStub$OnInflateListener",
		"android.view.View$OnCreateContextMenuListener",
		"android.view.View$OnClickListener",
		"android.view.View$OnTouchListener",
		"android.view.ViewTreeObserver$OnTouchModeChangeListener",
		"android.view.View$OnKeyListener",
		"android.view.ViewTreeObserver$OnScrollChangedListener",
		"android.view.View$OnLongClickListener",
		"android.view.ScaleGestureDetector$OnScaleGestureListener",
		"android.view.GestureDetector$OnDoubleTapListener",
		"android.view.ViewTreeObserver$OnGlobalLayoutListener",
		"android.view.OrientationEventListener",
		"android.media.MediaPlayer$OnSeekCompleteListener",
		"android.media.MediaPlayer$OnInfoListener",
		"android.media.MediaScannerConnection$OnScanCompletedListener",
		"android.media.MediaRecorder$OnInfoListener",
		"android.media.MediaPlayer$OnBufferingUpdateListener",
		"android.media.MediaPlayer$OnVideoSizeChangedListener",
		"android.media.MediaPlayer$OnCompletionListener",
		"android.media.SoundPool$OnLoadCompleteListener",
		"android.media.MediaPlayer$OnErrorListener",
		"android.media.JetPlayer$OnJetEventListener",
		"android.media.AudioTrack$OnPlaybackPositionUpdateListener",
		"android.media.MediaRecorder$OnErrorListener",
		"android.media.MediaPlayer$OnPreparedListener",
		"android.media.AudioManager$OnAudioFocusChangeListener",
		"android.media.AudioRecord$OnRecordPositionUpdateListener",
		"android.text.method.TimeKeyListener",
		"android.text.method.TextKeyListener",
		"android.text.method.MultiTapKeyListener",
		"android.text.method.DateTimeKeyListener",
		"android.text.method.DigitsKeyListener",
		"android.text.method.NumberKeyListener",
		"android.text.method.QwertyKeyListener",
		"android.text.method.KeyListener",
		"android.text.method.MetaKeyKeyListener",
		"android.text.method.DateKeyListener",
		"android.text.method.TextKeyListener$Capitalize",
		"android.text.method.BaseKeyListener",
		"android.text.method.DialerKeyListener",
		"android.preference.PreferenceManager$OnActivityDestroyListener",
		"android.preference.PreferenceManager$OnActivityStopListener",
		"android.preference.PreferenceManager$OnActivityResultListener",
		"android.preference.Preference$OnPreferenceChangeListener",
		"android.preference.Preference$OnPreferenceClickListener",
		"android.content.DialogInterface$OnMultiChoiceClickListener",
		"android.content.DialogInterface$OnDismissListener",
		"android.content.DialogInterface$OnCancelListener",
		"android.content.SharedPreferences$OnSharedPreferenceChangeListener",
		"android.content.DialogInterface$OnKeyListener",
		"android.content.DialogInterface$OnClickListener",
		"android.content.DialogInterface$OnShowListener",
		"android.widget.AdapterView$OnItemLongClickListener",
		"android.widget.PopupWindow$OnDismissListener",
		"android.widget.TabHost$OnTabChangeListener",
		"android.widget.RadioGroup$OnCheckedChangeListener",
		"android.widget.AdapterView$OnItemClickListener",
		"android.widget.Chronometer$OnChronometerTickListener",
		"android.widget.ExpandableListView$OnGroupCollapseListener",
		"android.widget.AbsListView$OnScrollListener",
		"android.widget.TimePicker$OnTimeChangedListener",
		"android.widget.TextView$OnEditorActionListener",
		"android.widget.ExpandableListView$OnGroupClickListener",
		"android.widget.ExpandableListView$OnChildClickListener",
		"android.widget.CompoundButton$OnCheckedChangeListener",
		"android.widget.SlidingDrawer$OnDrawerScrollListener",
		"android.widget.AbsListView$RecyclerListener",
		"android.widget.Filter$FilterListener",
		"android.widget.SeekBar$OnSeekBarChangeListener",
		"android.widget.RatingBar$OnRatingBarChangeListener",
		"android.widget.SlidingDrawer$OnDrawerCloseListener",
		"android.widget.ZoomButtonsController$OnZoomListener",
		"android.widget.ExpandableListView$OnGroupExpandListener",
		"android.widget.AdapterView$OnItemSelectedListener",
		"android.widget.DatePicker$OnDateChangedListener",
		"android.widget.SlidingDrawer$OnDrawerOpenListener",
		"android.app.SearchManager$OnDismissListener",
		"android.app.DatePickerDialog$OnDateSetListener",
		"android.app.SearchManager$OnCancelListener",
		"android.app.TimePickerDialog$OnTimeSetListener",
		"android.speech.tts.TextToSpeech$OnInitListener",
		"android.speech.tts.TextToSpeech$OnUtteranceCompletedListener",
		"android.speech.RecognitionListener",
		"android.gesture.GestureOverlayView$OnGesturePerformedListener",
		"android.gesture.GestureOverlayView$OnGestureListener",
		"android.gesture.GestureOverlayView$OnGesturingListener",
		"android.sax.ElementListener",
		"android.sax.EndElementListener",
		"android.sax.EndTextElementListener",
		"android.sax.TextElementListener",
		"android.sax.StartElementListener",
		"android.webkit.WebView$PictureListener",
		"android.webkit.WebIconDatabase$IconListener",
		"android.webkit.DownloadListener",
		"android.database.sqlite.SQLiteTransactionListener",
		"android.location.GpsStatus$NmeaListener",
		"android.location.LocationListener",
		"android.location.GpsStatus$Listener",
		"android.os.RecoverySystem$ProgressListener",
		"android.telephony.PhoneStateListener",
		"android.hardware.SensorListener",
		"android.hardware.Camera$OnZoomChangeListener",
		"android.hardware.SensorEventListener",
		"android.accounts.OnAccountsUpdateListener",
		"android.inputmethodservice.KeyboardView$OnKeyboardActionListener"
	};
	
	//approximately, but i do believe it works well enough in practice
	public static boolean isEventHandlingMethod(IMethod m, ClassHierarchy cha) {
		String name = m.getName().toString();
		
		//first see the method name
		boolean matchMethod = false;
		for(String e : eventHandlers) {
			if(name.equals(e)) {
				matchMethod = true;
			}
		}
		if(!matchMethod) {
			return false;
		}
		
		//check the class that defines the method
		IClass declaredClass = m.getDeclaringClass();
		String fullName = WALAUtils.getJavaFullClassName(declaredClass);
		if(isAndroidListener(fullName)) {
			return true;
		}
		
		if(isCustomizedListener(cha, fullName)) {
			return true;
		}
		
		return false;
	}
	
	private static String[] eventHandlers = new String[] {
		"onJetEvent",
		"onDateChanged",
		"onAnimationRepeat",
		"onGpsStatusChanged",
		"onChildClick",
		"onItemLongClick",
		"onDataActivity",
		"onGesturePerformed",
		"onScale",
		"onZoom",
		"onProviderEnabled",
		"onLongPress",
		"onUtteranceCompleted",
		"onScroll",
		"onSingleTapConfirmed",
		"onVideoSizeChanged",
		"onRelease",
		"onTimeSet",
		"onRatingChanged",
		"onGestureStarted",
		"onKeyUp",
		"onItemClick",
		"onCellLocationChanged",
		"onSensorChanged",
		"onNmeaReceived",
		"onShowPress",
		"onJetNumQueuedSegmentUpdate",
		"onTabChanged",
		"onGestureCancelled",
		"onJetUserIdUpdate",
		"onAnimationStart",
		"onStartTrackingTouch",
		"onFilterComplete",
		"onAnimationEnd",
		"onScrollChanged",
		"onTouch",
		"onRollback",
		"onCancel",
		"onDrawerOpened",
		"onError",
		"onPeriodicNotification",
		"onEndOfSpeech",
		"onCompletion",
		"onInfo",
		"onScaleBegin",
		"onCallForwardingIndicatorChanged",
		"onFocusChange",
		"onTimeChanged",
		"onMenuItemClick",
		"onDoubleTapEvent",
		"onBufferingUpdate",
		"onCallStateChanged",
		"onMessageWaitingIndicatorChanged",
		"onSignalStrengthChanged",
		"onScrollStateChanged",
		"onAccuracyChanged",
		"onText",
		"onSpanAdded",
		"onDismiss",
		"onReceivedIcon",
		"onPreferenceClick",
		"onCommit",
		"onProgress",
		"onEvent",
		"onGlobalFocusChanged",
		"onGesturingEnded",
		"onChronometerTick",
		"onPartialResults",
		"onBeginningOfSpeech",
		"onGestureEnded",
		"onPreDraw",
		"onNothingSelected",
		"onDrawerClosed",
		"onOrientationChanged",
		"onClick",
		"onAccountsUpdated",
		"onSingleTapUp",
		"onZoomChange",
		"onLocationChanged",
		"onResults",
		"onScrollStarted",
		"onScaleEnd",
		"onLoadComplete",
		"onAudioFocusChange",
		"onChildViewAdded",
		"onMovedToScrapHeap",
		"onGroupExpand",
		"onPress",
		"onKeyDown",
		"onKey",
		"onInflate",
		"onBufferReceived",
		"onDateSet",
		"onPreferenceChange",
		"onSharedPreferenceChanged",
		"onCreateContextMenu",
		"onFling",
		"onDataConnectionStateChanged",
		"onSpanChanged",
		"onBegin",
		"onGlobalLayout",
		"onScrollEnded",
		"onKeyOther",
		"onCheckedChanged",
		"onDownloadStart",
		"onJetPauseUpdate",
		"onDoubleTap",
		"onSeekComplete",
		"onChildViewRemoved",
		"onProviderDisabled",
		"onLongClick",
		"onTouchModeChanged",
		"onSpanRemoved",
		"onEditorAction",
		"onSignalStrengthsChanged",
		"onReadyForSpeech",
		"onNewPicture",
		"onGesture",
		"onGroupClick",
		"onStopTrackingTouch",
		"onScanCompleted",
		"onVisibilityChanged",
		"onProgressChanged",
		"onGesturingStarted",
		"onRmsChanged",
		"onItemSelected",
		"onPrepared",
		"onDown",
		"onInit",
		"onMarkerReached",
		"onActivityResult",
		"onServiceStateChanged",
		"onGroupCollapse",
		"onStatusChanged",
		"onShow",
		"onActivityDestroy",
		"onActivityStop",
		
		//XXX not sure
		"canDetectOrientation",
		"lookup",
		"clearMetaKeyState",
		"ok",
		"getMetaState",
		"markAsReplaced",
		"swipeUp",
		"swipeRight",
		"start",
		"clear",
		"isSelectingMetaTracker",
		"swipeDown",
		"valueOf",
		"end",
		"adjustMetaAfterKeypress",
		"isMetaTracker",
		"resetLockedMeta",
		"handleKeyUp",
		"enable",
		"resetMetaState",
		"handleKeyDown",
		"values",
		"getInstance",
		"release",
		"getInputType",
		"filter",
		"shouldCap",
		"backspace",
		"getAcceptedChars",
		"disable",
		"swipeLeft"
	};
	
	private static String[] activityMethods = new String[] {
		"onCreate",
		"onStart",
		"onRestart",
		"onResume",
		"onPause",
		"onStop",
		"onDestroy",
		"onUserLeaveHint",
		"onTitleChanged",
		"onSaveInstanceState",
		"onRestoreInstanceState",
		"onPrepareDialog",
		"onPostResume",
		"onPostCreate",
		"onNewIntent",
		"onCreateDialog",
		"onChildTitleChanged",
		"onApplyThemeResource",
		"onActivityResult"
	};
	
	public static boolean isActivityCallbackMethod(ClassHierarchy cha, IMethod method) {
		IClass declaringClass = method.getDeclaringClass();
		if(!AndroidUtils.isAppActivity(cha, declaringClass)) {
			throw new RuntimeException("method: " + method + ", in class: " + declaringClass + " is not an activity class.");
		}
		String methodName = method.getName().toString();
		return Utils.includedIn(methodName, activityMethods);
	}
}