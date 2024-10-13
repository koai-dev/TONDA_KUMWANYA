package in.gabsinfo.usertimeslotgeofenceattendance.utils;

import android.content.Context;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AppendLog {
	public void appendLog(Context context, String text, String fileName) {
		String path;
		path = CommonMethods.getApplicationDirectoryForLog(
				CommonMethods.SubDirectory.APP_LOG_DIRECTORY, context, true)
				.getAbsolutePath();
		File logFile = new File(path + "/" + fileName);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void deleteLog(Context context, String fileName) {
		String path;
		path = CommonMethods.getApplicationDirectoryForLog(
				CommonMethods.SubDirectory.APP_LOG_DIRECTORY, context, true)
				.getAbsolutePath();
		File logFile = new File(path + "/" + fileName);
		if (logFile.exists()) {
			try {
				logFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



}