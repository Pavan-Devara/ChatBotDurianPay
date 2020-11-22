package com.sms.durianpay

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.sms.durianpay.Adapters.ChatMessageAdapter
import com.sms.durianpay.Data.dataClasses.CovidCasesSummary
import com.sms.durianpay.Viewmodels.ChatBotViewModel
import com.sms.durianpay.utils.ResourceStatus
import com.sms.durianpay.utils.StatusType
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private val CASESSUMMARY: String = "CovidCasesSummary"
    private var TRYCOUNT: Int = 0
    private val CHATARRAY = "ChatArray"
    private var sharedPreferences: SharedPreferences? = null
    private var allChat = listOf<String>().toMutableList()
    var gson = Gson()

    private lateinit var viewModelChatBot: ChatBotViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //set the UI for the activity
        setContentView(R.layout.activity_main)

        //initialize viewmodel by lateint
        viewModelChatBot = ViewModelProviders.of(this)[ChatBotViewModel::class.java]
        //getting the required sharedpreferences file
        sharedPreferences = getSharedPreferences("ChatDetails", Context.MODE_PRIVATE);
        //get the previous conversations with chatbot
        val jsonText: String? = sharedPreferences!!.getString(CHATARRAY, null)
        if (jsonText!=null) {
            //if the conversation is not empty, load all the chat to local variable "allChat"
            //gson is used to convert the array to string and store it in local
            val chats: Array<String> =
                gson.fromJson(jsonText, Array<String>::class.java)
            allChat = chats.toMutableList()
        }
        //click operation for the ask button
        button_send.setOnClickListener{
            if (!text_to_send.text.trim().isEmpty()) {
                //operation to compute the deaths/cases by chatbot
                sendmessage();
            }
            //clear the asked question from the edit box
            text_to_send.setText("")
        }
        //listener function to call the covid data api
        setUpListeners()
        //observers to observe whenever the api returns a response
        setupObservers()
        //function to call the recycler view to display all the chat messages
        createChat()
    }

    private fun setupObservers() {
        //observing the response received from the api hit
        viewModelChatBot.responseLiveData.observe(this, Observer {
            //check if we do not have any error messages
            if (it.Message==""){
                //intialize the editor to edit the sharedpreferences file
                val editor = sharedPreferences!!.edit()
                //save the received data as key value pair with key as "CovidCasesSummary"
                editor.putString(CASESSUMMARY, gson.toJson(it))
                //apply all the changes
                editor.apply()
            }else{
                //call the api again two times if the first time or the second time fails
                if (TRYCOUNT<3) {
                    setUpListeners()
                }
            }
        })
        //observe the status code of the api hit, whether it is a hit or fail
        viewModelChatBot.apiCallStatus.observe(this, Observer {
            //processStatus function for taking action according to the status code of api
            processStatus(it)
        })
    }

    private fun setUpListeners() {
        //calling the api
        viewModelChatBot.callCovidData(this)
        TRYCOUNT = TRYCOUNT + 1
    }


    private fun sendmessage() {
        //add the recent message typed to the "allChat" list
        allChat.add(text_to_send.text.trim().toString())

        //get the saved response data from api
        val summaryString = sharedPreferences!!.getString(CASESSUMMARY, null)
        if (summaryString != null) {//check if there is any repsonse present in our local
            val casesSummary = gson.fromJson(summaryString, CovidCasesSummary::class.java)//changing the string to json
            val totalDeaths = casesSummary.Global.TotalDeaths + casesSummary.Global.NewDeaths//calculating the no. of global deaths
            //calculating the global active cases
            val totalActiveCases =
                casesSummary.Global.TotalConfirmed + casesSummary.Global.NewConfirmed -
                        casesSummary.Global.NewRecovered - casesSummary.Global.TotalRecovered -
                        casesSummary.Global.NewDeaths - casesSummary.Global.TotalDeaths
            when (text_to_send.text.trim().toString()) {//switch case to respond for the questions
                "CASES TOTAL" -> allChat.add(totalActiveCases.toString())//adds the bot response of total active cases to allChat
                "DEATHS TOTAL" -> allChat.add(totalDeaths.toString())//adds the bot response of total death cases to allChat
                else -> {
                    val result = getDeathsorCases(casesSummary)//function call to check the other two cases or default case
                    allChat.add(result)//adds the bot response returned from above function
                }
            }
            //same segment as onCreate() to update the local db and createChat() called to update the recycler view to display the changes
            val jsonText: String = gson.toJson(allChat)
            val editor = sharedPreferences!!.edit()
            editor!!.putString(CHATARRAY, jsonText)
            editor.apply()
            createChat()
        }else{
            //toast if no data is available in the db
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDeathsorCases(casesSummary: CovidCasesSummary): String {
        val input = text_to_send.text.trim().toString()
        if (input.contains("CASES")){//check if the question is asked about active cases
            val temp1 = "CASES "
            val countryCode = input.substring(input.indexOf("CASES ") + temp1.length).trim();//trimming the country code from the string
            Log.e("MAINACTIVITY", "country_code " + countryCode)//logs the country code
            for (i in casesSummary.Countries){//iterate through the list of countries
                if (i.CountryCode.equals(countryCode)){//check if any code matches
                    val totalActiveCases =
                        i.TotalConfirmed + i.NewConfirmed - i.NewRecovered - i.TotalRecovered - i.NewDeaths - i.TotalDeaths
                    return totalActiveCases.toString()//return the active cases from above calculation
                }
            }
        }else if (input.contains("DEATHS")){//check if the question is asked about death cases
            val temp1 = "DEATHS "
            val countryCode = input.substring(input.indexOf("DEATHS ") + temp1.length).trim();//trimming the country code from the string
            Log.e("MAINACTIVITY", "country_code " + countryCode)//logs the country code
            for (i in casesSummary.Countries){//iterate through the list of countries
                if (i.CountryCode.equals(countryCode)){//check if any code matches
                    val totalDeaths = i.TotalDeaths + i.NewDeaths
                    return totalDeaths.toString()//return the dead cases from above calculation
                }
            }
        }else{
            //default return to suugest the user to use only any of the four commands for chatbot to respond
            return "Please type any one of the four commands 1) CASES TOTAL, 2) DEATHS TOTAL, 3) CASES <country-code>, 4) DEATHS <country-code>"
        }
        //default return to suugest the user to use only any of the four commands for chatbot to respond
        return "Please type any one of the four commands 1) CASES TOTAL, 2) DEATHS TOTAL, 3) CASES <country-code>, 4) DEATHS <country-code>"
    }


    @SuppressLint("WrongConstant")
    private fun createChat() {
        //intialize the recycler view
        val recyclerView = findViewById<RecyclerView>(R.id.chat_recycler_view)
        //define layout for the recycler view
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        //scroll the screen automatically to the end of the screen which shows the latest chat
        recyclerView.scrollToPosition(allChat.size - 1)
        //initialize and set the recycler adapter with updated allChat array everytime
        val adapter = ChatMessageAdapter(allChat)
        recyclerView.adapter = adapter
    }


    private fun processStatus(resource: ResourceStatus) {
        //check the returned status code of api hit
        when (resource.status) {
            StatusType.SUCCESS -> {
                Toast.makeText(this, "Api hit Successful", Toast.LENGTH_SHORT).show()
            }
            StatusType.EMPTY_RESPONSE -> {
                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
            }
            StatusType.PROGRESSING -> {

            }
            StatusType.ERROR -> {
                Toast.makeText(this, "Please try again", Toast.LENGTH_SHORT).show()
            }
            StatusType.LOADING_MORE -> {
                Toast.makeText(this, "Loading......", Toast.LENGTH_SHORT).show()
            }
            StatusType.NO_NETWORK -> {
                Toast.makeText(this, "Please check internet connection", Toast.LENGTH_SHORT).show()
            }
            StatusType.SESSION_EXPIRED -> {
                Toast.makeText(this, "Session Expired", Toast.LENGTH_SHORT).show()
            }
        }

    }

}