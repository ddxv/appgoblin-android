package dev.thirdgate.appgoblin.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AppInfo(
    val name: String,
    val packageName: String,
    var isSelected: Boolean = false,
    @Transient
    val appIcon: ImageBitmap? = null
) : Parcelable {
    // Tell Android how to create this object from a Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt() == 1,
        null // Don't include the image
    )

    // Tell Android how to write this object to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(packageName)
        parcel.writeInt(if (isSelected) 1 else 0)
        // Don't write the image
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AppInfo> {
        override fun createFromParcel(parcel: Parcel): AppInfo = AppInfo(parcel)
        override fun newArray(size: Int): Array<AppInfo?> = arrayOfNulls(size)
    }
}