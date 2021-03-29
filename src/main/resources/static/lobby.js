const LANGUAGES = {
    "en": "🇬🇧 English",
    "pl": "🇵🇱 polski"
};

var lobby = new Vue({
    el: "#lobby",

    created() {
        this.t = t;
        this.lang = lang;
        this.console = console;
        this.LANGUAGES = LANGUAGES;
    },

    data: {
        lang: lang,
        playerName: null,
        games : [],
        popups: [],
        errors: []
    },

    computed: { },

    template: `
<main class="mainForm">

<div id="errors" v-if="errors.length > 0">
    <aside v-for="error in errors">
        {{error.message}}
    </aside>
</div>

<div class="popups" v-if="popups.length > 0">
    <aside v-for="popup in popups">
        {{popup.message}}
    </aside>
</div>

<div id="player-settings">
    <input type="text" v-bind:placeholder="t.yourName" v-model="playerName" required id="playerName">
    <select id="langSelect" name="lang" v-model="lang" onchange="reloadLanguage(this)">
        <option value="en">{{LANGUAGES['en']}}</option>
        <option value="pl">{{LANGUAGES['pl']}}</option>
    </select>
</div>

<div id="play-with-friends">
    <h2 v-html="t.playWithFriends"></h2>
    <form action="/game" method="post">
        <input type="hidden" name="gameName" value="Private game">
        <input type="hidden" name="playerName" v-bind:value="playerName">
        <input type="hidden" name="preferredLanguage" v-bind:value="lang">
        <input type="hidden" name="accessType" value="PRIVATE">
        <button type="submit" v-html="t.createPrivateGame" v-bind:disabled="!playerName"></button>
    </form>
</div>

<div id="play-with-strangers">
    <h2 v-html="t.playWithStrangers"></h2>
    <nav>
        <h3>{{t.openPublicGames}}</h3>
        <ol>
            <li class="publicGameBox" v-if="games.length == 0">
                <h4>{{t.no_games}}</h4>
            </li>
            <li class="publicGameBox" v-for="game in games">
                <h4><a v-bind:href="getGameLink(game.id)">{{game.name}}</a></h4>
                <p>
                    (<span v-html="t.playersCount(game.playersCount)"></span>)
                    {{t.preferred_language}}: {{LANGUAGES[game.preferredLang]}}
                </p>
            </li>
            <li class="createGameBox">
                <h4>{{t.createPublicGame}}</h4>
                <form action="/game" method="post" id="createGameForm">
                    <input type="text" name="gameName" id="gameName" placeholder="Game Name" required />
                    <input type="hidden" name="playerName" v-bind:value="playerName">
                    <input type="hidden" name="preferredLanguage" v-bind:value="lang">
                    <input type="hidden" name="accessType" value="PUBLIC">
                    <button type="submit" v-bind:disabled="!playerName">{{t.create}}</button>
                </form>
            </li>
        </ol>
    </nav>
</div>
</main>
    `,

    methods: {
        getGameLink: function(gameId) {
            if (!this.playerName) {
                return "javascript:lobby.askForPlayerNameHarshly();";
            }
            return '/game/' + gameId + '?playerName=' + this.playerName;
        },
        askForPlayerNameHarshly: function() {
            alert('Please provide your player name first!');
        },

        updateGames: function (gameListResponse) {
            this.games = gameListResponse.games;
        },

        showErrorMessage: function (errorMessage) {
            const newId =  Math.floor(Math.random() * 1000000);
            this.errors.push({
                message: msg,
                id: newId
            });
            setTimeout(() => {
                this.errors = this.errors.filter((x) => x.id !== newId);
            }, 7500);
        }
    }
});

updateGames();
setInterval(updateGames, 3000);

function updateGames() {
    let req = new XMLHttpRequest();
    req.open('GET', '/api/lobby/open-public-games', true);
    req.onload = function(e) {
        if (req.readyState === 4) {
            if (req.status === 200) {
                lobby.updateGames(JSON.parse(req.responseText));
            } else {
                lobby.showErrorMessage(req.statusText);
            }
        }
    };
    req.onerror = function (e) {
        lobby.showErrorMessage(req.statusText);
    };
    req.send(null);
}

function reloadLanguage(event) {
    // easiest way to deal with language change is to just reload the page not using Vue
    window.location.href = "/?lang=" + event.value;
}