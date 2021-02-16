const t = {
  "copy_to_clipboard": "Skopiuj",
  "waiting_for_players": "Udostępnij link do tej strony znajomym by zaprosić ich do gry. Do rozpoczęcia potrzeba co najmniej trzech, a najlepiej czterech-pięciu graczy.",
  "press_start_to_begin": "Jeżeli jesteś gotowy i zebrali się wszyscy gracze, wciśnij start. Gra rozpocznie się gdy wszyscy gracze wcisną start.",
  "waiting_for_narrator": "Oczekiwanie aż narrator ustawi hasło",
  "choose_card_and_set_phrase": "Wybierz kartę i hasło które umożliwi pozostałym graczom odgadnięcie twojego obrazka",
  "do_set_phrase": "Wybierz hasło które umożliwi pozostałym graczom odgadnięcie twojego obrazka. Pamiętaj, że nie może być zbyt oczywiste!",
  "do_send_card": "Wybierz obrazek pasujący do hasła",
  "waiting_for_cards": "Oczekiwanie aż wszyscy pozostali gracze wyślą swój obrazek na stół",
  "do_vote": "Spróbuj odgadnąć który z obrazków na stole należy do narratora?",
  "waiting_for_votes": "Oczekiwanie aż wszyscy gracze oddają swój głos",
  "waiting_to_proceed": "Wszyscy gracze muszą wcisnąć 'Dalej' by przejść do następnej rundy",
  "finished": "Koniec gry!",
  "corrupted": "Niestety, dane tej gry są nieprawidłowe. Dalsza rozgrywka jest niemożliwa.",

  "all_voted_for_narrator": "Wszyscy odgadli obrazek narratora. +2 punkty dla wszystkich poza narratorem",
  "no_one_voted_for_narrator": "Nikt nie odgadł obrazka narratora. +po jednym punkcie na każdy głos dla każdego na czyj obrazek zagłosowano",
  "someone_voted_for_narrator": "+3 punkty dla narratora, +1 dla każdego kto odgadł kartę narratora, +po jednym punkcie na głos dla każdego na czyj obrazek zagłosowano",
  "in_progress": "Runda w trakcie...",

  "waiting_for_others": "Oczekiwanie na pozostałych graczy...",
  "start": "Start",
  "proceed": "Dalej",
  "select_card": "Wybierz obraz i ustaw hasło",
  "set_phrase": "Ustaw hasło",
  "phrase": "Hasło:",
  "set": "Ustaw",
  "your_deck": "Twoja talia",
  "connection_lost": "Utracono połączenie z serwerem, próba ponownego łączenia...",

  "click_to_zoom_in": "Kliknij by przybliżyć",
  "card_is_hidden": "Obraz zasłonięty",
  "narrators_card": "Obraz narratora",
  "xyzs_card_fmt": function(xyz) { return `Właściciel: <b>${xyz}</b>`; },
  "voted_on_by": "Zagłosowali:",
  "send": "Wyślij",
  "vote": "Głosuj",
  "select": "Wybierz",

  "points_fmt": function(x) {
    if (x == 1) { return "punkt"; } else {
      let polski_xd = x % 100;
      let polski_xd2 = x % 10;
      if ((polski_xd2 == 2 || polski_xd2 == 3 || polski_xd2 == 4) &&
          (polski_xd < 10 || polski_xd > 20)) {
        return "punkty";
      }
      return "punktów";
    }
  },
  "narrator": "Narrator",
  "still_thinking": "wciąż myśli...",

  "NARRATOR_LOST_ROUND_REPLAY": "Narrator opuścił grę. Runda zostaje powtórzona z nowym narratorem"
};