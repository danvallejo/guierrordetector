package edu.washington.cs.detector.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import com.ibm.wala.ipa.cha.ClassHierarchy;

public class AndroidLogicByPassFileCreator {
	
	//note, this output dir MUST be in class path
	private String defaultOutputDir = "." + Globals.fileSep + "dat";
	
	private final String layoutDir;
	private final ClassHierarchy cha;
	
	public AndroidLogicByPassFileCreator(String layoutDir, ClassHierarchy cha) {
		this.layoutDir = layoutDir;
		this.cha = cha;
	}

	//create the bypass logic file as: defaultDir / fileName
	public void createByPassLogicFile(String fileName) {
		if(layoutDir == null || cha == null) {
			throw new RuntimeException("The layout dir and cha should not be null.");
		}
		try {
			Collection<String> fullUIClassNames = AndroidUtils.extractAllUIs(this.cha, new File(this.layoutDir));
			createByPassLogicFile(fileName, fullUIClassNames);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	//java full names: a.b.c.d
	public void createByPassLogicFile(String fileName, Collection<String> javaFullNames) throws IOException {
		
		if(!fileName.endsWith(".xml")) {
			throw new RuntimeException(fileName + " must be an XML file.");
		}
		
		Collection<String> walaClassNames = new LinkedList<String>();
		for(String javaFullName : javaFullNames) {
			walaClassNames.add(WALAUtils.javaClassToWalaClass(javaFullName));
		}
		StringBuilder fileContent = new StringBuilder();
		fileContent.append(this.createStartElement());
		fileContent.append(this.createReturnElements(walaClassNames));
		fileContent.append(this.createEndElement());
		
		//write to the file
		Files.writeToFile(fileContent.toString(), new File(this.defaultOutputDir + Globals.fileSep + fileName));
	}
	
	protected String createStartElement() {
		/*
		 * <summary-spec>
             <classloader name="Application">
	           <package name="android/view">
                 <class name="View">
				    <method name="findViewById" descriptor="(I)Landroid/view/View;" static="false">
		 * */
		StringBuilder sb = new StringBuilder();
		
		sb.append("<summary-spec>");
		sb.append(Globals.lineSep);
		sb.append("    <classloader name=\"Application\">");
		sb.append(Globals.lineSep);
		sb.append("        <package name=\"android/app\">");
		sb.append(Globals.lineSep);
		sb.append("            <class name=\"Activity\">");
		sb.append(Globals.lineSep);
		sb.append("                <method name=\"findViewById\" descriptor=\"(I)Landroid/view/View;\" static=\"false\">");
		sb.append(Globals.lineSep);
		
		return sb.toString();
	}
	
	//note the name should already been transformed well: La/b/c/classname
	protected String createReturnElements(Collection<String> fullWalaClassNames) {
		StringBuilder sb = new StringBuilder();
		/*
		 * <new def="x" class="Landroid/widget/Button" />
		   <return value="x"/>
		 * */
		int index = 0;
		for(String walaClassName : fullWalaClassNames) {
			String retValue = "x" + (index++);
			sb.append("                    <new def=\"" + retValue
					+ "\" class=\"" +  walaClassName.trim() + "\"/>");
			sb.append(Globals.lineSep);
			sb.append("                    <return value=\"" + retValue + "\"/>");
			sb.append(Globals.lineSep);
		}
		
		return sb.toString();
	}
	
	protected String createEndElement() {
		/*
		 *     </method>
			</class>
	      </package>
        </classloader>
      </summary-spec>
		 * */
		StringBuilder sb = new StringBuilder();
		sb.append("                </method>");
		sb.append(Globals.lineSep);
		sb.append("            </class>");
		sb.append(Globals.lineSep);
		sb.append("        </package>");
		sb.append(Globals.lineSep);
		sb.append("    </classloader>");
		sb.append(Globals.lineSep);
		sb.append("</summary-spec>");
		sb.append(Globals.lineSep);
		
		return sb.toString();
	}
	
	public void setDefaultDir(String defaultDir) {
		this.defaultOutputDir = defaultDir;
	}
	
	public String getDefaultDir() {
		return this.defaultOutputDir;
	}
	
	public String getLayoutDir() {
		return this.layoutDir;
	}
	
	public ClassHierarchy getClassHierarchy() {
		return this.cha;
	}
	
	@Deprecated
	public static void main(String[] args) throws IOException {
		AndroidLogicByPassFileCreator creator = new AndroidLogicByPassFileCreator(null, null);
		Collection<String> walaNames = new LinkedList<String>();
		walaNames.add("Landroid/widget/Button");
		
		System.out.println(creator.createStartElement());
		System.out.println(creator.createReturnElements(walaNames));
		System.out.println(creator.createEndElement());
		
		Collection<String> javaClassNames = new LinkedList<String>();
		javaClassNames.add("android.widget.Button");
		javaClassNames.add("android.widget.TextView");
		creator.createByPassLogicFile("androidsamplereflection-test.xml", javaClassNames);
	}
}