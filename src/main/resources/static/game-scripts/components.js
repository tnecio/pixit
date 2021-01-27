const EXAMPLE_CARD_STATE = {
    sendable: false,
    votable: false,
    choosable: false,
    chosen: false
};

Vue.component('card', {
    props: ['card', 'state'],
    template: `
<figure
    v-on="state.choosable ? { click: () => $emit('choose-card', card.id) } : {}"
    v-bind:class="{ chosenCard: state.chosen }"
>
    <img v-bind:src="card.image.url" v-bind:alt="card.image.alt" />

    <figcaption>
        <div>{{card.image.attribution}}, Unsplash</div>
        <div>
            <button type="button" v-if="state.sendable" v-on:click="$emit('send-card', card.id)">✅ Send</button>
            <button type="button" v-if="state.votable" v-on:click="$emit('vote-card', card.id)">✅ Vote</button>
        </div>
    </figcaption>
</figure>`
});

Vue.component('playerEntry', {
    props: ['player', 'isCurrent'],
    template: `
    <ul v-bind:class="{ currentPlayerEntry: isCurrent }" >
        <b>{{player.name}}</b> <br> ({{player.points}} points)
        <span title="Player voted for a card" v-if="player.vote != null">✅</span>
        <span title="Player sent their card" v-if="player.sentTheirCard">🃏</span>
    </ul>
    `
});