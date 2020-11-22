package com.sms.durianpay.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sms.durianpay.R

//recycler adapter to show the chat messges
class ChatMessageAdapter(val chatlist: MutableList<String>): RecyclerView.Adapter<ChatMessageAdapter.ViewHolder>() {

    val MESSAGE_TYPE_LEFT = 0
    val MESSAGE_TYPE_RIGHT = 1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        //initialize the text view of the individual chat messages
        val displayMessage = itemView.findViewById<TextView>(R.id.text_message)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType==MESSAGE_TYPE_LEFT){ //check if the message is from bot or from the user
            //left view layout is intialized and returned as the present view for the user
            val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_left_view, parent, false )
            return ViewHolder(v)
        }else{
            //right view layout is intialized and returned as the present view for the bot response
            val v = LayoutInflater.from(parent.context).inflate(R.layout.chat_right_view, parent, false )
            return ViewHolder(v)
        }
    }

    override fun getItemCount(): Int {
        return chatlist.size //return the number of items present in the received chat array
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //retrieve the message according to its position in the chatlist array
        val message: String = chatlist[position]
        holder.setIsRecyclable(false) // set this as false to remove duplicate recycled elements to appear
        holder.displayMessage.text = message // set the message in the text view of user/bot
    }

    override fun getItemViewType(position: Int): Int {
        if (position%2 == 0){ //check if the message is from user/bot to initialize the view accordingly
            return MESSAGE_TYPE_LEFT
        }else{
            return MESSAGE_TYPE_RIGHT
        }
    }
}