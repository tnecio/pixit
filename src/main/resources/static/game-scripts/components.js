Vue.component('card', {
    props: ['card', 'chosenCardId'],
    template: `
<figure>
    <img v-bind:src="card.image.url" v-bind:alt="card.image.alt"/>

    <figcaption
    v-bind:style= "[card.image.id == chosenCardId ? {styleA} : {styleB}]"
    >{{image.attribution}}, Unsplash <a click="chooseCard({{card.id}})">✅</a></figcaption>
</figure>`
});

Vue.component('word', {
    props: ['value'],
    template: `
    <h2>{{value}}</h2>`
});

Vue.component('playerEntry', {
    props: ['player'],
    template: `
    <ul>{{player.name}} ({{player.points}}</ul>
    `
});

Vue.component('gamestate-info', {
    props: ['game', 'avatar', 'is-admin', 'is-narrator'],
    template: `
<div>
    <h3>{{game.state}}</h3> Player name: {{avatar.name}} ({{avatar.points}} pts)
    <span v-if="is-admin"> (ADMIN) </span>
    <span v-if="is-narrator"> (NARRATOR) </span>
    <li>
        <playerEntry v-for="(player, index) in game.players" v-bind:player="player"></playerEntry>
    </li>
</div>
`
});