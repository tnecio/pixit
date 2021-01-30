class GameControlRequest {
    constructor(_endpoint, _payload) {
        this.endpoint = _endpoint;
        this.payload = _payload;
    }
}

class Requester {
    constructor(_userId) {
        this.userId = _userId;
    }

    send(request) {
        console.log("Sending request " + JSON.stringify(request));
        sendRequest(gameControlEndpoint(request.endpoint),
            {payload: request.payload, userId: userId}
        );
    }

    startGame() {
        this.send(new GameControlRequest("start", {}));
    }

    setWord(word, chosenCardId) {
        this.send(new GameControlRequest("set-word", {"word" : {"value": word}, "cardId" : chosenCardId }));
    }

    sendCard(cardId) {
        this.send(new GameControlRequest("send-card", {"cardId": cardId}));
    }

    vote(cardId) {
        this.send(new GameControlRequest("vote", {"cardId": cardId}));
    }

    proceed() {
        this.send(new GameControlRequest("proceed", {}));
    }

    requestGameState() {
        this.send(new GameControlRequest("send-state", {}));
    }
}