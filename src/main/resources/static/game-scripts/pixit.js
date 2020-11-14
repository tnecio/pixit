var pixit = new Vue({
    el: "#pixit",
    data: {
        cards: [
            {
                image: {
                    url: "https://i.imgur.com/3IFTi.jpg",
                    alt: "Bienvenidos",
                    attribution: "www.maxitter.com/imgur/"
                }
            },
            {image: image}
        ]
    },
    methods: {
        requestAddCard: function() {
            sendRequest(new AddCardRequest())
        },
        addCard: function (image) {
            this.cards.push({
                // image: {
                //     url: "https://i.imgur.com/3IFTi.jpg",
                //     alt: "Bienvenidos",
                //     attribution: "www.maxitter.com/imgur/"
                // }
                image: image
            })
        }
    }
});