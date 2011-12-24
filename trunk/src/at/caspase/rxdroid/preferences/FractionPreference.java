/**
 * Copyright (C) 2011 Joseph Lehner <joseph.c.lehner@gmail.com>
 *
 * This file is part of RxDroid.
 *
 * RxDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RxDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RxDroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package at.caspase.rxdroid.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.util.AttributeSet;
import android.util.Log;
import at.caspase.rxdroid.Fraction;
import at.caspase.rxdroid.FractionInputDialog2;
import at.caspase.rxdroid.preferences.StateSaver.SaveState;

/**
 * A preference for storing fractions.
 *
 * @author Joseph Lehner
 *
 */
public class FractionPreference extends DialogPreference implements 
		FractionInputDialog2.OnFractionSetListener, OnActivityDestroyListener, OnDismissListener
{
	private static final String TAG = FractionPreference.class.getName();

	@SaveState
	Fraction mValue;
	@SaveState
	Fraction mLongClickSummand;
	@SaveState
	boolean mIsShowingDialog = false;
		
	//FractionInputDialog2 mDialog;
	
	public FractionPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FractionPreference(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);

		mValue = Fraction.decode(getPersistedString("0"));
	}

	public void setValue(Fraction value) 
	{
		mValue = value;
		
		if(super.getSummary() == null)
			setSummary(value.toString());
	}

	public Fraction getValue() {
		return mValue;
	}

	public void setLongClickSummand(Fraction value) {
		mLongClickSummand = value;
	}
	
	@Override
	public CharSequence getSummary()
	{
		CharSequence summary = super.getSummary();
		if(summary == null)
			return mValue.toString();
		
		return summary;
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		if(which == DialogInterface.BUTTON_NEUTRAL)
		{
			FractionInputDialog2 myDialog = (FractionInputDialog2) dialog;
			onFractionSet(myDialog, myDialog.getValue().add(mLongClickSummand));
		}
	}
	
	@Override
	public void onFractionSet(FractionInputDialog2 dialog, Fraction value)
	{
		mValue = value;
		
		boolean canPersist = callChangeListener(mValue);
		if(canPersist && shouldPersist())
			persistString(mValue.toString());
		
		notifyChanged();
	}
	
	@Override
	protected void showDialog(Bundle state)
	{
		mIsShowingDialog = true;
		
		// XXX
		FractionInputDialog2 mDialog = new FractionInputDialog2(getContext(), mValue, this);
		
		String middleButtonText = getMiddleButtonText();
		if(middleButtonText != null)		
			mDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getMiddleButtonText(), this);
		
		mDialog.setTitle(getDialogTitle());
		mDialog.setIcon(android.R.drawable.ic_dialog_dialer);
		mDialog.setOnFractionSetListener(this);
		mDialog.setOnDismissListener(this);
		/*mDialog.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				mIsShowingDialog = false;
			}
		});*/
		
		mDialog.show();
	}
	
	@Override
	protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
		// do nothing
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		String value = a.getString(index);
		Log.d(TAG, "onGetDefaultValue: value=" + value);
		return value != null ? value : "0";
	}
	
	@Override
	protected Parcelable onSaveInstanceState()
	{
		Parcelable superState = super.onSaveInstanceState();		
		return StateSaver.saveState(this, superState, null);
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state)
	{
		super.onRestoreInstanceState(StateSaver.getSuperState(state));		
		StateSaver.restoreState(this, state);
		
		if(mIsShowingDialog)
			showDialog(null);
	}
	
	private String getMiddleButtonText()
	{
		if(mLongClickSummand != null && !mLongClickSummand.isZero())		
			return mLongClickSummand.isNegative() ? "" : "+" + mLongClickSummand;
				
		return null;
	}
}
