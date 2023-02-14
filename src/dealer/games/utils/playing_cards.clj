(ns dealer.games.utils.playing-cards)

(def ranks
  (let [numbers (mapv str (range 2 11))
        faces ["Jack" "Queen" "King" "Ace"]]
   (into numbers faces)))

(def suits ["Clubs" "Diamonds" "Hearts" "Spades"])

(def cards
  (->> (utils/cartesian-product ranks suits)
         (map (fn [[rank suit]] {:rank rank :suit suit}))))

(defn shuffled-cards
  []
  (shuffle cards))

(defn is-card-of-suit?
  [suit card]
  (-> card
      (:suit)
      (= suit)))

(defn highest-face-card
  [cards]
  (->> cards
       (sort-by #(.indexOf ranks (:rank %)))
       first))
