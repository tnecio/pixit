var stompClient;

connect();
setInterval(tryConnectIfDisconnected, 3000);

function tryConnectIfDisconnected() {
    console.log("tryConnectIfDisconnected");
    if (pixit.isDisconnected()) {
        pixit.startConnecting();
        console.log("disconnected, retrying connection");
        connect();
    }
}

function connect() {
    stompClient = Stomp.over(new SockJS('/pixit-websocket'));
    stompClient.connect({}, frame => {
            console.log('Connected: ' + frame);
            stompClient.subscribe('/topic/' + gameId + '/' + sessionId + '/response', event => {
                handleAcknowledgment(JSON.parse(event.body));
            });
            stompClient.subscribe('/topic/' + gameId + '/' + sessionId + '/gameUpdate', event => {
                dispatchGameUpdate(JSON.parse(event.body));
            });
            stompClient.subscribe('/topic/' + gameId + '/heartbeat', event => {
                handleHeartbeat(JSON.parse(event.body));
            });
            stompClient.subscribe('/topic/' + gameId + '/event', event => {
                handleGameEvent(JSON.parse(event.body));
            });
            stompClient.subscribe('/topic/' + gameId + '/' + sessionId + '/event', event => {
                handleGameEvent(JSON.parse(event.body));
            });
            stompClient.subscribe('/topic/' + gameId + '/chat', event => {
                handleChatUpdate(JSON.parse(event.body));
            });
            sendRequest(gameControlEndpoint("connected"), {});
            pixit.connectionUp();
            console.log("connectionUp");
        },
        error => {
            pixit.connectionLost();
            console.log("connectionFailed");
        });
}

function sendRequest(endpoint, request) {
    stompClient.send("/app" + endpoint, {}, JSON.stringify(request));
}


class GameControlRequest {
    constructor(_endpoint, _payload) {
        this.endpoint = _endpoint;
        this.payload = _payload;
    }
}

function gameControlEndpoint(ending) {
    return "/v1/game-control/" + gameId + "/" + sessionId + "/" + ending
}

class Requester {
    constructor(_userId) {
        this.userId = _userId;
    }

    send(request) {
        console.log("Sending request " + JSON.stringify(request));
        sendRequest(gameControlEndpoint(request.endpoint), request.payload);
    }

    startGame() {
        this.send(new GameControlRequest("start", {}));
    }

    setWord(chosenCardId, word) {
        this.send(new GameControlRequest("set-word", {"word": {"value": word}, "cardId": chosenCardId}));
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

    kickOut(playerId) {
        this.send(new GameControlRequest("kick-out", {"playerId": playerId}))
    }

    sendChat(msg) {
        sendRequest("/v1/chat/" + gameId + "/" + sessionId + "/chat", { "msg": msg });
    }
}

/*
 Dispatch incoming events
 */

function handleAcknowledgment(event) {
    console.log("Received acknowledgment: " + JSON.stringify(event))
}

function handleGameEvent(event) {
    if (event.payload === "KICKED_OUT") {
        window.location.href = "/kicked-out";
    } else if (event.payload === "GAME_NOT_FOUND") {
        window.location.reload();
    } else {
        const message = t[event.payload];
        pixit.displayMessage(message);
    }
}

function handleChatUpdate(event) {
    pixit.handleChatUpdate(event);
}


function dispatchGameUpdate(msg) {
    if (msg.type === "gameUpdate") {
        pixit.update(msg.payload);
    } else {
        console.log("Couldn't dispatch user's game update: " + JSON.stringify(msg));
    }
}

/*
 Heartbeats logic
 */

setInterval(sendHeartbeat, 3000);

function sendHeartbeat() {
    sendRequest("/v1/heartbeat/" + gameId + "/" + sessionId,
        {version: pixit.game.version}
    );
}