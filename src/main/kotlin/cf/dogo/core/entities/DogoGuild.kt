package cf.dogo.core.entities

import cf.dogo.core.profiles.PermGroup
import cf.dogo.core.profiles.PermGroupSet
import net.dv8tion.jda.core.entities.Guild
import org.bson.Document
import java.util.*
import kotlin.collections.ArrayList

class DogoGuild (id : String){
    val id = id;
    companion object {
        val col = cf.dogo.core.DogoBot.db?.getCollection("GUILDS")
    }

    constructor(g : Guild) : this(g.id)
    val g = cf.dogo.core.DogoBot.jda?.getGuildById(id)

    init {
        if((col?.count(Document("ID", id)) as Long) < 1){
            col.insertOne(Document("ID", id))
        }
    }

    var prefix : ArrayList<String>
        get() {
            val doc = find()
            return if(doc.containsKey("prefix")) doc["prefix"] as ArrayList<String> else ArrayList()
        }
        set(value) = update(Document("\$set", Document("prefix", value)))

    var permgroups : PermGroupSet
        get() {
            val doc = find()
            if(!doc.containsKey("permgroups")) update(Document("\$set", Document("permgroups", ArrayList<String>())))
            val list = PermGroupSet()
            for(s in find().get("permgroups") as ArrayList<String>){
                list.add(PermGroup(s))
            }
            return list
        }
        set(value) {
            //todo fix that gambiarra
            val maped = ArrayList(Arrays.asList(value.map { v -> v.id }.toTypedArray()))
            update(Document("\$set", Document("permgroups", maped)))
        }



    fun find() : Document {
        return col?.find(Document("ID", id))?.first() as Document
    }

    fun update(replace : Document) {
        col?.findOneAndUpdate(Document("ID", id), replace)
    }
}