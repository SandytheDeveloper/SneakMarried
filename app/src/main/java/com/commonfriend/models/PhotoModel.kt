package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PhotoModel : Serializable {



    @Expose
    @SerializedName("photo")
    var photo: String = ""

    @Expose
    @SerializedName("crop_photos_details")
    var cropPhotosDetails: ArrayList<MultipleFaceModel>  =ArrayList()

    @Expose
    @SerializedName("message")
    var message : String = ""

    @Expose
    @SerializedName("sub_message")
    var subMessage : String = ""

    @Expose
    @SerializedName("is_valid_pic")
    var isValidPic : String = ""


    @Expose
    @SerializedName("hand_written_texts")
    var handWrittenTexts : ArrayList<String> = ArrayList()


    @Expose
    @SerializedName("validation_found")
    var validationFound : String = ""




}