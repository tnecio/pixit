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
            name: "",
            state: "WAITING_FOR_PLAYERS",
            word: null, /* {
                value: string
            }*/
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
            roundResult: "IN_PROGRESS",
            admin: null
        },

        myCards: {
            lastVote: null,
            lastSent: null
        },

        shuffling: false,

        connected: false,

        popups: []
    },

    computed: {
        playerIdsSorted: function () {
            return Object.keys(this.game.players).sort();
        }
    },

    template: `
<main>
    <aside id="errors" v-if="isDisconnected()">
        {{t.connection_lost}}
    </aside>
    
    <div class="popups" v-if="popups.length > 0">
        <aside v-for="popup in popups">
            {{popup.message}}
        </aside>
    </div>

    <section id="common-area">
        <header id="game-info">
            <h1><a href="/">P<span style="font-size:1.2em;">i</span>X<span style="font-size:1.2em;">i</span>T!</a></h1>
            <aside id="game-players">
                <h2 class="playersHeader">{{t.players}}:</h2>
                <playerEntry
                    v-for="k in playerIdsSorted"
                    v-bind:key="k"
                    v-bind:playerId="k"
                    v-bind:player="game.players[k]"
                    v-bind:isCurrent="k == userId"
                    v-bind:isNarrator="k == game.narrator"
                    v-bind:isAdmin="k == game.admin"
                    v-bind:showAdminControls="userId == game.admin"
                    v-bind:gameState="game.state"
                    @kick-out="(playerId) => requests.kickOut(playerId)">
                </playerEntry>
            </aside>
            <span v-if="isRoundEnd()"><br><b v-html="roundEndSummary()"></b></span>
        </header>
    
        <div id="table">
            <section id="game-guide">
                <span id="game-state" v-html="gamePhaseGuide()"></span>
            </section>
            
            <div id="pre-start" v-if="!gameStarted()">
                <button
                    class="copyLinkButton"
                    type="button"
                    onclick="navigator.clipboard.writeText(location)"
                >
                    {{t.copy_to_clipboard}}
                </button>
                <button 
                    @click="requests.startGame()"
                    v-bind:disabled="game.players[userId].startRequested"
                    class="actionButton"
                >
                    {{ game.players[userId].startRequested ? t.waiting_for_others : t.start }}
                </button>
            </div>
            
            <div id="round-end" v-if="isRoundEnd()">
                <button
                    @click="requests.proceed()"
                    v-bind:disabled="game.players[userId].proceedRequested"
                    class="actionButton"
                >
                    {{ game.players[userId].proceedRequested ? t.waiting_for_others : t.proceed }}
                </button>
            </div>
    
            <transition name="shufflingmove" mode="out-in">
            <transition-group v-if="!shuffling" tag="div" class="deck" name="cardmove" key="deck">
                <card
                    v-for="(card, index) in game.table"
                    v-bind:key="card.id"
                    v-bind:card="card"
                    v-bind:state="{
                        sendable: false, votable: canVote(card.id), choosable: false, chosen: false,
                        narrators: game.state === 'WAITING_TO_PROCEED' && game.players[game.narrator].sentCard === card.id,
                        whoVotedNames: getWhoVotedNames(card.id),
                        owner: getOwnerOfCardOnTableName(card.id),
                        sentByPlayer: myCards.lastSent === card.id, votedByPlayer: myCards.lastVote === card.id
                    }"
                    @vote-card="(id) => { requests.vote(id); myCards.lastVote = id; }"
                >
                </card>
            </transition-group>
            <div v-else class="deck" id="shufflingArea" key="shufflingArea">
                {{t.shuffling}}
            </div>
            </transition>
        </div>
    </section>
    
    <section id="players-private-area" v-if="gameStarted()">
        <h2>{{t.your_deck}}</h2>

        <transition-group tag="div" class="deck" name="cardmove">
            <card v-for="(card, index) in game.players[userId].deck"
                  v-bind:key="card.id"
                  v-bind:card="card"
                  v-bind:state="{
                      sendable: canSendCard(),
                      votable: false,
                      choosable: canSetWord(),
                      chosen: false,
                      narrators: false,
                      whoVotedNames: null, owner: null, sentByPlayer: false, votedByPlayer: false
                  }"
                  @send-card="(id) => { requests.sendCard(id); myCards.lastSent = id; }"
                  @set-word="(id, word) => requests.setWord(id, word)"
            >
            </card>
        </transition-group>
    </section>
</main>
    `,

    methods: {
        /* MODEL UPDATE */
        update: function (newGame) {
            if (newGame.version > this.game.version) {
                if (this.game.state === "WAITING_FOR_CARDS" && newGame.state === "WAITING_FOR_VOTES") {
                    this.shuffling = true;
                    setTimeout(() => { this.shuffling = false; }, 3000);
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
        shouldDisplayTable() {
            return this.gameStarted() && this.game.state !== "WAITING_FOR_WORD";
        },
        shouldDisplayWord() {
            return this.game.state !== 'WAITING_FOR_WORD' && this.game.state !== 'WAITING_FOR_PLAYERS' && this.game.word;
        },
        isRoundEnd() {
            return this.game.state === 'WAITING_TO_PROCEED';
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
            return (this.connected !== true);
        },

        /* POP-UPS */
        displayMessage(msg) {
            const t = new Date();
            this.popups.push({
                message: msg,
                ts: t
            });
            setTimeout(() => {
                this.popups = this.popups.filter((x) => x.ts !== t);
            }, 7500);
        },

        /* USER GUIDE */
        gamePhaseGuide() {
            const phrase = this.game.word ? `<span class="phrase">${this.game.word.value}</span>` : null;

            if (this.game.state === 'WAITING_FOR_PLAYERS') {
                const numberOfPlayersInTheGame = Object.keys(this.game.players).length;
                if (numberOfPlayersInTheGame < 3) {
                    return t.waiting_for_players;
                } else {
                    return t.press_start_to_begin;
                }
            } else if (this.game.state === 'WAITING_FOR_WORD') {
                if (this.game.narrator === userId) {
                    return t.choose_card_and_set_phrase;
                } else {
                    return t.waiting_for_narrator;
                }
            } else if (this.game.state === 'WAITING_FOR_CARDS') {
                if (this.game.players[userId].sentCard) {
                    return t.waiting_for_cards_fmt(phrase);
                } else {
                    return t.do_send_card_fmt(phrase);
                }
            } else if (this.game.state === 'WAITING_FOR_VOTES') {
                if (this.game.narrator === userId || this.game.players[userId].vote) {
                    return t.waiting_for_votes_fmt(phrase);
                }
                return t.do_vote_fmt(phrase);
            } else if (this.game.state === 'WAITING_TO_PROCEED') {
                return t.waiting_to_proceed_fmt(phrase);
            } else if (this.game.state === 'FINISHED') {
                return t.finished;
            } else {
                return t.corrupted;
            }
        },

        /* ROUND END SUMMARY */
        roundEndSummary() {
            if (this.game.roundResult === 'ALL_VOTED_FOR_NARRATOR') {
                return t.all_voted_for_narrator;
            } else if (this.game.roundResult === 'NO_ONE_VOTED_FOR_NARRATOR') {
                return t.no_one_voted_for_narrator;
            } else if (this.game.roundResult === 'SOMEONE_VOTED_FOR_NARRATOR') {
                return t.someone_voted_for_narrator;
            } else {
                return "";
            }
        }
    }
});