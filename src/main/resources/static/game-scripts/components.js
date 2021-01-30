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
    data: function () {
        return {
            showModal: false
        };
    },
    template: `
<div>
    <figure
        v-bind:class="{ chosenCard: state.chosen || state.narrators }"
        class="cardFigureNormal"
    >
        <aside v-if="state.owner" class="cardOwner">
            <b>{{state.owner}}</b>'s card
        </aside>
        <img
            v-bind:src="card.image.url"
            v-bind:alt="card.image.alt"
            v-bind:class="{ revealed : card.revealed }"
            v-on="card.revealed ? { click: () => enlarge() } : { }"
            v-bind:title="card.revealed ? 'Click to zoom in' : 'Card is hidden' " />
    
        <figcaption>
            <span>{{card.image.attribution}}</span>
        </figcaption>
        <aside v-if="state.whoVotedNames && state.whoVotedNames.length > 0" class="whoVotedNames">
            Voted on by: <br>
            <ul>
                <li v-for="playerName in state.whoVotedNames">{{playerName}}</li>
            </ul>
        </aside>
    </figure>
    <div class="modal" v-bind:class="showModal ? 'shownModal' : 'hiddenModal'" @click="closeModal()">
        <figure @v-on:click.prevent> <!-- TODO this click.prevent does not work as intended -->
            <img v-bind:src="card.image.url" v-bind:alt="card.image.alt" />
            <figcaption>
                <span>
                    <span>{{card.image.attribution}}</span> <!-- TODO Anchor to source, Improve caption -->
                    <span class="separator"> | </span>
                    <i>{{card.image.alt}}</i>
                </span>
                <button type="button" v-if="state.sendable" v-on:click="$emit('send-card', card.id)">Send ✅</button>
                <button type="button" v-if="state.votable" v-on:click="$emit('vote-card', card.id)">Vote ✅</button>
                <button type="button" v-if="state.choosable" v-on:click="$emit('choose-card', card.id)">Select ✅</button>
            </figcaption>
        </figure>
    </div>
</div>`,
    methods: {
        enlarge: function () {
            this.showModal = true;
        },

        closeModal: function () {
            this.showModal = false;
        }
    }
});

Vue.component('playerEntry', {
    props: ['player', 'isCurrent', 'isNarrator', 'gameState'],
    template: `
    <div class="playerEntry" v-bind:class="{ currentPlayerEntry: isCurrent }">
        <b>{{player.name}}</b>
        <span title="Narrator" v-if="isNarrator"> 📜 </span>
        <span title="still thinking..." v-if="isThinking(player, isCurrent, isNarrator, gameState)"> 💭</span>
        <br>
        {{player.points}}<b v-if="player.roundPointDelta">+{{player.roundPointDelta}}</b> points
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