const EXAMPLE_CARD_STATE = {
    sendable: false,
    votable: false,
    choosable: false,
    chosen: false
};

Vue.component('card', {
    props: ['card', 'state'],
    data: function() { return {
        showModal: false
    }; },
    template: `
<div>
    <figure
        v-bind:class="{ chosenCard: state.chosen }"
        class="cardFigureNormal"
    >
        <img v-bind:src="card.image.url" v-bind:alt="card.image.alt" @click="enlarge()" title="Click to enlarge" />
    
        <figcaption>
            <span>{{card.image.attribution}}</span>
            <button type="button" v-if="state.sendable" v-on:click="$emit('send-card', card.id)" title="Send">✅</button>
            <button type="button" v-if="state.votable" v-on:click="$emit('vote-card', card.id)" title="Vote">✅</button>
            <button type="button" v-if="state.choosable" v-on:click="$emit('choose-card', card.id)" title="Select">✅</button>
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
        enlarge: function() {
            this.showModal = true;
        },

        closeModal: function() {
            this.showModal = false;
        }
    }
});

Vue.component('playerEntry', {
    props: ['player', 'isCurrent', 'isNarrator', 'isAdmin'],
    template: `
    <div class="playerEntry" v-bind:class="{ currentPlayerEntry: isCurrent }">
        <b>{{player.name}}</b>
        <span class="separator" title="Administrator" v-if="isAdmin"> | 👮 </span>
        <span class="separator" title="Narrator" v-if="isNarrator"> | 📜 </span>
        <span class="separator" title="Player voted for a card" v-if="player.vote != null"> | ✅</span>
        <span class="separator" title="Player sent their card" v-if="player.sentCard != null"> | 🃏</span>
        <br>
        {{player.points}} points
    </div>
    `
});