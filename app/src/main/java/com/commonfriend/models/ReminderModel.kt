package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ReminderModel: Serializable {

    @Expose
    @SerializedName("reminder_type") // reminder_type = 1 for penalty, 2 for upload photos,  3 for ans min ques, 4 for changes rejected, 5 for changes accepted, 6 for complete profile, 7 for adhar verificaton pending, 8 for Pending steps screen
    var reminderType: String = ""

    @Expose
    @SerializedName("time")
    var time: String = ""

    @Expose
    @SerializedName("reminder")
    var reminderNotify: String = ""

    @Expose
    @SerializedName("description")
    var reminderInfo: String = ""

    @Expose
    @SerializedName("font_change")
    var fontChange: String = ""
}