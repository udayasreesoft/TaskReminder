package com.udayasreesoft.businesslibrary.models

import android.os.Parcel
import android.os.Parcelable

data class AmountViewModel(val title : String, val total : Int) : Parcelable  {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeInt(total)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AmountViewModel> {
        override fun createFromParcel(parcel: Parcel): AmountViewModel {
            return AmountViewModel(parcel)
        }

        override fun newArray(size: Int): Array<AmountViewModel?> {
            return arrayOfNulls(size)
        }
    }
}