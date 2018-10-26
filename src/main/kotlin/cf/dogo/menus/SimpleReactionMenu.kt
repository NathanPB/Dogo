package cf.dogo.menus

import cf.dogo.core.cmdHandler.CommandContext
import cf.dogo.core.eventBus.EventBus
import cf.dogo.utils.EmoteReference
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.PrivateChannel
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import java.util.*

/**
 * Created by nathanpb on 12/2/17.
 */
class SimpleReactionMenu(val context: CommandContext) {
    protected var actions: ArrayList<Action> = ArrayList()
    var embed = EmbedBuilder()
    private var deleteOnClick = true
    protected val target = context.sender.id
    var msg: Message? = null

    protected var eventbusId : Array<Long> = emptyArray()

    fun build(preset : EmbedBuilder? = null): SimpleReactionMenu {
        if(preset == null) {
            for (ac in actions) {
                embed.setDescription(embed.descriptionBuilder.toString() + ac.emote + " " + ac.description + "\n")
            }
        } else {
            this.embed = preset
        }
        return this
    }

    fun end(delete : Boolean = true) {
        cf.dogo.core.DogoBot.eventBus.unregister(eventbusId)
        if(delete) {
            msg?.delete()?.queue()
            msg = null
        } else {
            msg?.clearReactions()?.queue()
        }
    }

    fun send() {
        eventbusId = cf.dogo.core.DogoBot.eventBus.register(this)
        if(msg == null) {
            msg = context.replySynk(embed.build())
        } else {
            (msg as Message).editMessage(embed.build()).queue()
            if(!(context.replyChannel() is PrivateChannel)) {
                (msg as Message).clearReactions().complete()
            }
        }
        actions.forEach { a -> msg!!.addReaction(a.emote.id).queue({}, {}) }
    }

    fun addAction(emote: EmoteReference, description: String, action: () -> Unit): SimpleReactionMenu {
        if (!actions.stream().anyMatch { a -> a.emote == emote }) {
            actions.add(
                    object : Action {
                        override val action = action
                        override val description = description
                        override val emote = emote
                    }
            )
        }
        return this
    }

    fun removeAction(emote: EmoteReference): SimpleReactionMenu {
        actions = ArrayList(actions.filter { a -> a.emote != emote })
        return this
    }

    @EventBus.Listener(0)
    fun onReact(e: GenericMessageReactionEvent) {
        if(e is MessageReactionAddEvent || (e is MessageReactionRemoveEvent && e.channel is PrivateChannel)) {
            if (msg != null && e.messageId == msg!!.id && e.user.id == target) {
                for (ac in actions) {
                    if (e.reactionEmote.name == ac.emote.id) {
                        kotlin.run(ac.action)
                        break
                    }
                }
            }
        }
    }

    protected interface Action {
        val action: () -> Unit
        val description: String
        val emote: EmoteReference
    }
}