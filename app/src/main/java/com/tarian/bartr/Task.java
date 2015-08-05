package com.tarian.bartr;

import com.google.android.gms.maps.model.LatLng;

import java.util.UUID;

/**
 * Created by Tommy on 7/31/2015.
 */
public class Task {
    UUID mId;
    String mItem;
    long mMaxPrice;
    long mBounty;
    String mNotes;
    LatLng mLocation;

    public Task(final String item, final long price, final long bounty, final String notes, final LatLng location) {
        this.mItem = item;
        this.mMaxPrice = price;
        this.mBounty = bounty;
        this.mNotes = notes;
        this.mLocation = location;
    }

    public void generateNewUUID() {
        this.mId = UUID.randomUUID();
    }

    public String[] getFields() {
        final String[] fields = new String[5];
        fields[0] = mItem;
        fields[1] = Long.toString(mMaxPrice);
        fields[2] = Long.toString(mBounty);
        fields[3] = mNotes;
        fields[4] = latLngToString(mLocation);
        return fields;
    }

    public String[] getFieldsWithId() {
        final String[] fieldsWithoutId = getFields();
        final String[] fields = new String[6];
        fields[0] = mId.toString();
        for (int i = 0; i < fieldsWithoutId.length; ++i) {
            fields[i+1] = fieldsWithoutId[i];
        }
        return fields;
    }

    public String latLngToString(final LatLng location) {
        final String locationString =
                Double.toString(location.latitude) +
                "," +
                Double.toString(location.longitude);
        return locationString;
    }
}
