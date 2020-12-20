/*
 * This file abstracts away implementation of the WebSocket interface
 */

let stompClient = null;

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function currentTime() {
    return new Date().toLocaleTimeString('en-US', { hour12: false,
        hour: "numeric",
        minute: "numeric"});
}

function connect() {
    var socket = new SockJS('/pixit-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, frame => {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/' + gameId + '/response', event  => {
            console.log(currentTime() + " Received response for game request:" + event.body); // DEBUG
            dispatchGameEvent(JSON.parse(event.body));
        });
        stompClient.subscribe('/topic/' + gameId + '/player/' + userId + '/response', event  => {
            console.log(currentTime() + " Received response for player request:" + event.body); // DEBUG
            dispatchPlayerEvent(JSON.parse(event.body));
        });
        stompClient.subscribe('/topic/' + gameId + '/gameUpdate', event  => {
            console.log(currentTime() + " Received new game update:" + event.body); // DEBUG
            dispatchGameUpdate(JSON.parse(event.body));
        });
        stompClient.subscribe('/topic/' + gameId + '/' + userId + '/gameUpdate', event  => {
            console.log(currentTime() + " Received new game update for current user:" + event.body); // DEBUG
            dispatchUserGameUpdate(JSON.parse(event.body));
        });
        stompClient.subscribe('/topic/heartbeat', event  => {
            console.log(currentTime() + "Received heartbeat:" + event.body); // DEBUG
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendRequest(endpoint, request) {
    console.log(currentTime() + " sendRequest:" + request); // DEBUG
    stompClient.send("/app" + endpoint, {}, JSON.stringify(request));
}

function gameControlEndpoint(ending) {
    return "/v1/game-control/" + gameId + "/" + ending
}

connect();