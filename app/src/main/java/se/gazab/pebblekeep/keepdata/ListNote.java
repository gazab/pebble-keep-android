package se.gazab.pebblekeep.keepdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import se.gazab.pebblekeep.DataReceiver;
import se.gazab.pebblekeep.Pebble;

public class ListNote extends KeepItem {
	private String title;
	private String note;
	
	private String trimmedText;
	
	public ListNote(String title, int parentId, SQLiteDatabase db)
	{
		
		Cursor cursor = db.rawQuery("SELECT text,is_checked FROM list_item WHERE list_parent_id = ? ORDER BY order_in_parent DESC", new String[] { Integer.toString(parentId) });
		
		note = "";
		
		while (cursor.moveToNext())
		{
			boolean checked = cursor.getInt(1) == 1;
			String text = cursor.getString(0);
			
			if (checked)
				note += "+ ";
			else 
				note += "- ";
	
			note += text;
			
			note += "\n";
		}
		
		note = note.substring(0, note.length() - 1);
				
		if (title == null || title.trim().length() == 0)
			title = note;
		else
			note = title + "\n\n" + note;

		this.title = "L|" + title;

		cursor.close();
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	private void sendNote(Context context, int location)
	{
		int length = Math.min(trimmedText.length() - location, 75);

		PebbleDictionary data = new PebbleDictionary();

		data.addInt8(0, (byte) 1); 
		data.addUint16(1, location);
		data.addUint16(2,  length);

		String subString = trimmedText.substring(location, location + length);
		data.addString(4, subString);

		PebbleKit.sendDataToPebble(context, DataReceiver.keepUUID, data);
	}
	
	@Override
	public void noteOpened(Context context) {
		trimmedText = Pebble.prepareString(note, Pebble.FULLSCREEN_TEXT_LIMIT);
		
		sendNote(context, 0);
	}

	@Override
	public void dataReceived(PebbleDictionary data, Context context) {		
		int location = data.getUnsignedInteger(1).intValue();
		sendNote(context, location);

	}
}
