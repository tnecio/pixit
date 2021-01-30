const EXAMPLE_CARD_STATE = {
    sendable: false,
    votable: false,
    choosable: false,
    chosen: false
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
        v-bind:class="{ chosenCard: state.chosen }"
        class="cardFigureNormal"
    >
        <img
            v-bind:src="card.image.url"
            v-bind:alt="card.image.alt"
            v-bind:class="{ revealed : card.revealed }"
            v-on="state.choosable ? { click: () => $emit('choose-card', card.id) } : card.revealed ? { click: () => enlarge() } : { }"
            v-bind:title="state.choosable ? 'Click to select' : card.revealed ? 'Click to zoom in' : 'Card is hidden' " />
    
        <figcaption>
            <span>{{card.image.attribution}}</span>
            <button type="button" v-on:click="enlarge()" title="Zoom" v-if="card.revealed">🔎</button>
        </figcaption>
    </figure>
    <div class="modal" v-bind:class="showModal ? 'shownModal' : 'hiddenModal'" @click="closeModal()">
        <figure @v-on:click.prevent>
            <img v-bind:src="card.image.url" v-bind:alt="card.image.alt" />
            <figcaption>
                <span>
                    <span>{{card.image.attribution}}</span> <!-- TODO Anchor to source -->
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
        {{player.points}} points
    </div>
    `,
    methods: {
        isThinking: function(player, isCurrent, isNarrator, gameState) {
            if (isCurrent) { return false; }
            if (gameState === 'WAITING_FOR_PLAYERS' && !player.startRequested) { return true; }

            if (isNarrator) { return false; }
            if (gameState === 'WAITING_FOR_VOTES' && player.vote === null) { return true; }
            if (gameState === 'WAITING_FOR_CARDS' && player.sentCard === null) { return true; }
            return false;
        }
    }
});