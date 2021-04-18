const EXAMPLE_CARD_STATE = {
    sendable: false,
    votable: false,
    choosable: false,
    chosen: false,
    narrators: false,
    whoVotedNames: [],
    owner: null,
    sentByPlayer: false,
    votedByPlayer: false
};

Vue.component('card', {
    props: ['card', 'state'],
    created() {
        this.t = t;
    },
    data: function () {
        return {
            showModal: false,
            word: null
        };
    },
    template: `
<div>
    <figure
        v-bind:class="{ chosenCard: state.chosen || state.narrators }"
        v-bind:title="state.narrators ? t.narrators_card : ''"
        class="cardFigureNormal"
    >
        <img
            v-bind:src="card.image.url"
            v-bind:alt="card.image.alt"
            v-bind:class="{ revealed : card.revealed }"
            v-on="card.revealed ? { click: () => enlarge() } : { }"
            v-bind:title="card.revealed ? t.click_to_zoom_in : t.card_is_hidden " />
        <figcaption v-if="card.image.attribution">
            <span v-html="card.image.attribution"></span>
        </figcaption>
        <aside v-if="state.owner || state.votedByPlayer" class="whoVotedNames">
            <span v-if="state.owner" v-html="t.xyzs_card_fmt(state.owner)"></span>
            <span v-if="state.whoVotedNames && state.whoVotedNames.length > 0">
                <br>{{t.voted_on_by}}
                <b v-for="playerName in state.whoVotedNames"><br>{{playerName}}</b>
            </span>
            <span v-if="state.votedByPlayer && !state.owner">{{t.you_voted_for_this_card}}</span>
        </aside>
    </figure>
    <div class="modal" v-bind:class="showModal ? 'shownModal' : 'hiddenModal'" onclick="history.back()">
        <figure>
            <img v-bind:src="card.image.url" v-bind:alt="card.image.alt" />
            <form v-if="state.choosable" id="phrase-set" v-on:submit.prevent="closeModal(); $emit('set-word', card.id, word)" @click.stop>
                <label for="wordInput">
                    <input type="text" name="wordInput" autocomplete="off" v-model="word" 
                    v-bind:placeholder="t.set_phrase">
                </label>
                <button type="submit"
                    v-bind:disabled="!word"
                    v-bind:title="t.select"
                >
                    {{t.select}}
                </button>
            </form>
            <figcaption>
                <span>
                    <span v-html="card.image.attribution"></span>
                    <span class="separator"> | </span>
                    <i>{{card.image.description ? card.image.description : card.image.alt}}</i>
                </span>
                <button type="button" v-if="state.sendable" v-on:click="$emit('send-card', card.id)">{{t.send}} ✅</button>
                <button type="button" v-if="state.votable" v-on:click="$emit('vote-card', card.id)">{{t.vote}} ✅</button>
                <button type="button" v-if="state.sentByPlayer" disabled>{{t.this_is_your_card}} ✅</button>
            </figcaption>
        </figure>
    </div>
</div>`,
    methods: {
        handleHashchange: function (e) {
            this.showModal = window.location.hash === "#modal" + this.card.id;
        },

        enlarge: function () {
            this.showModal = true;

            window.location.hash = "#modal" + this.card.id;
            window.addEventListener('hashchange', this.handleHashchange);
        },

        closeModal: function () {
            window.removeEventListener('hashchange', this.handleHashchange);

            this.showModal = false;
        }
    }
});

Vue.component('playerEntry', {
    props: ['playerId', 'player', 'isCurrent', 'isNarrator', 'isAdmin', 'isWinner', 'gameState', 'showAdminControls'],
    created() {
        this.t = t;
    },
    template: `
    <tr
        class="playerEntry"
        v-bind:class="{ narratorPlayerEntry: isNarrator }"
    >
    <td>
        <b v-bind:title="player.name + (isNarrator ? ' (' + t.narrator + ')' : '')">{{player.name}}</b>
        <span v-if="isWinner">🎉</span>
        <span v-if="isCurrent">({{t.you}})</span>
        <span v-if="isAdmin" style="color: darkred;" v-bind:title="t.adminTitle">({{t.admin}})</span>
        <span v-if="showAdminControls && !isAdmin">
            <a
                href="javascript:void()"
                v-bind:title="t.kick_out"
                style="color: darkred;"
                v-on:click="kickOut()"
            >
                <button type="button"><b>X</b></button>
            </a>
        </span>
        <span v-bind:title="t.still_thinking" v-if="isThinking(player, isCurrent, isNarrator, gameState)"> 💭</span>
    </td>
    
    <td>
        {{player.points}}
        <b v-if="player.roundPointDelta">+{{player.roundPointDelta}}</b>
    </td>
    </tr>
    `,
    methods: {
        isThinking: function (player, isCurrent, isNarrator, gameState) {
            if (isCurrent) {
                return false;
            }
            if (gameState === 'WAITING_FOR_PLAYERS' && !player.startRequested) {
                return true;
            }
            if (gameState === 'WAITING_TO_PROCEED' && !player.proceedRequested) {
                return true;
            }

            if (isNarrator) {
                return false;
            }
            if (gameState === 'WAITING_FOR_VOTES' && player.vote === null) {
                return true;
            }
            return gameState === 'WAITING_FOR_CARDS' && player.sentCard === null;

        },

        kickOut: function() {
            let sure = confirm(t.confirm_kickout + this.player.name + "?");
            if (sure) {
                this.$emit('kick-out', this.playerId);
            }
        }
    }
});

const EXAMPLE_MESSAGE = {
    author: "Someone",
    time: "21:37",
    content: "Hi!"
};

Vue.component('chat', {
    props: ['messages'],
    created() {
        this.t = t;
    },
    data: function () {
        return {
            newMessage: "",
            shown: true
        };
    },
    computed: {
        reverseMessages: function() {
            // not using .reverse() to avoid modifying the original array
            const msgs = this.messages;
            return msgs.map((item, idx) => msgs[msgs.length - 1 - idx])
        }
    },
    template: `
    <aside class="chat">
        <a href="javascript:void(0);" v-if="shown" v-on:click="shown = false" class="chatVisibLink">{{t.hide_chat}}</a>
        <a href="javascript:void(0);" v-if="!shown" v-on:click="shown = true" class="chatVisibLink">{{t.show_chat}}</a>
        <div class="messages" v-if="shown">
            <div class="message" v-for="message in reverseMessages">
                <b class="author">{{message.author}}</b> <span class="time">{{getTimeStr(message.time)}}</span>: {{message.content}}
            </div>
        </div>
        <form class="new-message" v-on:submit.prevent="sendMessage()" v-if="shown">
            <input v-model="newMessage" placeholder="Chat">
            <button>&gt;</button>
        </form>
    </aside>
`,
    methods: {
        getTimeStr: function(timeStr) {
            let d = new Date(timeStr);
            let hour = d.getHours();
            if (hour < 10) hour = "0" + hour;
            let minutes = d.getMinutes();
            if (minutes < 10) minutes = "0" + minutes;
            return hour + ":" + minutes;
        },

        sendMessage: function () {
            this.$emit('send-message', this.newMessage);
            this.newMessage = "";
        }
    }
});