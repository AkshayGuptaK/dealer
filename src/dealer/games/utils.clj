(ns dealer.games.utils
  (:require
   [dealer.utils.utils :as utils]))

;; more core functions than utils, consider moving elsewhere

(defn player-ids
  [game]
  (get-in game [:players :general :players]))

(defn deal-cards-to-players
  ([player-ids cards game]
   (deal-cards-to-players player-ids cards game {:cards-to-deal 1 :zone-deal-to :hand}))
  ([player-ids cards game {:keys [cards-to-deal zone-deal-to]}]
   (let [hands (map (fn [hand] (into {} (map (fn [{:keys [instance-id] :as card}] [instance-id card])) hand)) (partition cards-to-deal cards))
         deal (map vector player-ids hands)]
     (reduce
      (fn [acc [player-id hand]] (assoc-in acc [:zones player-id zone-deal-to :cards] hand))
      game
      deal))))


(defn player-maps
  ""
  [player-ids map-template]
  (->> player-ids
       (map (fn [id] {id map-template}))))

(defn game-maps
  ""
  [player-ids general-map player-map]
  (reduce merge
          {:general general-map}
          (player-maps player-ids player-map)))

(defn turn-order
  [player-ids starting-player]
  (->> player-ids
       (cycle)
       (drop-while #(not= % starting-player))
       (take (count player-ids))))
