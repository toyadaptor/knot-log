(ns knotlog.clj.main
  (:require
    [knotlog.clj.router :as router]))

(defn -main
  [& args]
  (println "Hello world!")
  (router/start))
