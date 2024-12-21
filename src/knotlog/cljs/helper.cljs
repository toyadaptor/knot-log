(ns knotlog.cljs.helper
  (:require [knotlog.cljs.config :refer [backend-url]]))

(defn is-cookie-auth []
  (let [cookies (js/document.cookie.split "; ")]
    (some (fn [cookie]
            (let [[k v] (clojure.string/split cookie #"=")]
              (when (= k "login")
                (= "1" v))))
          cookies)))

(defn update-query-path [new-query-path]
  (.pushState js/history nil "" new-query-path))


(defn get-backend-url [path]
  (let [url (str backend-url "/" path)]
    (clojure.string/replace url #"(?<!:)/{2,}" "/")))
