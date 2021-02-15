var pixit = new Vue({
    el: "#pixit",

    created() {
        this.userId = userId;
        this.requests = new Requester(userId);
        this.game = initialGame;
        this.t = t;
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
        },

        connected: true
    },

    template: `
<main>
    <aside id="errors" v-if="!connected">
        {{t.connection_lost}}
    </aside>

    <section id="game-info">
        <aside id="game-players"> <!-- TODO in the order of playing! -->
            <playerEntry
                v-for="(player, k) in game.players"
                v-bind:key="k"
                v-bind:player="player"
                v-bind:isCurrent="k == userId"
                v-bind:isNarrator="k == game.narrator"
                v-bind:gameState="game.state">
            </playerEntry>
        </aside>
        <div id="game-state" v-html="gamePhaseGuide()">
        </div>
    </section>
    
    <section id="preStart" v-if="!gameStarted()">
        <button @click="requests.startGame()" v-bind:disabled="game.players[userId].startRequested">
            {{ game.players[userId].startRequested ? t.waiting_for_others : t.start }}
        </button>
    </section>
    
    <section id="roundEnd" v-if="game.state === 'WAITING_TO_PROCEED'">
        <button @click="requests.proceed()" v-bind:disabled="game.players[userId].proceedRequested">
            {{ game.players[userId].proceedRequested ? t.waiting_for_others : t.proceed }}
        </button>
    </section>

    <section id="table" v-if="gameStarted()">
        <form  v-on:submit.prevent="setWord()" id="phrase-set" v-if="canSetWord()">
            <label for="wordInput">
                <input type="text" name="wordInput" v-model="interface.word" 
                v-bind:placeholder="t.select_card">
            </label>
            <button type="submit"
                v-bind:disabled="!interface.chosenCardId"
                v-bind:title="interface.chosenCardId ? t.set_phrase : t.select_card"
            >
                {{t.set}}
            </button>
        </form>
        <header id="phrase" v-else>
            <span v-if="game.word">{{t.phrase}}</span>
            <span v-else>{{t.waiting_for_narrator}}...</span>
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
                    narrators: game.state === 'WAITING_TO_PROCEED' && game.players[game.narrator].sentCard === card.id,
                    whoVotedNames: getWhoVotedNames(card.id),
                    owner: getOwnerOfCardOnTableName(card.id)
                }"
                @vote-card="(id) => requests.vote(id)"
            >
            </card>
        </div>
    </section>
    
        <section id="playersPrivateArea" v-if="gameStarted()">
        <h2>{{t.your_deck}}</h2>

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
                console.log("Out of order game update " + newGame.version + " < " + this.game.version);
            }
        },
        updateIfVersionIsNewer: function (version) {
            if (version > this.game.version) {
                this.requests.requestGameState();
                console.log("Updating game due to version mismatch between heartbeat and local: " + version + " > " + this.game.version);
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
        },

        /* CONNECTION ISSUES */
        connectionLost() {
            this.connected = false;
        },
        startConnecting() {
            this.connected = "connecting"; // TODO use one type for this.connected
        },
        connectionUp() {
            this.connected = true;
        },
        isDisconnected() {
            return (this.connected === false);
        },

        /* USER GUIDE */
        gamePhaseGuide() {
            if (this.game.state === 'WAITING_FOR_PLAYERS') {
                if (Object.keys(this.game.players).length) {
                    return t.waiting_for_players;
                } else {
                    return t.press_start_to_begin;
                }
            } else if (this.game.state === 'WAITING_FOR_WORD') {
                if (this.game.narrator === userId) {
                    if (this.interface.chosenCardId) {
                        return t.do_set_phrase;
                    }
                    return t.choose_card_and_set_phrase;
                } else {
                    return t.waiting_for_narrator;
                }
            } else if (this.game.state === 'WAITING_FOR_CARDS') {
                if (this.game.players[userId].sentCard) {
                    return t.waiting_for_cards;
                } else {
                    return t.do_send_card;
                }
            } else if (this.game.state === 'WAITING_FOR_VOTES') {
                if (this.game.players[userId].vote) {
                    return t.waiting_for_votes;
                }
                return t.do_vote;
            } else if (this.game.state === 'WAITING_TO_PROCEED') {
                let res;
                if (this.game.roundResult === 'ALL_VOTED_FOR_NARRATOR') {
                    res = t.all_voted_for_narrator;
                } else if (this.game.roundResult === 'NO_ONE_VOTED_FOR_NARRATOR') {
                    res = t.no_one_voted_for_narrator;
                } else if (this.game.roundResult === 'SOMEONE_VOTED_FOR_NARRATOR') {
                    res = t.someone_voted_for_narrator;
                } else {
                    return t.corrupted;
                }
                return t.waiting_to_proceed + "<br><b>" + res + "</b>"
            } else if (this.game.state === 'FINISHED') {
                return t.finished;
            } else {
                return t.corrupted;
            }
        }
    }
});