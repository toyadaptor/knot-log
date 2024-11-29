(ns knotlog.clj.config
  (:require [environ.core :refer [env]]))

(def db-config {:dbtype      "postgresql"
                :host        (env :db-host)
                :dbname      (env :db-name)
                :port        (env :db-port)
                :user        (env :db-user)
                :password    (env :db-password)
                :auto-commit true})

