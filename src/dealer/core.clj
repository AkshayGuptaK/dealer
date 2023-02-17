(ns dealer.core
  (:gen-class)
  (:require [clojure.string :as str]
            [dealer.domain.model :as model]))

(defn process-user-input
  [line]
  (-> line
      (str/split #" " 2)
      ((fn [[command args]]
         (model/process-command command (read-string args))))
      prn))

(defn -main
  [& args]
  (prn "Enter commands:")
  (while true
    (loop []
      (process-user-input (read-line))
      (recur))))
