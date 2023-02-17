(ns dealer.domain.model
  (:require
   [dealer.domain.referee :as referee]
   [dealer.games.bridge.bridge :as bridge]))

(def running-game (atom {}))

(defn- set-game
  [game]
  (swap! running-game (fn [atom game] game) game)
  game)

(defn- logged-game
  [game]
  (when (some? game)
    (merge game {:log []})))

(defn new-game
  "Create a new game"
  [game-type opts]
  (-> game-type
      (case
          :bridge (bridge/new-bridge-game opts)
          nil)
      logged-game
      set-game))

(defn update-game
  "Update game if action was valid"
  [updated-game-or-failed-validity]
  (if (= (:valid updated-game-or-failed-validity) false)
    updated-game-or-failed-validity
    (set-game updated-game-or-failed-validity)))

(defn process-command
  [command args]
  (case command
    "new-game" (new-game (first args) {:players (second args)})
    "do-action" (update-game (referee/process-action @running-game args))))
