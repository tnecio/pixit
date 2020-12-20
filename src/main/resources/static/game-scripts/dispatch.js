function dispatchGameEvent(event) {
    // no-op for now
}

function dispatchPlayerEvent(event) {
    // no-op for now
}

function dispatchGameUpdate(msg) {
    if (msg.type === "commonGameViewUpdate") {
        pixit.commonPartsGameUpdate(msg.payload);
    }
    console.log("Couldn't dispatch game update: " + msg);
}

function dispatchUserGameUpdate(msg) {
    if (msg.type === "gameViewUpdate") {
        pixit.wholeGameUpdate(msg.payload);
    }
    console.log("Couldn't dispatch user's game update: " + msg);
}