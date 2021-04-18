package io.tnec.pixit.game

import io.tnec.pixit.card.CardId
import org.springframework.stereotype.Component

@Component
class PointsCounter {
    fun countPoints(model: GameModel) {
        val narratorId = model.narrator
        val whoVotedForWhom = getWhoVotedForWhom(model)

        for ((_, avatar) in model.players) {
            avatar.roundPointDelta = 0
        }

        model.roundResult = when {
            (whoVotedForWhom.all { it.value == narratorId }) -> RoundResult.ALL_VOTED_FOR_NARRATOR
            (whoVotedForWhom.all { it.value != narratorId }) -> RoundResult.NO_ONE_VOTED_FOR_NARRATOR
            else -> RoundResult.SOMEONE_VOTED_FOR_NARRATOR
        }

        when (model.roundResult) {
            RoundResult.NO_ONE_VOTED_FOR_NARRATOR -> {
                everyoneButNarratorGetsTwo(model)
                trickstersGetOne(model, whoVotedForWhom)
            }
            RoundResult.SOMEONE_VOTED_FOR_NARRATOR -> {
                narratorGetsThree(model)
                trickstersGetOne(model, whoVotedForWhom)
                correctGuessersGetThree(model, whoVotedForWhom)
            }
            RoundResult.ALL_VOTED_FOR_NARRATOR -> {
                everyoneButNarratorGetsTwo(model)
            }
            else -> throw IllegalStateException()
        }
    }

    private fun getWhoVotedForWhom(model: GameModel): Map<UserId, UserId> {
        val narratorId = model.narrator

        val whoseCardIsIt: MutableMap<CardId, UserId> = HashMap()
        for ((playerId, avatar) in model.players) {
            if (avatar.sentCard == null) {
                // someone joined after card sending
                continue
            }
            whoseCardIsIt[avatar.sentCard!!] = playerId
        }

        val whoVotedForWhom: MutableMap<UserId, UserId> = HashMap()
        for ((playerId, avatar) in model.players) {
            if (playerId == narratorId) continue
            whoVotedForWhom[playerId] = whoseCardIsIt[avatar.vote!!] ?: continue
        }

        return whoVotedForWhom
    }

    private fun everyoneButNarratorGetsTwo(model: GameModel) {
        val narratorId = model.narrator
        for (playerId in model.players.keys) {
            if (playerId == narratorId) continue
            model.players[playerId]!!.roundPointDelta += 2
        }
    }

    private fun narratorGetsThree(model: GameModel) {
        val narratorId = model.narrator
        model.players[narratorId]!!.roundPointDelta += 3
    }

    private fun trickstersGetOne(model: GameModel, whoVotedForWhom: Map<UserId, UserId>) {
        val narratorId = model.narrator
        for (playerId in model.players.keys) {
            val trickster = whoVotedForWhom[playerId] ?: continue
            if (trickster == narratorId) continue
            model.players[trickster]!!.roundPointDelta += 1
        }
    }

    private fun correctGuessersGetThree(model: GameModel, whoVotedForWhom: Map<UserId, UserId>) {
        val narratorId = model.narrator
        if (model.roundResult == RoundResult.SOMEONE_VOTED_FOR_NARRATOR) {
            for ((playerId, votedFor) in whoVotedForWhom) {
                if (votedFor == narratorId) {
                    model.players[playerId]!!.roundPointDelta += 3
                }
            }
        }
    }
}