package com.commonfriend.viewmodels

import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.Protocol
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.commonfriend.R
import com.commonfriend.utils.BUCKET_FOLDER_NAME
import com.commonfriend.utils.BUCKET_NAME
import com.commonfriend.utils.BUCKET_REGION
import com.commonfriend.utils.COGNITO_POOL_ID
import com.commonfriend.utils.Util
import java.io.File


class UploadImageViewModel {
    companion object {
        val shared: UploadImageViewModel = UploadImageViewModel()
    }

    //    val uploadResponse: MutableLiveData<String> = MutableLiveData()
    fun uploadWithTransferUtility(
        context: Context,
        filePath: String,isShowProgress : Boolean = true,
        onComplete: ((String) -> Unit)? = null
    ) {

        if (!Util.isOnline(context)) {
            Util.showToastMessage(
                context,
                context.resources.getString(R.string.checkInternet),
                false
            )
            return
        }

        if(isShowProgress){
            Util.run { showProgress(context) }
        }


//    TransferNetworkLossHandler.getInstance(Context)
        val configuration = ClientConfiguration()
        configuration.maxErrorRetry = 3;
        configuration.protocol = Protocol.HTTP;
        configuration.connectionTimeout = 30*1000;
        configuration.socketTimeout = 30*1000;

        val transferUtility = TransferUtility.builder().s3Client(
            AmazonS3Client(
                CognitoCachingCredentialsProvider(
                    context.applicationContext,
                    COGNITO_POOL_ID,
                    Regions.fromName(BUCKET_REGION)
                ),
                Region.getRegion(Regions.fromName(BUCKET_REGION)),configuration
            )
        ).context(context.applicationContext).build()

        if (!File(filePath).exists()) {
            Util.dismissProgress()
            Util.showToastMessage(context, "Could not find the filepath of the selected file", true)
            return
        }

        val isVideo = Util.checkIsVideoFile(filePath)
        val time = System.currentTimeMillis()
        val fileName: String =
            if (isVideo) "$BUCKET_FOLDER_NAME/android_$time.mp4" else "$BUCKET_FOLDER_NAME/android_$time.png"

        val url = "https://s3.$BUCKET_REGION.amazonaws.com/$BUCKET_NAME/$fileName"
//        val url = "https://getmarried.s3.amazonaws.com/$BUCKET_NAME/$fileName"

        val observer: TransferObserver =
            transferUtility.upload(BUCKET_NAME, fileName, File(filePath))
//        val uploadObserver = transferUtility.upload("s3folder/s3key.txt", File("/path/to/localfile.txt"))

        // Attach a listener to the observer
        observer.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                Util.print("=============== state :: " + state +"::"+id)

                if (state == TransferState.COMPLETED) {
                    // Handle a completed upload
                    Util.dismissProgress()
//                    uploadResponse.postValue(url)
                    Util.print("=============== Final URIL :: " + url)
                    onComplete?.invoke(url)
                }
            }

            override fun onProgressChanged(id: Int, current: Long, total: Long) {
                val done = (((current.toDouble() / total) * 100.0).toInt())
            }

            override fun onError(id: Int, ex: Exception) {
                ex.printStackTrace()
                Util.dismissProgress()
            }
        })
        // If you prefer to long-poll for updates
        /*        if (observer.state == TransferState.COMPLETED) {
            *//* Handle completion *//*
        }

        val bytesTransferred = observer.bytesTransferred*/
    }
}