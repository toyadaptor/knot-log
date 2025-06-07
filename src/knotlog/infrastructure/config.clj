(ns knotlog.infrastructure.config
  (:require [environ.core :refer [env]]
            [knotlog.infrastructure.repositories.piece-repository :as piece-repo]
            [knotlog.infrastructure.repositories.link-repository :as link-repo]
            [knotlog.infrastructure.repositories.file-repository :as file-repo]
            [knotlog.infrastructure.firebase-storage :as firebase]))

;; Database configuration
(def db-config {:dbtype      "postgresql"
                :host        (env :db-host)
                :dbname      (env :db-name)
                :port        (env :db-port)
                :user        (env :db-user)
                :password    (env :db-password)
                :auto-commit true})

;; Firebase configuration
(def firebase-config {:account-key (env :firebase-account-key)
                      :bucket-name (env :firebase-bucket-name)})

;; Repository instances
(def piece-repository (piece-repo/create-piece-repository db-config))
(def link-repository (link-repo/create-link-repository db-config))
(def file-repository (file-repo/create-file-repository db-config))

;; Storage instances
(defn init-firebase-storage []
  (firebase/create-firebase-storage firebase-config))

;; Authentication configuration
(def auth-secret (env :auth-secret))
(def auth-data {:admin (env :auth-password)})
(def auth-domain (env :auth-domain))