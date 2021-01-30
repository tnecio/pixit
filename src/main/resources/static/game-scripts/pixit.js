/*
 * Definition of the Vue app representing the current game
 */

const GAME_STATES_DESCRIPTIONS = {
    "WAITING_FOR_PLAYERS": "All players must press 'Start' to begin!",
    "WAITING_FOR_WORD": "Waiting for the narrator to set the phrase",
    "WAITING_FOR_CARDS": "Waiting for all players to choose a card",
    "WAITING_FOR_VOTES": "Waiting for all players to cast their vote",
    "WAITING_TO_PROCEED": "All players must press 'Proceed' to begin next round!",
    "FINISHED": "Game is over!",
    "CORRUPTED": "Sorry but this game instance is corrupted. No further play is possible"
};

const ROUND_RESULTS_DESCRIPTIONS = {
    "ALL_VOTED_FOR_NARRATOR": "All players voted for the narrator's card. +2 points for everyone except narrator",
    "NO_ONE_VOTED_FOR_NARRATOR": "No one voted for the narrator's card. +1 point for everyone whose card got voted on for each vote",
    "SOMEONE_VOTED_FOR_NARRATOR": "+3 points for the narrator, +1 point for everyone whose card got voted on for each vote",
    "IN_PROGRESS": "Round is in progress..."
};

var pixit = new Vue({
    el: "#pixit",

    created() {
        // TODO preload game values
        this.userId = userId;
        this.requests = new Requester(userId);
        this.GAME_STATES_DESCRIPTIONS = GAME_STATES_DESCRIPTIONS; // TODO proper i18n
        this.ROUND_RESULTS_DESCRIPTIONS = ROUND_RESULTS_DESCRIPTIONS;

        this.console = console; // TODO remove once we get rid of TODOs

        this.game = initialGame;
    },

    data: {
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
                    sentCard: null,
                    startRequested: false,
                    proceedRequested: false,
                    roundPointDelta: 0
                }
            },
            version: 0,
            narrator: null,
            roundResult: "IN_PROGRESS"
        },

        interface: {
            word: null,
            chosenCardId: null
        }
    },

    template: `
<main>
    <section id="game-info">
        <aside id="game-players">
            <playerEntry
                v-for="(player, k) in game.players"
                v-bind:key="player.points"
                v-bind:player="player"
                v-bind:isCurrent="k == userId"
                v-bind:isNarrator="k == game.narrator"
                v-bind:gameState="game.state">
            </playerEntry>
        </aside>
        <div id="game-state">
            <span v-if="game.state === 'WAITING_TO_PROCEED'">{{ROUND_RESULTS_DESCRIPTIONS[game.roundResult]}}<br></span>
            {{GAME_STATES_DESCRIPTIONS[game.state]}}
        </div> <!-- TODO proper 18n, discoverability etc. -->
        <nav id="game-control">
            <button @click="requests.leave()" class="danger">Leave</button>
        </nav>
    </section>
    
    <section id="preStart" v-if="!gameStarted()">
        <button @click="requests.startGame()" v-if="!game.players[userId].startRequested">Start</button>
        <span v-else>Waiting for the other players...</span>
    </section>
    
    <section id="roundEnd" v-if="game.state === 'WAITING_TO_PROCEED'">
        <button @click="requests.proceed()" v-if="!game.players[userId].proceedRequested">Proceed</button>
        <span v-else>Waiting for the other players...</span>
    </section>

    <section id="playersPrivateArea" v-if="gameStarted()">
        <h2>Your deck</h2>

        <div class="deck">
            <card v-for="(card, index) in game.players[userId].deck"
                  v-bind:key="index"
                  v-bind:card="card"
                  v-bind:state="{
                      sendable: canSendCard(),
                      votable: false,
                      choosable: canSetWord(),
                      chosen: card.id === interface.chosenCardId,
                      narrators: false,
                      whoVotedNames: null, owner: null
                  }"
                  @send-card="(id) => requests.sendCard(id)"
                  @choose-card="(id) => { interface.chosenCardId = id; }"
            >
            </card>
        </div>
    </section>
    
    <section id="table" v-if="gameStarted()">
        <form  v-on:submit.prevent="setWord()" id="phrase-set" v-if="canSetWord()">
            <label for="wordInput"><input type="text" name="wordInput" v-model="interface.word" placeholder="Set a phrase"></label>
            <button type="submit"
                v-bind:disabled="!interface.chosenCardId"
                v-bind:title="interface.chosenCardId ? 'Set phrase' : 'Select a card to go with the phrase' "
            >
                Set
            </button>
        </form>
        <header id="phrase" v-else>
            <span v-if="game.word">Phrase:</span>
            <span v-else>Waiting for the narrator...</span>
            <h2 v-if="shouldDisplayWord()">
                {{game.word.value}}
            </h2>
        </header>
        <div class="deck">
            <card
                v-for="(card, index) in game.table"
                v-bind:key="index"
                v-bind:card="card"
                v-bind:state="{
                    sendable: false, votable: canVote(card.id), choosable: false, chosen: false,
                    narrators: game.players[game.narrator].sentCard === card.id,
                    whoVotedNames: getWhoVotedNames(card.id),
                    owner: getOwnerOfCardOnTableName(card.id)
                }"
                @vote-card="(id) => requests.vote(id)"
            >
            </card>
        </div>
    </section>
</main>
    `,

    methods: {
        /* MODEL UPDATE */
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
                this.requests.requestGameState();
                this.console.log("Updating game due to version mismatch between heartbeat and local: " + version + " > " + this.game.version);
            }
        },

        /* STATE */
        gameStarted() {
            return this.game.state !== 'WAITING_FOR_PLAYERS';
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
            return this.game.state !== 'WAITING_FOR_WORD' && this.game.state !== 'WAITING_FOR_PLAYERS' && this.game.word;
        },

        /* ACTIONS */
        setWord() {
            if (this.interface.chosenCardId !== null) {
                this.requests.setWord(this.interface.word, this.interface.chosenCardId);
            } else {
                // TODO an aesthetic warning
            }
        },

        /* WAITING_TO_PROCEED screen functions */
        getWhoVotedNames(cardId) {
            if (this.game.state !== 'WAITING_TO_PROCEED') { return null; }
            let res = [];
            for (let player in this.game.players) {
                if (this.game.players[player].vote === cardId) { res.push(this.game.players[player].name); }
            }
            return res;
        },
        getOwnerOfCardOnTableName(cardId) {
            if (this.game.state !== 'WAITING_TO_PROCEED') { return null; }
            for (let player in this.game.players) {
                if (this.game.players[player].sentCard === cardId) { return this.game.players[player].name; }
            }
        }
    }
});