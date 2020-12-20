/*
 * Definition of the Vue app representing the current game
 */

var pixit = new Vue({
    el: "#pixit",

    data: {
        pixit: {
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
            player: {
                votedCardId: null,
                cardSent: false,
                isAdmin: false,
                isNarrator: false,
                name: playerName,
                userId: userId
            }
        },

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
            this.pixit.game = newGameCommon;
        },

        wholeGameUpdate: function (newGame) {
            this.pixit.game = newGame.game;
            this.pixit.avatar = newGame.avatar;
            this.pixit.player = newGame.player;
        },

        start: function () {
            console.log("start method called");
            this.pixit.game.state = "WAITING_FOR_WORD"
        },

        setWord: function (word) {
            this.pixit.game.word.value = word;
        }
    }
});