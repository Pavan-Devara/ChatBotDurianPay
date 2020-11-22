package com.sms.durianpay.Data.repositories

import com.sms.durianpay.Data.dataClasses.CovidCasesSummary
import com.sms.durianpay.Data.interfaces.CovidDataInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import javax.security.auth.callback.Callback

object ChatBotRepository {
    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job) // initialize the coroutine scope


    fun covidSummaryRepoonResult( onResult: (isSuccess: Boolean, response: CovidCasesSummary?) -> Unit){
        scope.launch {
            //call the interface to build the http call and execute
            CovidDataInterface.instance.covidCasesSummary().enqueue(object : retrofit2.Callback<CovidCasesSummary> {
                override fun onFailure(call: Call<CovidCasesSummary>, t: Throwable) {
                    onResult(false, null)//return the response boolean and null response to viewmodel on failure
                }

                override fun onResponse(
                    call: Call<CovidCasesSummary>,
                    response: Response<CovidCasesSummary>
                ) {
                    if (response != null && response.isSuccessful)
                        onResult(true, response.body()!!)//return the response boolean and response to viewmodel on success
                    else
                        onResult(false, null)//return the response boolean and null response to viewmodel on failure
                }


            })
        }
    }

}