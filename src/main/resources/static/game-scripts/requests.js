class Request {
    constructor(_name) {
        this.name = _name
    }
    send() {
        sendRequest(this);
    }
}

let requests = {
    addCard() {
        return new Request("AddCard");
    }
};