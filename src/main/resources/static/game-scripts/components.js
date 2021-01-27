const EXAMPLE_CARD_STATE = {
    displayed: false,
    sendable: false,
    votable: false
};

Vue.component('card', {
    props: ['index', 'card', 'state'],
    template: `
<figure
    v-on:click="$emit('choose-card', card.id)"
>
    <img v-if="state.displayed" v-bind:src="card.image.url" v-bind:alt="card.image.alt" />
    <img v-if="!(state.displayed)" src="TODO" alt="Card's back" />

    <figcaption>
        <div>{{card.image.attribution}}, Unsplash</div>
        <div>
            <a v-if="state.sendable" v-on:click="$emit('send-card', index)">✅ Send</a>
            <a v-if="state.votable" v-on:click="$emit('vote-card', index)">✅ Vote</a>
        </div>
    </figcaption>
</figure>`
});

Vue.component('playerEntry', {
    props: ['player', 'isCurrent'],
    template: `
    <ul v-bind:class="{ currentPlayerEntry: isCurrent }" >
        {{player.name}} ({{player.points}} points)
        <span title="Player voted for a card" v-if="player.vote != null">✅</span>
        <span title="Player sent their card" v-if="player.sentTheirCard">🃏</span>
    </ul>
    `
});