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
    props: ['pixit'],
    template: `
<div>
    <h3>{{pixit.game.state}}</h3> Player name: {{pixit.avatar.name}} ({{pixit.avatar.points}} pts)
    <span v-if="pixit.player.isAdmin"> (ADMIN) </span>
    <span v-if="pixit.player.isNarrator"> (NARRATOR) </span>
    <li>
        <playerEntry
            v-for="(player, index) in pixit.game.players"
            v-bind:key="index"
            v-bind:player="player">
        </playerEntry>
    </li>
</div>
`
});

Vue.component('playerdeck', {
    props: ['pixit'],
    template: `
    <section>
        <h3>Player {{pixit.player.name}}</h3>

        <div class="deck">
            <card v-for="(card, index) in pixit.avatar.deck"
                  v-bind:key="index"
                  v-bind:card="card"
                  v-bind:chosenCardId="ui.chosenCardId">
            </card>
        </div>
    </section>    
`
});

Vue.component('pixittable', {
    props: ['pixit'],
    template: `
<section class="table">
    <h3>Table</h3>
    <div class="deck">
    <card v-for="(card, index) in pixit.table" v-bind:key="index" v-bind:card="card"></card>
    </div>
    <button @click="executeRequest(requests.addCard)">Add Card (defunct for now!)</button><!-- TODO -->
</section>
`
});