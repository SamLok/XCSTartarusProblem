package quirl.utils;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class Logger {
	public static enum LogLevel{
		MESSAGE,
		DEBUG,
		ERROR;
	}

	private static Set<LogLevel> enabled = new HashSet<LogLevel>();

	public static void redirect(String file){
		try {
			System.setOut(new PrintStream(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void enableLevel(LogLevel l){
		enabled.add(l);
	}

	public static void disableLevel(LogLevel l){
		enabled.remove(l);
	}

	public static void logError(String s, Object... args){
		log(LogLevel.ERROR, s+"%n", args);
	}

	public static void logDebug(String s, Object... args){
		log(LogLevel.DEBUG, s+"%n", args);
	}

	public static void logMessage(String s, Object... args){
		log(LogLevel.MESSAGE, s+"%n", args);
	}

	private static void log(LogLevel level, String s, Object... args){
		if(enabled.contains(level)){
			if(level == LogLevel.ERROR){
				System.err.printf(s, args);
			}
			else{
				System.out.printf(s, args);
			}
		}
	}
}
