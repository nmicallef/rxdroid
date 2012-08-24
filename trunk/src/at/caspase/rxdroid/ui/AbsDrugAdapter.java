/**
 * Copyright (C) 2011, 2012 Joseph Lehner <joseph.c.lehner@gmail.com>
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

package at.caspase.rxdroid.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import at.caspase.rxdroid.DoseView;
import at.caspase.rxdroid.db.Drug;
import at.caspase.rxdroid.util.CollectionUtils;
import at.caspase.rxdroid.widget.Rot13TextView;

public abstract class AbsDrugAdapter extends ArrayAdapter<Drug>
{
	private final ArrayList<Drug> mAllItems;

	protected final Activity mActivity;
	protected ArrayList<Drug> mItems;
	protected final Date mAdapterDate;

	public AbsDrugAdapter(Activity activity, List<Drug> items, Date date)
	{
		super(activity.getApplicationContext(), 0, items);

		mActivity = activity;
		mAllItems = mItems = new ArrayList<Drug>(items);
		mAdapterDate = date;
	}

	public void setFilter(CollectionUtils.Filter<Drug> filter)
	{
		if(filter != null)
			mItems = (ArrayList<Drug>) CollectionUtils.filter(mAllItems, filter);
		else
			mItems = mAllItems;

		notifyDataSetChanged();
	}

	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);

	@Override
	public Drug getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public int getPosition(Drug drug) {
		return mItems.indexOf(drug);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public boolean isEmpty() {
		return mItems.isEmpty();
	}

	static class DoseViewHolder
	{
		Rot13TextView name;
		ImageView icon;
		DoseView[] doseViews = new DoseView[4];
		View missedDoseIndicator;
		View lowSupplyIndicator;
		TextView info1;
		TextView info2;
		View[] dividers = new View[3];
	}
}
