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
        addCard: function () {
            this.cards.push({
                image: {
                    url: "https://i.imgur.com/3IFTi.jpg",
                    alt: "Bienvenidos",
                    attribution: "www.maxitter.com/imgur/"
                }
            })
        }
    }
});