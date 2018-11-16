package cf.dogo.core.boot

import cf.dogo.core.DogoBot
import cf.dogo.core.profiles.PermGroup
import cf.dogo.server.APIServer
import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.Game
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun main(args : Array<String>){
   Boot()
}

class Boot {
    private val phaseList = listOf(
            Phase("Initializing JDA") {
                cf.dogo.core.DogoBot.jda = JDABuilder(AccountType.BOT)
                        .setToken(DogoBot.data.BOT_TOKEN)
                        .setGame(Game.watching("myself starting"))
                        .addEventListener(DogoBot.eventBus)
                        .build().awaitReady()
            },
            Phase("Connecting to Database") {
                DogoBot.mongoClient = MongoClient(
                        ServerAddress(DogoBot.data.DB_HOST, DogoBot.data.DB_PORT),
                        MongoCredential.createCredential(
                                DogoBot.data.DB_USER,
                                DogoBot.data.DB_NAME,
                                DogoBot.data.DB_PWD.toCharArray()
                        ),
                        MongoClientOptions.builder().build()
                ).also {
                    DogoBot.db = it.getDatabase(DogoBot.data.DB_NAME)
                }
            },
            Phase("Checking Database") {
                if (!DogoBot.db!!.hasCollection("USERS")) {
                    DogoBot.logger.info("USERS collection doesn't exists! Creating one...")
                    DogoBot.db?.createCollection("USERS")
                }
                if (!DogoBot.db!!.hasCollection("GUILDS")) {
                    DogoBot.logger.info("GUILDS collection doesn't exists! Creating one...")
                    DogoBot.db?.createCollection("GUILDS")
                }
                if(!DogoBot.db!!.hasCollection("PERMGROUPS")) {
                    DogoBot.logger.info("PERMGROUPS collection doesn't exists! Creating one...")
                    DogoBot.db?.createCollection("PERMGROUPS")
                }
                if(!DogoBot.db!!.hasCollection("STATS")) {
                    DogoBot.logger.info("STATS collection doesn't exists! Creating one...")
                    DogoBot.db?.createCollection("STATS")
                }
            },
            Phase("Registering Commands"){
                DogoBot.cmdFactory.registerCommand(cf.dogo.commands.Help(cf.dogo.core.DogoBot.cmdFactory))
                DogoBot.cmdFactory.registerCommand(cf.dogo.commands.Stats(cf.dogo.core.DogoBot.cmdFactory))
            },
            Phase("Setting up Permgroups") {
                PermGroup("0").apply {
                    name = "default"
                    priority = 0
                    applyTo = arrayListOf("everyone")
                    include = arrayListOf("command.*")
                    exclude = arrayListOf("command.admin.*")
                }
                PermGroup("-1").apply {
                    name = "admin"
                    include = arrayListOf("command.admin.*")
                    exclude = arrayListOf("command.admin.root.*")
                    priority = -1
                }
                PermGroup("-2").apply {
                    name = "root"
                    applyTo = arrayListOf(DogoBot.data.OWNER_ID)
                    include = arrayListOf("*")
                    priority = -2
                }
            },
            Phase("Initializing API"){
                DogoBot.apiServer = APIServer().also {
                    it.start()
                }
            }
    )

    init {
        Thread.currentThread().name = "Boot"
        System.setProperty("logFile", File(File(DogoBot.data.LOGGER_PATH), SimpleDateFormat("dd-MM-YYYY HH-mm-ss").format(Date())).absolutePath)

        DogoBot.logger.info("Starting Dogo v${DogoBot.version}")

        if(DogoBot.data.DEBUG_PROFILE){
            DogoBot.logger.warn("DEBUG PROFILE IS ACTIVE")
        }

        try {
            startup()
        } catch (ex : java.lang.Exception){
            DogoBot.logger.fatal("STARTUP FAILED", ex)
            System.exit(1)
        }
    }

    @Throws(Exception::class)
    fun startup() {
        DogoBot.logger.info(
                "\n  _____                    ____        _   \n" +
                " |  __ \\                  |  _ \\      | |  \n" +
                " | |  | | ___   __ _  ___ | |_) | ___ | |_ \n" +
                " | |  | |/ _ \\ / _  |/ _ \\|  _ < / _ \\| __|\n" +
                " | |__| | (_) | (_| | (_) | |_) | (_) | |_ \n" +
                " |_____/ \\___/ \\__, |\\___/|____/ \\___/ \\__|\n" +
                "                __/ |                      \n" +
                "               |___/                    \n" +
                "By NathanPB - https://github.com/DogoBot/Dogo")

        var count = 1

        for(phase in phaseList){
            val time = System.currentTimeMillis()
            DogoBot.logger.info("["+count+"/"+phaseList.size+"] " + phase.display)
            DogoBot.jda?.presence?.game = Game.watching("myself starting - "+phase.display)
            phase.start()
            DogoBot.logger.info("["+count+"/"+phaseList.size+"] Done in ${time.timeSince()}")
            count++
        }

        DogoBot.ready = true
        DogoBot.jda?.presence?.game = Game.watching("${cf.dogo.core.DogoBot.jda?.guilds?.size} guilds| dg!help")
        DogoBot.logger.info("Dogo is Done! ${cf.dogo.core.DogoBot.initTime.timeSince()}")
    }


    /*
     extension shit
     */

    private fun MongoDatabase.hasCollection(name : String) : Boolean {
        return this.listCollectionNames().contains(name)
    }

    private fun Long.timeSince() : String {
        val time = System.currentTimeMillis() - this
        return when{
            time < 1000 -> time.toString()+"ms"
            time < 60000 -> TimeUnit.MILLISECONDS.toSeconds(time).toString()+"sec"
            else -> TimeUnit.MILLISECONDS.toMinutes(time).toString()+"min"
        }
    }
}