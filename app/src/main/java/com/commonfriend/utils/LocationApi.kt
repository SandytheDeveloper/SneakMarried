package com.commonfriend.utils

import android.content.Context
import com.commonfriend.R
import com.commonfriend.models.GeneralModel
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

class LocationApi(var type: String,var context : Context) {

    private val apiKey = context.getString(R.string.location_api_key)

    fun autoComplete(input: String): ArrayList<GeneralModel> {
        var resultList: ArrayList<GeneralModel> = ArrayList()
        var conn: HttpURLConnection? = null
        val jsonResults = StringBuilder()
        try {
            val sb = StringBuilder("https://maps.googleapis.com/maps/api/place/autocomplete/json")
            sb.append(
                "?input=" + URLEncoder.encode(
                    input,
                    "utf8"
                ) + "&sensor=false&key=" + apiKey + "&language=en&type=$type"
            )
            Util.print("=========== URL :: $sb")//
            val url = URL(sb.toString())
            conn = url.openConnection() as HttpURLConnection
            val `in` = InputStreamReader(conn.inputStream)
            var read: Int
            val buff = CharArray(1024)
            while (`in`.read(buff).also { read = it } != -1) {
                jsonResults.append(buff, 0, read)
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return resultList
        } catch (e: IOException) {
            e.printStackTrace()
            return resultList
        } finally {
            conn?.disconnect()
        }
        try { // Create a JSON object hierarchy from the results
            val jsonObj = JSONObject(jsonResults.toString())
            val predsJsonArray = jsonObj.getJSONArray("predictions")
            Util.print("===Predection Obj>>>>>>>>>>>>>>>> >> $jsonObj")
            // Extract the Place descriptions from the results
            resultList = ArrayList(predsJsonArray.length())
            for (i in 0 until predsJsonArray.length()) {
                val addressModel = GeneralModel()
                addressModel.description = predsJsonArray.getJSONObject(i).getString("description")
                addressModel.placeId = predsJsonArray.getJSONObject(i).getString("place_id")
                addressModel.address =
                    predsJsonArray.getJSONObject(i).getJSONObject("structured_formatting")
                        .getString("main_text")

                if (predsJsonArray.getJSONObject(i).getJSONArray("terms").length() == 1) {
                    addressModel.isCountry = true
                    addressModel.country =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(0)
                            .getString("value").trim()
                    addressModel.name =  addressModel.country

                } else if (predsJsonArray.getJSONObject(i).getJSONArray("terms").length() == 2) {
                    addressModel.isState = true
                    addressModel.country =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(1)
                            .getString("value").trim()
                    addressModel.state =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(0)
                            .getString("value").trim()

                    addressModel.name =  addressModel.state

                } else if (predsJsonArray.getJSONObject(i).getJSONArray("terms").length() == 3) {
                    addressModel.isCity = true
                    addressModel.country =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(2)
                            .getString("value").trim()
                    addressModel.state =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(1)
                            .getString("value").trim()
                    addressModel.city =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(0)
                            .getString("value").trim()

                    addressModel.name =  addressModel.city

                } else if (predsJsonArray.getJSONObject(i).getJSONArray("terms").length() == 4) {
                    addressModel.isArea = true
                    addressModel.country =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(3)
                            .getString("value").trim()
                    addressModel.state =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(2)
                            .getString("value").trim()
                    addressModel.city =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(1)
                            .getString("value").trim()
                    addressModel.area =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(0)
                            .getString("value").trim()

                    addressModel.name = addressModel.area
                }else if (predsJsonArray.getJSONObject(i).getJSONArray("terms").length() == 5) {
                    addressModel.isArea = true
                    addressModel.country =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(4)
                            .getString("value").trim()
                    addressModel.state =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(3)
                            .getString("value").trim()
                    addressModel.city =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(2)
                            .getString("value").trim()
                    addressModel.area =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(0)
                            .getString("value").trim()

                    addressModel.name = addressModel.area
                }else if (predsJsonArray.getJSONObject(i).getJSONArray("terms").length() == 6) {
                    addressModel.isArea = true
                    addressModel.country =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(5)
                            .getString("value").trim()
                    addressModel.state =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(4)
                            .getString("value").trim()
                    addressModel.city =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(3)
                            .getString("value").trim()
                    addressModel.area =
                        predsJsonArray.getJSONObject(i).getJSONArray("terms").getJSONObject(0)
                            .getString("value").trim()

                    addressModel.name = addressModel.area
                }


                resultList.add(addressModel)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return resultList
    }
}