class RequestWrapper {
    constructor(_request) {
        this.request = _request;
        this.name = _request.name;
    }

    send() {
        sendRequest(this);
    }
}

class Request {
    constructor(_name) {
        this.name = _name
    }
}

let requests = {
    addCard() {
        return new Request("DrawCard");
    }
};