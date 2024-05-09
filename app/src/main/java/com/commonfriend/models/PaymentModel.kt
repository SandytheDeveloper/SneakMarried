package com.commonfriend.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PaymentModel : Serializable {

    @Expose
    @SerializedName("coins_list")
    var coinsList : ArrayList<PaymentModel> = ArrayList()

    @Expose
    @SerializedName("coins")
    var coins : Int = 0

    @Expose
    @SerializedName("is_selected")
    var isSelected : Boolean = false

    @Expose
    @SerializedName("transaction_coin")
    var transactionCoin : String = ""

    @Expose
    @SerializedName("transaction_date")
    var transactionDate : String = ""

    @Expose
    @SerializedName("transaction_title")
    var transactionTitle : String = ""

    @Expose
    @SerializedName("transaction_desc")
    var transactionDesc : String = ""

    @Expose
    @SerializedName("is_coin_credited")
    var isCoinCredited : Boolean = true



}