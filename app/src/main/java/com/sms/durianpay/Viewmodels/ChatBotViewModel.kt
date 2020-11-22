package com.sms.durianpay.Viewmodels

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sms.durianpay.Data.dataClasses.CovidCasesSummary
import com.sms.durianpay.Data.repositories.ChatBotRepository
import com.sms.durianpay.utils.ResourceStatus

class ChatBotViewModel: ViewModel() {

    var apiCallStatus = MutableLiveData<ResourceStatus>() // livedata for observing API call status
    var responseLiveData = MutableLiveData<CovidCasesSummary>() // live data for getting response


    fun callCovidData(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nInfo: NetworkInfo? = connectivityManager.getActiveNetworkInfo()
        val connected =
            nInfo != null && nInfo.isAvailable && nInfo.isConnected
        if (connected) { //using the above connectivity manager, check user has an active internet
            apiCallStatus.value = ResourceStatus.loading()//change the value of api call status to check in main activity
            //call the repository for api hit and get the success boolean and the response
            ChatBotRepository.covidSummaryRepoonResult() { isSuccess, response ->

                if (isSuccess) {
                    apiCallStatus.value =
                        ResourceStatus.success("")//change the value of api call status to check in main activity
                    responseLiveData.postValue(response) //store the response in response live data

                } else {
                    if (response?.Message== "") { // check if the response message is successful one
                        apiCallStatus.value =
                            ResourceStatus.sessionexpired() //change the value of api call status to check in main activity
                    } else {
                        apiCallStatus.value =
                            ResourceStatus.error("") //change the value of api call status to check in main activity
                    }

                }

            }


        } else {
            Log.e("CallStatus","No netwrok") // log if the user doesn't have an active network
            apiCallStatus.value = ResourceStatus.nonetwork() //change the value of api call status to check in main activity
        }

    }
}