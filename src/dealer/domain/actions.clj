(ns dealer.domain.actions)

(declare updated-game-after-action)

(defn- move-card
  [game card-instance-id from-path to-path]
  (let [card-path (concat [:zones] from-path [:cards card-instance-id])
        card (get-in game card-path)]
    (-> game
        (update-in from-path dissoc card-instance-id)
        (assoc-in (concat [:zones] to-path [:cards card-instance-id]) card))))

(defn- set-value
  [game path value]
  (assoc-in game path value))

(defn- inc-value
  [game path increment]
  (update-in game path (partial + increment)))

(defn- get-step-consequent-actions
  "Get actions triggered by the current action"
  [trigger game player-id]
  (let [steps (get game :steps)
        step-path (get-in game [:players player-id :step])
        current-step (get-in steps step-path)]
    (concat
     (get-in current-step [trigger player-id])
     (get-in current-step [trigger :*]))))

(defn- next-step-path
  [{:keys [steps]} current-step-path]
  (loop [index (dec (count current-step-path))
         step-path current-step-path]
    (let [updated-step-path (update step-path index inc)]
      (if-not (nil? (get-in steps updated-step-path))
        updated-step-path
        (recur (dec index) (assoc updated-step-path index 0))))))

(defn- next-step
  [game player-id]
  (let [on-end-actions (get-step-consequent-actions :on-end game player-id)
        updated-game (reduce updated-game-after-action game on-end-actions)
        updated-step-path (get-in updated-game [:players player-id :step])
        next-step (next-step-path updated-game updated-step-path)]
    (assoc-in updated-game [:players player-id :step] next-step)))

(def action-functions
  {:move-card move-card
   :set-value set-value
   :inc-value inc-value
   :next-step next-step})

(defn parse-action
  [[player-id action-type & args]]
  {:player-id player-id
   :action-type action-type
   :args args})

(defn updated-game-after-action
  "Execute a valid action and return the updated game state"
  [game action]
  (let [{:keys [action-type args]} (parse-action action)]
    (->> args
         (map (fn [arg] (if (vector? arg)
                         (apply (first arg) game (rest arg))
                         arg)))
         (apply (action-type action-functions) game))))
       ;; add logs for actions committed

(defn updated-game-after-consequent-actions
  [game action]
  (let [{:keys [player-id]} (parse-action action)]
    (->> player-id
         (get-step-consequent-actions :on-action game)
         (reduce updated-game-after-action game))))
