package io.github.dogo.commands

import com.mashape.unirest.http.Unirest
import com.sun.org.glassfish.external.statistics.Statistic
import io.github.dogo.badwords.BadwordListener
import io.github.dogo.core.DogoBot
import io.github.dogo.core.command.*
import io.github.dogo.core.data.DogoData
import io.github.dogo.core.eventBus.EventBus
import io.github.dogo.security.PermGroup
import io.github.dogo.security.PermGroupSet
import io.github.dogo.discord.DiscordException
import io.github.dogo.discord.DiscordManager
import io.github.dogo.discord.IRepliable
import io.github.dogo.discord.JDAListener
import io.github.dogo.discord.menus.ListReactionMenu
import io.github.dogo.discord.menus.SelectorReactionMenu
import io.github.dogo.discord.menus.SimpleReactionMenu
import io.github.dogo.lang.LanguageEntry
import io.github.dogo.minigames.tictactoe.ITTTImp
import io.github.dogo.minigames.tictactoe.OnePlayerTTT
import io.github.dogo.minigames.tictactoe.TTTPlayer
import io.github.dogo.minigames.tictactoe.TwoPlayersTTT
import io.github.dogo.minigames.tictactoe.discord.TicTacToeImp
import io.github.dogo.server.APIException
import io.github.dogo.server.APIRequestProcessor
import io.github.dogo.server.APIServer
import io.github.dogo.server.Token
import io.github.dogo.statistics.TicTacToeStatistics
import io.github.dogo.utils.Holder
import io.github.dogo.utils._static.*
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Game
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import org.jsoup.Jsoup
import java.awt.Color
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URL
import javax.script.Invocable

class Eval {

    companion object {
        val imports = arrayOf(
                BadwordListener::class,
                CommandCategory::class,
                CommandContext::class,
                CommandReference::class,
                CommandRouter::class,
                ReferencedCommand::class,
                DogoData::class,
                EventBus::class,
                PermGroup::class,
                PermGroupSet::class,
                DogoBot::class,
                JDAListener::class,
                APIException::class,
                DiscordException::class,
                IRepliable::class,
                LanguageEntry::class,
                ListReactionMenu::class,
                SelectorReactionMenu::class,
                SimpleReactionMenu::class,
                TicTacToeImp::class,
                ITTTImp::class,
                OnePlayerTTT::class,
                TTTPlayer::class,
                TicTacToe::class,
                TwoPlayersTTT::class,
                Token::class,
                APIRequestProcessor::class,
                APIServer::class,
                Statistic::class,
                TicTacToeStatistics::class,
                BeamUtils::class,
                DiscordAPI::class,
                DisplayUtils::class,
                EmoteReference::class,
                FacebookUtils::class,
                FileUtils::class,
                HastebinUtils::class,
                Holder::class,
                SystemUtils::class,
                ThemeColor::class,
                UnitUtils::class,

                Guild::class,
                Message::class,
                User::class,
                EmbedBuilder::class,
                Game::class,

                File::class,
                URL::class,

                Unirest::class,
                Jsoup::class,
                DriveUtils::class
        ).joinToString("") { "import ${it.qualifiedName}\n" }
    }

    class KotlinEval : ReferencedCommand(
            CommandReference("kotlin", aliases = "kt", args = 1, permission = "command.admin.root"),
            {
                val embedPast = replySynk(EmbedBuilder().setColor(Color.YELLOW).setTitle(langEntry.getText("evaluating")).build())
                System.setProperty("idea.io.use.fallback", "true")
                val desc = StringBuilder()
                EmbedBuilder()
                        .setAuthor(langEntry.getText("title"))
                        .setColor(Color.GREEN)
                        .also { embed ->
                            try {
                                ((KotlinJsr223JvmLocalScriptEngineFactory()
                                        .scriptEngine
                                        .also {
                                            var code = args.joinToString(" ")
                                            if(code.startsWith("\n```")){
                                                code = code.replaceFirst(code.split("\n")[1], "")
                                                code = code.substring(0, code.length-3)
                                            }
                                            it.eval(formatCode(code))
                                        } as Invocable)
                                        .invokeFunction("run", this).let { if(it is Unit) null else it } ?: langEntry.getText("noreturn"))
                                        .let { desc.append(it) }
                            } catch (ex: Exception) {
                                embed.setColor(Color.RED)
                                desc.append(StringWriter().also { ex.printStackTrace(PrintWriter(it)) }.toString())
                            }
                        }.let {
                            it.setDescription(
                                langEntry.getText("result")+
                                if(desc.length > 1500){
                                    " [Hastebin](${HastebinUtils.URL}${HastebinUtils.upload(desc.toString())})"
                                } else "\n```$desc```"
                            )
                            DiscordManager.jdaOutputThread.submit {
                                embedPast.editMessage(
                                        it.setFooter(langEntry.getText("took", UnitUtils.timeSince(embedPast.creationTime.toInstant().toEpochMilli())/1000), null)
                                        .setAuthor(langEntry.getText("title"), "https://kotlinlang.org", "https://kotlinlang.org/assets/images/open-graph/kotlin_250x250.png")
                                        .build()
                                ).complete()
                            }
                        }
            }
    ) {
        companion object {
            fun formatCode(code: String) = """
            $imports
            val a: CommandContext.()->Any? = {
                $code
            }
            fun run(cmd: CommandContext) = a(cmd)
        """.trimIndent()
        }
    }
}