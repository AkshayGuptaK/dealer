(ns dealer.utils.utils
  (:import java.util.UUID))

(defn cartesian-product [& seqs]
  (reduce
    (fn [acc seq]
      (for [x acc y seq]
        (conj x y)))
    [[]]
    seqs))

(defn uuid
  []
  (.toString (UUID/randomUUID)))
