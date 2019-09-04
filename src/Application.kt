package com.bigmeco

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.li
import kotlinx.html.ul
import me.ivmg.telegram.bot
import me.ivmg.telegram.dispatch
import me.ivmg.telegram.dispatcher.callbackQuery
import me.ivmg.telegram.dispatcher.command
import me.ivmg.telegram.entities.InlineKeyboardButton
import me.ivmg.telegram.entities.InlineKeyboardMarkup
import org.litote.kmongo.KMongo
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection


data class Jedi(val name: String, val age: Int)


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)


val client = KMongo.createClient()  //get com.mongodb.MongoClient new instance
val database = client.getDatabase("test") //normal java driver usage
val col = database.getCollection<Jedi>() //KMongo extension method


@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

//    col.insertOne(Jedi("Luke Skywalker", 19))
//    col.insertOne(Jedi("Luke sas", 43))

    val bot = bot {
        token = "829470729:AAHgoo9woTpGms1vxY6SVPDymLTvUPn_gQA"
        dispatch {
            command("start") { bot, update ->
                val chatId = update.message?.chat?.id ?: return@command

                val inlineKeyboardMarkup = InlineKeyboardMarkup(generateButtons())
                bot.sendMessage(chatId = chatId, text = "Hello, inline buttons!", replyMarkup = inlineKeyboardMarkup)
            }
            callbackQuery("testButton") { bot, update ->
                update.callbackQuery?.let {
                    val chatId = it.message?.chat?.id ?: return@callbackQuery
                    bot.sendMessage(chatId = chatId, text = col.find().toList().toString())
                }
            }
            callbackQuery("showAlert") { bot, update ->
                update.callbackQuery?.let {
                    update.message?.chat?.id
                    bot.sendMessage(
                        chatId = update.message?.chat?.id ?: return@callbackQuery,
                        text = "Ведите имя"
                    )

                    col.insertOne(Jedi(it.message?.chat?.lastName.toString(), 19))
                    val chatId = it.message?.chat?.id ?: return@callbackQuery
                    bot.sendMessage(chatId = chatId, text = "добавил")

                }
            }
        }
    }
    bot.startPolling()


    val yoda: Jedi? = col.findOne(Jedi::name eq "Yoda")

    install(ContentNegotiation) {
        gson {
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }
    }

}


fun generateButtons(): List<List<InlineKeyboardButton>> {
    return listOf(
        listOf(InlineKeyboardButton(text = "получить json", callbackData = "testButton")),
        listOf(InlineKeyboardButton(text = "добавить обьект json", callbackData = "showAlert"))
    )
}