package se.gazab.pebblekeep;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

public class Pebble {
	public static final int FULLSCREEN_TEXT_LIMIT = 3000;
	
	public static void notify(Context context, String title, String text)
	{
		Intent intent = new Intent("com.getpebble.action.SEND_NOTIFICATION");
		
		final Map<String, String> data = new HashMap<String, String>();
	    data.put("title", fixSumnike(title));
	    data.put("body", fixSumnike(text));
	   
	    final JSONObject jsonData = new JSONObject(data);
	    final String notificationData = new JSONArray().put(jsonData).toString();

	    intent.putExtra("messageType", "PEBBLE_ALERT");
	    intent.putExtra("sender", "PebbleToolkit");
	    intent.putExtra("notificationData", notificationData);
		
		context.sendBroadcast(intent);
	}
	
	public static String fixSumnike(String data)
	{
		data = data.replace("�", "c");
		data = data.replace("�", "C");
		//data = data.replace("ž", "z");
		//data = data.replace("š", "s");
		return data;
	}
	
	public static String prepareString(String text, int length)
	{
		text = text.trim();
		if (text.length() > length)
		{
			text = text.substring(0, length - 3).trim().concat("...");

		}
		text = Pebble.fixSumnike(text);
		return text;
	}
}
