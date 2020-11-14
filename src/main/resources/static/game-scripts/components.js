Vue.component('card', {
    props: ['image'],
    template: `
<figure>
    <img v-bind:src="image.url" v-bind:alt="image.alt"/>
    <figcaption>{{image.attribution}}, Unsplash</figcaption>
</figure>`
})