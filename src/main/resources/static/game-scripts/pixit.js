/*
 * Definition of the Vue app representing the curent game
 */

var pixit = new Vue({
    el: "#pixit",

    data: {
        table: [
            {image: image}
        ],
        player: {
            name: "(Loading player's name)",
            deck: [
                {image: image}
            ],
            points: 0
        },
        word: {
            value: "No word set"
        },
        game: {
            state: "WAITING_FOR_PLAYERS"
        },

        requests: requests
    },

    methods: {
        executeRequest: function(request) {
            console.log(request)
            request().send()
        },

        addCard: function (card) {
            this.table.push({
                image: card.image
            })
        }
    }
});