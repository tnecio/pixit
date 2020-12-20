Vue.component('card', {
    props: ['image'],
    template: `
<figure>
    <img v-bind:src="image.url" v-bind:alt="image.alt"/>
    <figcaption>{{image.attribution}}, Unsplash</figcaption>
</figure>`
});

Vue.component('word', {
    props: ['value'],
    template: `
    <h2>{{value}}</h2>`
});

Vue.component('gamestate-info', {
    props: ['state'],
    template: `
    <h3>{{state}}</h3>`
});