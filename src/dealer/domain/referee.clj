(ns dealer.domain.referee
  (:require [dealer.domain.actions :as actions]))

(defn- does-action-match-permitted?
  [action permission]
  (->> action
       (map vector permission)
       (every? (fn [[permission-item action-item]]
                 (or (= :* permission-item)
                     (= permission-item action-item))))))

(defn- does-action-match-any-permitted?
  "Is the action any of the permitted actions?"
  [permitted action]
  (some true? (map (partial does-action-match-permitted? action) permitted)))

(defn- is-action-permitted?
  "Check if action is in set of permitted actions for the player/step at this time"
  [game {:keys [player-id] :as action}]
  (if-let [player-permitted (get-in game [:players player-id :permitted])]
    (does-action-match-any-permitted? player-permitted action)
    (let [path-to-current-step (get-in game [:players player-id :step])
          path-to-step-permitted (concat [:steps]
                                         path-to-current-step
                                         [:permitted])]
      (-> game
          (get-in path-to-step-permitted)
          (does-action-match-any-permitted? action)))))

(defn- action-permitted-validity
  "Return the validity based on whether the action is permitted"
  [game action]
  (if (is-action-permitted? game action)
    {:valid true :reason nil}
    {:valid false :reason :forbidden}))

(defn- is-constraint-fulfilled?
  "Check if a particular constraint is fulfilled"
  [game constraint]
  true)

(defn- action-constraints-validity
  "Check if constraints for the action itself are fulfilled"
  [game action]
  {:valid true :reason nil})
;; e.g. also if card exists in the prev-zone on move card
;; get cord and zone constraints by the move zones

(defn- if-valid-proceed
  "If validated by check-fn execute next-fn"
  [check-fn next-fn game action]
  (let [validity (check-fn game action)]
    (if (:valid validity)
      (next-fn game action)
      validity)))

(defn- action-validity
  "Check constraints to determine if action is valid"
  [game action]
  (->> action
       actions/parse-action
       (partial if-valid-proceed
                action-permitted-validity
                action-constraints-validity
                game)))


(defn- updated-game-after-all-actions
  [game action]
  (->  game
       (actions/updated-game-after-action action)
       (actions/updated-game-after-consequent-actions action)))

(def process-action
  "Execute an action if it is valid else return the reason it is not"
  (partial if-valid-proceed
           action-validity
           updated-game-after-all-actions))
