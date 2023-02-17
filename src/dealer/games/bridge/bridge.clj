(ns dealer.games.bridge.bridge
  (:require [dealer.games.utils.playing-cards :as playing-cards]
            [dealer.games.utils :as game-utils]))

(defn- players
  [player-ids]
  (game-utils/game-maps
   player-ids
   {:players player-ids
    :step [1 0 0]
    :trump nil
    :bid nil}
   {:step [1 0 0]
    :permitted nil
    :tricks 0}))

(defn winning-card
  [trump suit cards]
  (let [trump-cards (filter (partial playing-cards/is-card-of-suit? trump) cards)
        suit-cards (filter (partial playing-cards/is-card-of-suit? suit) cards)]
    (assert (> (count suit-cards) 0))
    (if (seq trump-cards)
      (playing-cards/highest-face-card trump-cards)
      (playing-cards/highest-face-card suit-cards))))

(defn round-winner
  [game]
  (let [player-ids (game-utils/player-ids game)
        trump (get-in game [:players :general :trump])
        suit (get-in game [:zones :general :play :suit])
        cards (mapv #(second (first (get-in game [:zones % :play :cards]))) player-ids)
        cards-players (map hash-map cards player-ids)]
    (get cards-players (winning-card trump suit cards))))

(defn- game-with-next-play-round
  [starting-player game]
  (let [player-ids (game-utils/player-ids game)
        round (get-in game [:players :general :step 1])]
    (->>
       (->
          (->> (game-utils/turn-order player-ids starting-player)
               (map-indexed (fn [index player-id]
                              {:id index
                               :permitted [[player-id :move-card :* [player-id :hand] [player-id :play]]]
                               :on-start nil
                               :on-action [:* (into [[:general :next-step :general]]
                                                    (mapv (fn [player-id] [:general :next-step player-id]) player-ids))]
                               :on-end nil})))
          (vec)
          (assoc-in [(dec (count player-ids)) :on-end]
                    [:general (into [[:general :inc-value [:players [round-winner] :tricks] 1]
                                     [:general :set-value [:zones :play :suit] nil]
                                     [:general :set-value [:steps 1 (inc round)] [game-with-next-play-round [round-winner]]]]
                                    (mapv (fn [player-id] [:general :move-card [player-id :play] [:general :done round]])
                                          player-ids))]))
          (assoc-in game [:steps 1 round]))))

(defn- steps
  [player-ids]
  [[] []])

(defn- zones
  [player-ids]
  (game-utils/game-maps
   player-ids
   {:play {:suit nil}
    :done []}
   {:hand {:cards {}}
    :play {:cards []}}))
;; needs trigger to set suit

(defn new-bridge-game
  [{player-ids :players}]
  (assert (= (count player-ids) 4))
  (-> player-ids
      (game-utils/deal-cards-to-players
       (playing-cards/shuffled-cards)
       {:players (players player-ids)
        :steps (steps player-ids)
        :zones (zones player-ids)}
       {:cards-to-deal 13
        :zone-deal-to :hand})
      ((partial game-with-next-play-round (first player-ids)))))

;; bidding
;; play:
;; opening lead (declare suit for first trick)
;; playing cards (play the suit, unless you have none of it, then you may play anything)
;; on card entry into play zone, if suit-constraint is not specified, it is set as suit of new card
