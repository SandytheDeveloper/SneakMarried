package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class ImageModel : Serializable {

    @Expose
    @SerializedName("id")
    var id: Int = 0

    @Expose
    @SerializedName("index")
    var index: Int = 0

    @Expose
    @SerializedName("count")
    var count: String = ""

    @Expose
    @SerializedName("main_image")
    var mainImage: String = ""

    @Expose
    @SerializedName("is_lock")
    var isLock: String = ""


    @Expose
    @SerializedName("is_changed")
    var isChanged : String = ""

    @Expose
    @SerializedName("same_main_image")
    var sameMainImage: String = ""

    @Expose
    @SerializedName("old_image")
    var oldImage: String = ""

    @Expose
    @SerializedName("image_name")
    var imageName: String = ""

    @Expose
    @SerializedName("image_size")
    var imageSize: String = ""

    @Expose
    @SerializedName("ratio_1")
    var ratio1: String = ""

    @Expose
    @SerializedName("ratio_2")
    var ratio2: String = ""

    @Expose
    @SerializedName("quality")
    var quality: String = ""

    @Expose
    @SerializedName("album_screen_type")
    var albumScreenType: String = "" // 1 - FourPhoto , 2 - Single Photo

    @Expose
    @SerializedName(value = "crop_image", alternate = ["image", "img_url","profile_pic"])
    var imgUrl: String = ""


    @Expose
    @SerializedName("from_group_image")
    var fromGroupImage: String = ""

    @Expose
    @SerializedName("is_profile_pic")
    var isProfilePic: String = ""

    @Expose
    @SerializedName("is_valid_display_pic")
    var isValidDisplayPic: String = ""

    @Expose
    @SerializedName("error_message")
    var errorMessage: String = ""

    @Expose
    @SerializedName("sub_message")
    var subMessage: String = ""

    @Expose
    @SerializedName("hand_written_texts")
    var handWrittenTexts: ArrayList<String> = ArrayList()


    @Expose
    @SerializedName("dialog")
    var dialog: String = ""

    @Expose
    @SerializedName("isUploadByDoctor")
    var isUploadByDoctor: String = ""      // 0 = Patient, 1 = Doctor

    @Expose
    @SerializedName("is_selected")
    var isSelected : Int = 0

    @Expose
    @SerializedName("photo_album")
    var photoAlbum : ArrayList<ImageModel> = ArrayList()

    @Expose
    @SerializedName("is_shield")
    var isShield : String = "" // 0 - hide , 1 - visible

    @Expose
    @SerializedName("info_message")
    var infoMessage : String = ""
}
