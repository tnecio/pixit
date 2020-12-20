class GameControlRequest {
    constructor(_endpoint, _payload) {
        this.endpoint = _endpoint;
        this.payload = _payload;
    }
}

let requests = {
    startGame() {
        return new GameControlRequest("start", { });
    },
    setWord() {
        var word = document.getElementById("wordInput").value;
        return new GameControlRequest("set-word", { "value": word })
    },
    addCard() {
        // TODO not implemented yet!
    }
};