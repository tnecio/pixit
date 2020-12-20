function dispatchGameEvent(event) {
    // no-op for now
}

function dispatchPlayerEvent(event) {
    // no-op for now
}

function dispatchGameUpdate(newGame) {
    // Convert from backend model to Vue model
    var update = {
        word: (newGame.word ? newGame.word : { value: "No word set" }),
        game: {
            state: newGame.state
        },
        player: (newGame.players[userId] ? newGame.players[userId] : { name: "", deck: [], points: 0})
    };
    pixit.updateGame(update);
}