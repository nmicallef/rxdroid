
package at.caspase.rxdroid.test;

import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.test.AndroidTestCase;
import android.util.Log;
import at.caspase.rxdroid.ContextStorage;
import at.caspase.rxdroid.Preferences;
import at.caspase.rxdroid.Database.Drug;
import at.caspase.rxdroid.util.DateTime;

/**
 * Tests for the time handling methods from the Preferences class.
 * <p>
 * For the purpose of this test, the dose times are guaranteed to be the
 * following:
 * 
 * <pre>
 * Morning: 06:00-10:00
 * Evening: 18:00-21:00
 * Night  : 23:00-01:30
 * </pre>
 * 
 * The times not listed above cannot be relied upon and should not be used.
 * 
 * @author Joseph Lehner
 * 
 */
public class PreferencesTest extends AndroidTestCase
{
	private static final String TAG = PreferencesTest.class.getName();
	
	private SharedPreferences mPrefs;
	private Map<String, String> mPrefBackup;
	
	public void testGetActiveDoseTimeAndGetNextDoseTime()
	{
		final int[][] testCases = {
				{      18, 00, Drug.TIME_EVENING, Drug.TIME_NIGHT },
				{      21, 00, -1, Drug.TIME_NIGHT },
				{      23, 00, Drug.TIME_NIGHT, Drug.TIME_MORNING },
				{ 24 + 00, 00, Drug.TIME_NIGHT, Drug.TIME_MORNING },
				{ 24 + 00, 30, Drug.TIME_NIGHT, Drug.TIME_MORNING },
				{ 24 + 01, 30, -1, Drug.TIME_MORNING }
		};
		
		for(int i = 0; i != testCases.length; ++i)
		{
			final int hours        = testCases[i][0];
			final int minutes      = testCases[i][1];
			final int doseTime     = testCases[i][2];
			final int nextDoseTime = testCases[i][3];
						
			final Time time = new Time(DateTime.today().getTime() + ((hours * 60L + minutes) * 60000L));
			final Date date = new Date(time.getTime());
			final Preferences prefs = Preferences.instance();
			
			Log.d(TAG, "testGetActiveDoseTimeAndGetNextDoseTime:");
			Log.d(TAG, "  date/time   : " + date + ", " + time);
			Log.d(TAG, "  doseTime    : " + doseTime);
			Log.d(TAG, "  nextDoseTime: " + nextDoseTime + "\n");
						
			assertEquals(doseTime, prefs.getActiveDoseTime(time));
			Log.d(TAG, "  [OK] getActiveDoseTime");
			assertEquals(nextDoseTime, prefs.getNextDoseTime(time));
			Log.d(TAG, "  [OK] getNextDoseTime");
			Log.d(TAG, "-----------------------------");			
		}
	}
	
	public void testTimeOffsets()
	{		
		final long[][] testCases = {
				{      18, 00, -1, 10800000 }, // (21:00 - 18:01) = 180min in millis
				{      21, 00, 7200000, -1 }, // (23:00 - 21:00) = 120min in millis
				{      23, 00, 0, 9000000 }, // ((24 + 01:30) - 23:00 = 150min in millis
				{ 24 + 00, 00, -1, 5400000 }, // (01:30 - 00:00) = 90min in millis
				{ 24 + 00, 30, -1, 3600000 }, // (01:30 - 00:30) = 60min in millis
				{ 24 + 01, 30, 16200000, -1 } // (06:00 - 01:30) = 270min in millis
		};
		
		for(int i = 0; i != testCases.length; ++i)
		{
			final long hours            = testCases[i][0];
			final long minutes          = testCases[i][1];
			final long millisUntilBegin = testCases[i][2];
			final long millisUntilEnd   = testCases[i][3];
						
			final Time time = new Time(DateTime.today().getTime() + ((hours * 60L + minutes) * 60000L));
			final Date date = new Date(time.getTime());
			final Preferences prefs = Preferences.instance();

			final int activeOrNextDoseTime = prefs.getActiveOrNextDoseTime(time);
			
			Log.d(TAG, "testTimeOffsets:");
			Log.d(TAG, "  date/time           : " + date + ", " + time);
			Log.d(TAG, "  hours               : " + hours);
			Log.d(TAG, "  minutes             : " + minutes);
			Log.d(TAG, "  millisUntilBegin    : " + millisUntilBegin);
			Log.d(TAG, "  millisUntilEnd      : " + millisUntilEnd);
			Log.d(TAG, "  activeOrNextDoseTime: " + activeOrNextDoseTime);
			
			if(millisUntilBegin != -1) 
			{
				assertEquals(millisUntilBegin, prefs.getMillisUntilDoseTimeBegin(time, activeOrNextDoseTime));
				Log.d(TAG, "  [OK] getMillisUntilDoseTimeBegin");
			}
			else
				Log.d(TAG, "  [N/A] getMillisUntilDoseTimeBegin");
			
			if(millisUntilEnd != -1)
			{
				assertEquals(millisUntilEnd, prefs.getMillisUntilDoseTimeEnd(time, activeOrNextDoseTime));
				Log.d(TAG, "  [OK] getMillisUntilDoseTimeEnd");
			}
			else
				Log.d(TAG, "  [N/A] getMillisUntilDoseTimeEnd");
			
			Log.d(TAG, "-----------------------------");		
		}
	}

	@Override
	protected void setUp()
	{
		try
		{
			super.setUp();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		ContextStorage.set(mContext);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mPrefBackup = new HashMap<String, String>();

		for (String key : mPrefs.getAll().keySet())
		{
			if (key.startsWith("time_"))
				mPrefBackup.put(key, mPrefs.getString(key, null));
		}

		Editor e = mPrefs.edit();

		e.putString("time_morning_begin", "06:00");
		e.putString("time_morning_end", "10:00");
		
		e.putString("time_evening_begin", "18:00");
		e.putString("time_evening_end", "21:00");

		e.putString("time_night_begin", "23:00");
		e.putString("time_night_end", "01:30");

		e.commit();
	}

	@Override
	protected void tearDown()
	{
		try
		{
			super.tearDown();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		Editor e = mPrefs.edit();

		for (String key : mPrefBackup.keySet())
			e.putString(key, mPrefBackup.get(key));

		e.commit();
	}
}
