const EXAMPLE_CARD_STATE = {
    sendable: false,
    votable: false,
    choosable: false,
    chosen: false,
    narrators: false,
    whoVotedNames: [],
    owner: null
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
        <aside v-if="state.owner" class="whoVotedNames">
            <span v-html="t.xyzs_card_fmt(state.owner)"></span>
            <span v-if="state.whoVotedNames && state.whoVotedNames.length > 0">
                <br>{{t.voted_on_by}}
                <b v-for="playerName in state.whoVotedNames"><br>{{playerName}}</b>
            </span>
        </aside>
    </figure>
    <div class="modal" v-bind:class="showModal ? 'shownModal' : 'hiddenModal'" onclick="history.back()">
        <figure>
            <img v-bind:src="card.image.url" v-bind:alt="card.image.alt" />
            <form v-if="state.choosable" id="phrase-set" v-on:submit.prevent="closeModal(); $emit('set-word', card.id, word)" @click.stop>
                <label for="wordInput">
                    <input type="text" name="wordInput" v-model="word" 
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
    props: ['player', 'isCurrent', 'isNarrator', 'gameState'],
    created() {
        this.t = t;
    },
    template: `
    <div
        class="playerEntry"
        v-bind:class="{ narratorPlayerEntry: isNarrator }"
    >
        <b v-bind:title="player.name + (isNarrator ? ' (' + t.narrator + ')' : '')">{{player.name}}</b> 
        <span v-if="isCurrent">({{t.you}})</span>
        <span v-bind:title="t.still_thinking" v-if="isThinking(player, isCurrent, isNarrator, gameState)"> 💭</span>
        <br>
        {{player.points}}
        <b v-if="player.roundPointDelta">+{{player.roundPointDelta}}</b>
        {{t.points_fmt(player.points + player.roundPointDelta)}}
    </div>
    `,
    methods: {
        isThinking: function(player, isCurrent, isNarrator, gameState) {
            if (isCurrent) { return false; }
            if (gameState === 'WAITING_FOR_PLAYERS' && !player.startRequested) { return true; }
            if (gameState === 'WAITING_TO_PROCEED' && !player.proceedRequested) { return true; }

            if (isNarrator) { return false; }
            if (gameState === 'WAITING_FOR_VOTES' && player.vote === null) { return true; }
            if (gameState === 'WAITING_FOR_CARDS' && player.sentCard === null) { return true; }
            return false;
        }
    }
});