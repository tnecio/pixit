package io.tnec.pixit.game

import io.tnec.pixit.avatar.Avatar
import io.tnec.pixit.card.Card
import io.tnec.pixit.card.Image
import org.junit.jupiter.api.Test

class PointsCounterTest {

    @Test
    fun allPlayersGuessedCorrectly() {
        val pointsCounter = PointsCounter()
        val model = GameModel(
                players = mapOf("p1" to Avatar(
                        name = "p1",
                        deck = listOf(),
                        vote = "cn",
                        sentCard = "c1",
                        roundPointDelta = 0
                ), "p2" to Avatar(
                        name = "p2",
                        deck = listOf(),
                        vote = "cn",
                        sentCard = "c2",
                        roundPointDelta = 0
                ), "p3" to Avatar(
                        name = "p3",
                        deck = listOf(),
                        vote = "cn",
                        sentCard = "c3",
                        roundPointDelta = 0
                ), "n" to Avatar(
                        name = "n",
                        deck = listOf(),
                        vote = null,
                        sentCard = "cn",
                        roundPointDelta = 0
                )),
                narrator = "n",
                table = listOf(
                        Card("c1", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("c2", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("c3", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("cn", Image("url", "alt", "desc", "attr"), revealed = true)
                ),
                word = Word("w"),
                state = GameState.WAITING_FOR_VOTES,
                version = 1,
                roundResult = RoundResult.IN_PROGRESS,
                admin = "p1",
                winners = listOf()
        )

        pointsCounter.countPoints(model)

        assert(model.players["p1"]!!.roundPointDelta == 2)
        assert(model.players["p2"]!!.roundPointDelta == 2)
        assert(model.players["p3"]!!.roundPointDelta == 2)
        assert(model.players["n"]!!.roundPointDelta == 0)
    }


    @Test
    fun noPlayersGuessedCorrectly() {
        val pointsCounter = PointsCounter()
        val model = GameModel(
                players = mapOf("p1" to Avatar(
                        name = "p1",
                        deck = listOf(),
                        vote = "c2",
                        sentCard = "c1",
                        roundPointDelta = 0
                ), "p2" to Avatar(
                        name = "p2",
                        deck = listOf(),
                        vote = "c1",
                        sentCard = "c2",
                        roundPointDelta = 0
                ), "p3" to Avatar(
                        name = "p3",
                        deck = listOf(),
                        vote = "c1",
                        sentCard = "c3",
                        roundPointDelta = 0
                ), "n" to Avatar(
                        name = "n",
                        deck = listOf(),
                        vote = null,
                        sentCard = "cn",
                        roundPointDelta = 0
                )),
                narrator = "n",
                table = listOf(
                        Card("c1", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("c2", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("c3", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("cn", Image("url", "alt", "desc", "attr"), revealed = true)
                ),
                word = Word("w"),
                state = GameState.WAITING_FOR_VOTES,
                version = 1,
                roundResult = RoundResult.IN_PROGRESS,
                admin = "p1",
                winners = listOf()
        )

        pointsCounter.countPoints(model)

        assert(model.players["p1"]!!.roundPointDelta == 4)
        assert(model.players["p2"]!!.roundPointDelta == 3)
        assert(model.players["p3"]!!.roundPointDelta == 2)
        assert(model.players["n"]!!.roundPointDelta == 0)
    }

    @Test
    fun somePlayersGuessedCorrectly() {
        val pointsCounter = PointsCounter()
        val model = GameModel(
                players = mapOf("p1" to Avatar(
                        name = "p1",
                        deck = listOf(),
                        vote = "cn",
                        sentCard = "c1",
                        roundPointDelta = 0
                ), "p2" to Avatar(
                        name = "p2",
                        deck = listOf(),
                        vote = "c1",
                        sentCard = "c2",
                        roundPointDelta = 0
                ), "p3" to Avatar(
                        name = "p3",
                        deck = listOf(),
                        vote = "c1",
                        sentCard = "c3",
                        roundPointDelta = 0
                ), "n" to Avatar(
                        name = "n",
                        deck = listOf(),
                        vote = null,
                        sentCard = "cn",
                        roundPointDelta = 0
                )),
                narrator = "n",
                table = listOf(
                        Card("c1", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("c2", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("c3", Image("url", "alt", "desc", "attr"), revealed = true),
                        Card("cn", Image("url", "alt", "desc", "attr"), revealed = true)
                ),
                word = Word("w"),
                state = GameState.WAITING_FOR_VOTES,
                version = 1,
                roundResult = RoundResult.IN_PROGRESS,
                admin = "p1",
                winners = listOf()
        )

        pointsCounter.countPoints(model)

        assert(model.players["p1"]!!.roundPointDelta == 5)
        assert(model.players["p2"]!!.roundPointDelta == 0)
        assert(model.players["p3"]!!.roundPointDelta == 0)
        assert(model.players["n"]!!.roundPointDelta == 3)
    }

}