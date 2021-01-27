/*
 * Definition of the Vue app representing the current game
 */

const GAME_STATES_DESCRIPTIONS = {
    "WAITING_FOR_PLAYERS": "Awaiting start by game-admin",
    "WAITING_FOR_WORD": "Waiting for the narrator to set the phrase",
    "WAITING_FOR_CARDS": "Waiting for all players to choose a card",
    "WAITING_FOR_VOTES": "Waiting for all players to cast their vote",
    "WAITING_TO_PROCEED": "Waiting for the game-admin to proceed to the next round",
    "FINISHED": "Game is over!",
    "CORRUPTED": "Sorry but this game instance is corrupted. No further play is possible"
};

var pixit = new Vue({
    el: "#pixit",

    created() {
        this.userId = userId;
        this.requests = new Requester(userId);
        this.GAME_STATES_DESCRIPTIONS = GAME_STATES_DESCRIPTIONS;
        this.console = console; // TODO remove once we get rid of TODOs
    },

    data: { // TODO: preload data with values of the player on http request??
        game: {
            state: "WAITING_FOR_PLAYERS",
            word: {
                value: null
            },
            table: [],
            players: {
                [userId]: {
                    name: playerName,
                    deck: [],
                    points: 0,
                    vote: null,
                    sentCard: null
                }
            },
            version: 0,
            narrator: null,
            admin: null
        },

        interface: {
            word: null,
            chosenCardId: null
        }
    },

    template: `
<main>
    <section id="game-info">
        <div id="game-state">
            <h3>{{GAME_STATES_DESCRIPTIONS[game.state]}}</h3>
            <playerEntry
                v-for="(player, index) in game.players"
                v-bind:key="index"
                v-bind:player="player">
            </playerEntry>
        </div>
        <div id="game-control">
            <button @click="requests.startGame()" v-if="canStartGame()">Start</button>
            <button @click="console.log('TODO')" v-if="canFinishGame()">Finish game for all</button>
            <button @click="requests.proceed()" v-if="canProceed()">Proceed</button>
            <button @click="console.log('TODO')">Leave Game</button>
        </div>
    </section>
    
    <section id="phrase">
        <div v-if="canSetWord()">
            <label for="wordInput"><input type="text" name="wordInput" v-model="interface.word" placeholder="Set a phrase"></label>
            <button @click="setWord()">Set</button>
        </div>
        <h2 v-if="shouldDisplayWord()">
            {{game.word.value}}
        </h2>
    </section>

    <section id="table" v-if="gameStarted()">
        <h3>Table</h3>
        <div class="deck">
            <card
                v-for="(card, index) in game.table"
                v-bind:key="index"
                v-bind:card="card"
                v-bind:state="{ sendable: false, votable: canVote(card.id), choosable: false, chosen: false }"
                @vote-card="(id) => requests.vote(id)"
            >
            </card>
        </div>
    </section>

    <section id="playersPrivateArea" v-if="gameStarted()">
        <h3>Player {{game.players[userId].name}}</h3>

        <div class="deck">
            <card v-for="(card, index) in game.players[userId].deck"
                  v-bind:key="index"
                  v-bind:card="card"
                  v-bind:state="{ sendable: canSendCard(),
                  votable: false,
                  choosable: canSetWord(),
                  chosen: card.id === interface.chosenCardId }"
                  @send-card="(id) => requests.sendCard(id)"
                  @choose-card="(id) => { interface.chosenCardId = id; }"
            >
            </card>
        </div>
    </section>
</main>
    `,

    methods: {
        update: function (newGame) {
            if (newGame.version > this.game.version) {
                if (newGame.state === 'WAITING_FOR_WORD' && this.game.state !== 'WAITING_FOR_WORD') {
                    this.interface.word = null;
                }

                this.game = newGame;
            } else {
                this.console.log("Out of order game update " + newGame.version + " < " + this.game.version);
            }
        },
        updateIfVersionIsNewer: function (version) {
            if (version > this.game.version) {
                console.log("I (pixit) got called with version " + version);
                this.requests.requestGameState();
                this.console.log("Updating game due to version mismatch between heartbeat and local: " + version + " > " + this.game.version);
            }
        },

        gameStarted() {
            return this.game.state !== 'WAITING_FOR_PLAYERS';
        },
        canStartGame() {
            return this.game.admin === userId && this.game.state === "WAITING_FOR_PLAYERS";
        },
        canFinishGame() {
            return this.game.admin === userId && this.game.state !== "WAITING_FOR_PLAYERS";
        },
        canSetWord() {
            return this.game.state === "WAITING_FOR_WORD" && this.game.narrator === userId;
        },
        canVote(cardId) {
            return this.game.state === "WAITING_FOR_VOTES" && this.game.players[userId].vote === null
                && this.game.narrator !== userId && this.game.players[userId].sentCard !== cardId;
        },
        canSendCard() {
            return this.game.state === "WAITING_FOR_CARDS" && this.game.players[userId].sentCard === null;
        },
        shouldDisplayWord() {
            return this.game.state !== 'WAITING_FOR_WORD' && this.game.state !== 'WAITING_FOR_PLAYERS';
        },
        canProceed() {
            return this.game.admin === userId && this.game.state === "WAITING_TO_PROCEED";
        },

        setWord() {
            if (this.interface.chosenCardId !== null) {
                this.requests.setWord(this.interface.word, this.interface.chosenCardId);
            } else {
                // TODO an aesthetic warning
            }
        }
    }
});