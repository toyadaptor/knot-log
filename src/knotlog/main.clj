(ns knotlog.main
  (:gen-class)
  (:require
    [knotlog.interface.routes.router :as router]))

(defn -main
  [& _]
  (println "Starting knotlog with Clean Architecture...")
  (router/start))