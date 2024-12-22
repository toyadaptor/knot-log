(ns knotlog.clj.main
  (:gen-class)
  (:require
    [knotlog.clj.router :as router]))

(defn -main
  [& _]
  (println "Hello world!")
  (router/start))
