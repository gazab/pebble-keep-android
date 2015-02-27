package se.gazab.pebblekeep.keepdata;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.util.ArrayList;
import java.util.List;

import se.gazab.pebblekeep.DataReceiver;
import se.gazab.pebblekeep.Pebble;

public class ListNote extends KeepItem {
	private String title;
	private List<String> items;
    private String note = "asdasd";
	
	private String trimmedText;
	
	public ListNote(String title, int parentId, SQLiteDatabase db)
	{
		items = new ArrayList<String>();
		Cursor cursor = db.rawQuery("SELECT text,is_checked FROM list_item WHERE list_parent_id = ? ORDER BY order_in_parent DESC", new String[] { Integer.toString(parentId) });

        while (cursor.moveToNext())
		{
            boolean checked = cursor.getInt(1) == 1;
			String text = cursor.getString(0);
			
			if (checked)
				text = "+|" + text;
			else
                text = "-|" + text;
	
			items.add(text);
		}
		
		if (title == null || title.trim().length() == 0)
			title = "Untitled list";

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

		data.addInt8(0, (byte) 2);
		data.addUint16(1, location);
		data.addUint16(2,  length);

		String subString = trimmedText.substring(location, location + length);
		data.addString(4, subString);

		PebbleKit.sendDataToPebble(context, DataReceiver.keepUUID, data);
	}

    private void sendListPart(Context context, int index)
    {
        //0 = ID
        //1 = Index
        //2 = Max
        //3 = Text 1
        //4 = Text 2
        //5 = Text 3

        PebbleDictionary data = new PebbleDictionary();

        data.addInt8(0, (byte) 2);
        data.addUint8(1, (byte) index);
        data.addUint8(2, (byte) items.size());

        for (int i = 0; i < 3; i++)
        {
            int entry = index + i;
            if (entry > items.size() - 1)
                continue;

            data.addString(3 + i, Pebble.prepareString(items.get(entry), 20));
        }

        PebbleKit.sendDataToPebble(context, DataReceiver.keepUUID, data);
    }
	
	@Override
	public void noteOpened(Context context) {
		sendListPart(context, 0);
	}

	@Override
	public void dataReceived(PebbleDictionary data, Context context) {		
		int location = data.getUnsignedInteger(1).intValue();
		sendNote(context, location);

	}
}
