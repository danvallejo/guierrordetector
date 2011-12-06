package edu.washington.cs.detector.experiments.android;

import java.io.IOException;

public class TestMytracksAndroid extends AbstractAndroidTest {

	@Override
	protected String getAppPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getDirPath() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void testUnzip() throws IOException {
		String apkToolDir = "D:\\develop-tools\\apktool";
		String apkFile = "D:\\research\\guierror\\subjects\\android-programs\\MyTracks-1.1.11.rc1.apk";
		String extractDir = "D:\\research\\guierror\\subjects\\android-programs\\extracted\\MyTracks-1.1.11.rc1.apk";
		super.decryptXML(apkToolDir, apkFile, extractDir);
	}

}
