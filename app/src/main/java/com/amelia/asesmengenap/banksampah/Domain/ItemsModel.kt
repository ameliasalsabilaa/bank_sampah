package com.amelia.asesmengenap.banksampah.Domain

import android.os.Parcel
import android.os.Parcelable

data class ItemsModel(
    var itemId: String = "",
    var title: String = "",
    var description: String = "",
    var picUrl: MutableList<String> = ArrayList(),
    var price: Int = 0, // Mengubah tipe data dari Double menjadi Int
    var numberInCart: Int = 0,
    var showRecommended: Boolean = false,
    var categoryId: String = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.createStringArrayList() as ArrayList<String>,
        parcel.readInt(), // Ubah dari readDouble() menjadi readInt()
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemId)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeStringList(picUrl)
        parcel.writeInt(price) // Ubah dari writeDouble() menjadi writeInt()
        parcel.writeInt(numberInCart)
        parcel.writeByte(if (showRecommended) 1 else 0)
        parcel.writeString(categoryId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ItemsModel> {
        override fun createFromParcel(parcel: Parcel): ItemsModel {
            return ItemsModel(parcel)
        }

        override fun newArray(size: Int): Array<ItemsModel?> {
            return arrayOfNulls(size)
        }
    }
}
