(ns knotlog.clj.service-test
  (:require [clojure.test :refer :all]
            [knotlog.clj.service :as service]
            [knotlog.clj.mapper :as mapper]
            [knotlog.clj.util :refer :all]))


(def sample-knot "sample")

(defn setup-test-data []
  (let [piece (mapper/insert-piece nil sample-knot)
        link (mapper/insert-link 1 1)]
    {:piece-id (:id piece)
     :link-id  (:id link)}))

(defn cleanup-test-data []
  )


(use-fixtures :each
              (fn [f]
                (setup-test-data)
                (try
                  (f)
                  (finally
                    (cleanup-test-data)))))

