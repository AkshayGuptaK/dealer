(ns dealer.utils.utils)

(defn cartesian-product [& seqs]
  (reduce
    (fn [acc seq]
      (for [x acc y seq]
        (conj x y)))
    [[]]
    seqs))
