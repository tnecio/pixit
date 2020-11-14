class Request {
    send() {
        sendRequest(this);
    }
}

class AddCardRequest extends Request {
    name = "AddCard";
}