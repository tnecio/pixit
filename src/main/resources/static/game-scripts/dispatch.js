function dispatchGameEvent(event) {
    // no-op for now
}

function dispatchPlayerEvent(event) {
    // no-op for now
}

function dispatchGameUpdate(msg) {
    if (msg.type === "commonGameViewUpdate") {
        pixit.commonPartsGameUpdate(msg.payload);
    } else {
        console.log("Couldn't dispatch game update: " + JSON.stringify(msg));
    }
}

function dispatchUserGameUpdate(msg) {
    if (msg.type === "gameViewUpdate") {
        pixit.wholeGameUpdate(msg.payload);
    } else {
        console.log("Couldn't dispatch user's game update: " + JSON.stringify(msg));
    }
}