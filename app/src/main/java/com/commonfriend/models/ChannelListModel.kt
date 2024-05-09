package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ChannelListModel : Serializable {

    @Expose
    @SerializedName("channel_id")
    var channelId: ArrayList<ChannelListModel> = ArrayList()

}