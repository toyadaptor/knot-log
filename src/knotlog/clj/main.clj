(ns knotlog.clj.main
  (:require
    [knotlog.clj.router :as router]))

(defn -main
  [& _]
  (println "Hello world!")
  (router/start))
