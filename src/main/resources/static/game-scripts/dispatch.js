function handleAcknowledgment(event) {
    console.log("Received acknowledgment: " + JSON.stringify(event))
}


function dispatchGameUpdate(msg) {
    if (msg.type === "gameUpdate") {
        pixit.update(msg.payload);
    } else {
        console.log("Couldn't dispatch user's game update: " + JSON.stringify(msg));
    }
}

function handleHeartbeat(heartbeat) {
    console.log("Calling Pixit with heartbeat " + JSON.stringify(heartbeat));
    pixit.updateIfVersionIsNewer(heartbeat.payload.version);
}