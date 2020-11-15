/*
 * Definition of different components. Should more or less correspond to data classes in Game.kt
 */

Vue.component('card', {
    props: ['image'],
    template: `
<figure>
    <img v-bind:src="image.url" v-bind:alt="image.alt"/>
    <figcaption>{{image.attribution}}, Unsplash</figcaption>
</figure>`
})

Vue.component('word', {
    props: ['value'],
    template: `
    <h2>{{value}}</h2>`
})