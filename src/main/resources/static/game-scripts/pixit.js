/*
 * Definition of the Vue app representing the current game
 */

var pixit = new Vue({
    el: "#pixit",

    data: {
        table: [],
        player: {
            name: "(Loading player's name)",
            deck: [],
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
        executeRequest: function (requestFactory) {
            let request = requestFactory();
            sendRequest(gameControlEndpoint(request.endpoint),
                {payload: request.payload, userId: userId}
                );
        },

        updateGame: function (newGame) {
            console.log("updateGame called"); // DEBUG
            // TODO update all fields
            this.word = newGame.word;
            this.game = newGame.game;
            this.player.deck = newGame.player.deck;
        },

        addCard: function (card) {
            this.table.push({
                image: card.image
            })
        },

        start: function () {
            console.log("start method called");
            this.game.state = "WAITING_FOR_WORD"
        },

        setWord: function (word) {
            this.word.value = word;
        }
    }
});