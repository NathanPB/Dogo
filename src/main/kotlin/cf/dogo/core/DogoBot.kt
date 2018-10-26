package cf.dogo.core

import cf.dogo.core.boot.Boot
import cf.dogo.core.cmdHandler.CommandFactory
import cf.dogo.core.entities.DogoGuild
import cf.dogo.core.eventBus.EventBus
import cf.dogo.core.queue.DogoQueue
import cf.dogo.server.APIServer
import com.google.common.reflect.TypeToken
import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import net.dv8tion.jda.core.JDA
import ninja.leaping.configurate.ConfigurationOptions
import ninja.leaping.configurate.json.JSONConfigurationLoader
import ninja.leaping.configurate.loader.ConfigurationLoader
import java.awt.Color
import java.io.File
import java.lang.management.ManagementFactory

class DogoBot {
    companion object {
        var jda: JDA? = null
        var mongoClient: MongoClient? = null
        var db: MongoDatabase? = null
        val data = JSONConfigurationLoader.builder()
                .setSource{{
                    val file = File("init.json")
                    if(!file.exists()){
                        file.createNewFile()
                    }
                    file.bufferedReader()
                }()}
                .setIndent(3)
                .setDefaultOptions(ConfigurationOptions.defaults())
                .build()
        var boot: Boot? = null
        var logger: cf.dogo.core.Logger? = null
        val initTime = System.currentTimeMillis()
        var ready = false
        val threads = HashMap<String, DogoQueue>()

        val eventBus = EventBus("EVENT BUS")
        val jdaOutputThread = DogoQueue("JDA OUTPUT")
        val cmdProcessorThread = DogoQueue("CMD PROCESSOR")
        val ocWatcher = DogoQueue("OC WATCHER")
        val statsWatcher = DogoQueue("STATS WATCHER")

        val cmdFactory = CommandFactory(eventBus)
        val instance = DogoBot()
        var apiServer : APIServer? = null
    }

    fun isAvailable() : Boolean {
        return ready
    }

    fun getCommandPrefixes(vararg guilds : DogoGuild) : List<String> {
        val list = data.load().getNode("COMMAND_PREFIX").getList(TypeToken.of(String::class.java))
        guilds.forEach { g -> list.addAll(g.prefix) }
        list.sortedBy { a -> -a.length}
        return list
    }

    fun usedMemory() : Long {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024)
    }

    fun maxMemory() : Long {
        return Runtime.getRuntime().totalMemory() / (1024 * 1024)
    }

    fun usedCPU() : Double {
        return ManagementFactory.getOperatingSystemMXBean().systemLoadAverage/ManagementFactory.getOperatingSystemMXBean().availableProcessors
    }
}