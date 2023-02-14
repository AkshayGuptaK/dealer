(ns dealer.domain.model)

(defn if-valid-proceed
  ""
  [check-fn next-fn game action]
  (let [validity (check-fn game action)]
    (if (:valid validity)
      (next-fn game action)
      validity)))

(defn player-constraints-fulfilled?
  "Check if action is in set of permitted actions for the player at this time"
  [game action]
  ;; check player overriding constraints else check step constraints
  {:valid true :reason nil})

(defn action-constraints-fulfilled?
  "Check if constraints for the action itself are fulfilled"
  [game action]
  {:valid true :reason nil})

(def is-action-valid?
  "Check constraints to determine if action is valid"
  (partial if-valid-proceed player-constraints-fulfilled? action-constraints-fulfilled?))

(defn execute-action
  "Execute a valid action and return the updated game state"
  [game action]
  game)

(def process-action
  "Execute an action if it is valid else return the reason it is not"
  (partial if-valid-proceed is-action-valid? execute-action))

(defn- logged-game
  [game]
  (when (some? game)
    (merge game {:log []})))

(defn new-game
  "Create a new game"
  [game-type opts]
  (logged-game (case game-type
                 nil)))


;; change-zone card-instance-id prev-zone next-zone
;; card exists in the prev-zone
;; get cord and zone constraints by the move zones

