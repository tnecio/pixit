/*
 * Definition of the Vue app representing the current game
 */

var pixit = new Vue({
    el: "#pixit",

    data: {
        game: {
            state: "WAITING_FOR_PLAYERS",
            word: {
                value: ""
            },
            table: [],
            players: []
        },
        avatar: {
            name: "(Loading player's name)",
            deck: [],
            points: 0
        },
        isAdmin: false,
        isNarrator: false,

        ui: {
            chosenCardId: null
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

        commonPartsGameUpdate: function (newGameCommon) {
            this.game = newGameCommon;
        },

        wholeGameUpdate: function (newGame) {
            this.game = newGame.game;
            this.avatar = newGame.avatar;
            this.isAdmin = newGame.isAdmin;
            this.isNarrator = newGame.isNarrator;
        },

        updateGame: function (newGame) {
            console.log("updateGame called"); // DEBUG
            // TODO update all fields
            this.game.word = newGame.word;
            this.game.state = newGame.game.state;
            this.avatar.deck = newGame.player.deck;
        },

        addCard: function (card) {
            this.game.table.push({
                image: card.image
            })
        },

        start: function () {
            console.log("start method called");
            this.game.state = "WAITING_FOR_WORD"
        },

        setWord: function (word) {
            this.game.word.value = word;
        }
    }
});