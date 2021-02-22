const t = {
  "copy_to_clipboard": "Copy link",
  "waiting_for_players": "Share the link to this page with your friends to invite them to the game. You need at least three players, but four or more is recommended.",
  "press_start_to_begin": "Once you're ready and the team is complete, press Start. When all players do so the game will begin",
  "waiting_for_narrator": "Waiting for the narrator to set the phrase",
  "choose_card_and_set_phrase": "Choose a picture and then write a phrase to help them distinguish your picture from that of the other players.",
  "do_send_card_fmt": function (phrase) {
    return `Select a picture that makes you think of: <br>${phrase}`
  },
  "waiting_for_cards_fmt": function(phrase) {
    return `Waiting for all players to choose a picture for phrase: <br>${phrase}`;
  },
  "do_vote_fmt": function(phrase) {
    return `Try to guess which of these pictures made the narrator think of: <br>${phrase}`;
  },
  "waiting_for_votes_fmt": function (phrase) {
    return `Waiting for all players to cast their vote. Phrase: <br>${phrase}`;
  },
  "waiting_to_proceed_fmt": function (phrase) {
    return `All players must press 'Proceed' to begin next round! Phrase: <br>${phrase}`;
  },
  "finished": "Game is over!",
  "corrupted": "Sorry but this game instance is corrupted. No further play is possible",

  "all_voted_for_narrator": "All players voted for the narrator's card. +2 points for everyone except narrator",
  "no_one_voted_for_narrator": "No one voted for the narrator's card. +1 point per vote for everyone whose card got voted on",
  "someone_voted_for_narrator": "+3 points for the narrator, +1 point each for those who guessed the narrator's card correctly, +1 point per votefor everyone whose card got voted on",
  "in_progress": "Round is in progress...",

  "waiting_for_others": "Waiting for other players...",
  "start": "Start",
  "proceed": "Proceed",
  "select_card": "Select a card and set phrase",
  "set_phrase": "Set phrase",
  "phrase": "Phrase:",
  "set": "Set",
  "your_deck": "Your deck",
  "connection_lost" : "Connection to server lost. Trying to reconnect...",

  "click_to_zoom_in": "Click to zoom in",
  "card_is_hidden": "Card hidden",
  "narrators_card": "Narrator's card",
  "xyzs_card_fmt": function(xyz) { return `<b>${xyz}</b>'s card`; },
  "voted_on_by": "Voted on by:",
  "send": "Send",
  "vote": "Vote",
  "select": "Select",

  "points_fmt": function(x) {
    if (x == 1) { return "point"; } else { return "points"; }
  },
  "narrator": "Narrator",
  "still_thinking": "still thinking...",

  "NARRATOR_LOST_ROUND_REPLAY": "Narrator left the game. Replaying the round with a new narrator"
};