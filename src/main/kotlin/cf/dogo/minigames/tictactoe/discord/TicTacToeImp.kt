package cf.dogo.minigames.tictactoe.discord

import cf.dogo.core.DogoBot
import cf.dogo.core.command.CommandContext
import cf.dogo.core.entities.DogoUser
import cf.dogo.menus.SimpleReactionMenu
import cf.dogo.minigames.tictactoe.Player
import cf.dogo.minigames.tictactoe.TwoPlayersTTT
import cf.dogo.statistics.TicTacToeStatistics
import cf.dogo.utils.EmoteReference
import cf.dogo.utils.ThemeColor
import net.dv8tion.jda.core.EmbedBuilder
import java.util.*

class TicTacToeImp(context: CommandContext, val p1: DogoUser, val p2: DogoUser) : SimpleReactionMenu(context) {

    var hasWinner = false
    val ttt = TwoPlayersTTT {
        hasWinner = true
        this.build(
                createEmbed()
                .appendDescription("\n"+when(it){
                    Player.ENVIROMENT -> context.langEntry().getText(context.lang(), "tie")
                    else -> context.langEntry().getText(context.lang(), "winner", getCurrentPlayer().formatName(context.guild))
                })
        )
        this.send(true)
        this.end(false)
        dumbSetStatistics(it)
    }
    private fun dumbSetStatistics(winner: Player) {
        TicTacToeStatistics(ttt.table, p1, p2, if(winner != Player.ENVIROMENT) getCurrentPlayer() else null).update()
    }

    init {
        this.build(createEmbed())
        this.send(false)
    }

    private fun createEmbed() : EmbedBuilder {
        return EmbedBuilder()
            .setTitle("Tic Tac Toe!")
            .setColor(ThemeColor.PRIMARY)
            .also {
                if(!(againstBot() || hasWinner)){
                    it.appendDescription(context.langEntry().getText(context.lang(), "turn", getCurrentPlayer().formatName(this.context.guild))+"\n\n")
                }
                it.appendDescription(
                    getTableEmoted().mapIndexed { i, it ->
                        if((i+1)%3 == 0) "${it.getAsMention()}\n" else it.getAsMention()
                    }.joinToString("")
                )
            }
    }

    private fun getTableEmoted() : List<EmoteReference> {
        return ttt.table.toCharArray().mapIndexed { i, it ->
            when (it) {
                '1' -> EmoteReference.X
                '2' -> EmoteReference.O
                else -> EmoteReference.getRegional(i.toString()[0])
            }
        }
    }

    private fun getCurrentPlayer() : DogoUser = if(this.ttt.current == Player.P1) p1 else p2
    private fun againstBot() = p2.usr?.isBot == true || p2.usr?.isFake == true

    fun send(end: Boolean){
        DogoBot.eventBus.unregister(this)
        this.actions.clear()
        if(!end){
            getTableEmoted()
                    .filter { !(it == EmoteReference.O || it == EmoteReference.X) }
                    .forEach {
                        this.addAction(it,  "") {
                            this.ttt.play(it.getChar().toString().toInt())
                            if(!hasWinner){
                                if(againstBot()){
                                    this.ttt.play({
                                        val empty = ttt.table.toCharArray().mapIndexed{index, it ->
                                        if(it == '0') index else -1
                                    }.filter { it > -1 }
                                    empty[Random().nextInt(empty.size)]
                                    }(), Player.P2)
                                    ttt.togglePlayer()
                                }
                                if(!hasWinner){
                                    this.build(createEmbed())
                                    this.target = getCurrentPlayer().id
                                    this.send(false)
                                }
                            }
                        }
                    }
            this.addAction(EmoteReference.OCTAGONAL_SIGN, "") {ttt.togglePlayer(); ttt.onWin(ttt.current)}
        }
        super.send()
    }
}