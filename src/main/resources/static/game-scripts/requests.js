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
        this.send(new GameControlRequest("start", { }));
    }

    setWord(word) {
        this.send(new GameControlRequest("set-word", { "value": word }));
    }

    requestGameState() {
        this.send(new GameControlRequest("send-state", { }));
    }
}