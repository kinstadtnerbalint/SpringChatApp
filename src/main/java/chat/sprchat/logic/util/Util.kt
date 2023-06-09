package chat.sprchat.logic.util

import chat.sprchat.SprchatApplication.clients
import chat.sprchat.SprchatApplication.loadedMessages
import chat.sprchat.state.LoadedMessage
import chat.sprchat.state.orm.Message
import chat.sprchat.state.orm.MsgRepo
import com.google.gson.Gson
import org.java_websocket.WebSocket
import java.text.SimpleDateFormat

fun handleMessage(websocket: WebSocket, s: String, msgRepo: MsgRepo)
{
    when(s)
    {
        "#connectedUsers" -> websocket.send(
                "connectedUsers#" +
                clients.size
        )
        else -> {
            if(s.startsWith("#setName"))
            {
                val name = s.substring("#setName:".length)
                for(c in clients)
                    if(c.getSocket() == websocket)
                        c.setName(name)
            }
            else if(s.startsWith("#msg:"))
            {
                val msg = s.substring("#msg:".length)
                val data = msg.split(":")
                var username = ""

                for (c in clients)
                    if (c.getSocket() == websocket)
                        username = c.getName()

                val message = Message(username,
                        SimpleDateFormat("E MMM dd yyyy HH").parse(data[1]),
                        data.get(0))
                val newMessage= LoadedMessage(
                        message.user, message.message,
                        message.date)
                msgRepo.save(message)

                loadedMessages.add(newMessage)
                val gson = Gson()
                val msgSend = gson.toJson(loadedMessages)
                for(c in clients)
                    c.getSocket().send("#newMsg$msgSend")
            }
            else if(s.startsWith("#delete"))
            {
                val gson = Gson()
                val msgSend = gson.toJson(loadedMessages)
                for(c in clients)
                    c.getSocket().send("#newMsg$msgSend")
            }
        }
    }
}