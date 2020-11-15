/*
 * Reacts to events received from server. eventType must correspond to class names in GameEvent.kt
 */

function dispatch(event) {
    let body = event.body;
    switch (event.eventType) {
        case "DrawCard":
            pixit.addCard(body.card);
            break;
    }
}