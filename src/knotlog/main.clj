(ns knotlog.main
  (:gen-class)
  (:require
    [knotlog.interface.routes.router :as router]
    [taoensso.timbre :as timbre]
    [taoensso.timbre.appenders.core :as appenders]))

(defn init-logging! []
  (timbre/merge-config!
    {:level :info
     :appenders {:println (appenders/println-appender)}
     :timestamp-opts {:pattern  "yyyy-MM-dd HH:mm:ss"
                      :timezone  :jvm-default}}))

(defn -main
  [& _]
  (init-logging!)
  (println "Starting knotlog with Clean Architecture...")
  (router/start))