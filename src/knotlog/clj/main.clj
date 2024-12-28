(ns knotlog.clj.main
  (:gen-class)
  (:require
    [knotlog.clj.router :as router]
    [knotlog.clj.firebase :as fb]))

(defn -main
  [& _]
  (println "Hello world!")
  (router/start)
  (fb/initialize-firebase))
