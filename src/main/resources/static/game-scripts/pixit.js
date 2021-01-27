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
                    sentTheirCard: false
                }
            },
            version: 0,
            narrator: null,
            admin: null
        },

        interface: {
            word: null
        }
    },

    template: `
<main>
    <section class="game-info">
        <li style="display: block">
            <ul><h3>{{GAME_STATES_DESCRIPTIONS[game.state]}}</h3></ul>
            <playerEntry
                v-for="(player, index) in game.players"
                v-bind:key="index"
                v-bind:player="player">
            </playerEntry>
        </li>
        <div>
            <button @click="requests.startGame()" v-if="canStartGame()">Start</button>
            <button @click="console.log('TODO')" v-if="canFinishGame()">Finish game for all</button>
            <button @click="console.log('TODO')">Leave Game</button>
        </div>
    </section>
    
    <section class="phrase">
        <div v-if="canSetWord()">
            <label for="wordInput"><input type="text" name="wordInput" v-model="interface.word" placeholder="Set a phrase"></label>
            <button @click="requests.setWord(interface.word)">Set</button>
        </div>
        <h2 v-if="shouldDisplayWord()">
            {{game.word.value}}
        </h2>
    </section>

    <section class="table" v-if="gameStarted()">
        <h3>Table</h3>
        <div class="deck">
            <card
                v-for="(card, index) in game.table"
                v-bind:key="index" v-bind:index="index"
                v-bind:card="card"
                v-bind:state="{ displayed: false, sendable: false, votable: false }">
            </card>
        </div>
    </section>

    <section v-if="gameStarted()">
        <h3>Player {{game.players[userId].name}}</h3>

        <div class="deck">
            <card v-for="(card, index) in game.players[userId].deck"
                  v-bind:key="index" v-bind:index="index"
                  v-bind:card="card"
                  v-bind:state="{ displayed: true, sendable: true, votable: false }"
            >
            </card> <!-- TODO: send-card, vote-card -->
        </div>
    </section>
</main>
    `,

    methods: {
        update: function (newGame) {
            if (newGame.version > this.game.version) {
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
        shouldDisplayWord() {
            return this.game.state !== 'WAITING_FOR_WORD' && this.game.state !== 'WAITING_FOR_PLAYERS';
        }
    }
});