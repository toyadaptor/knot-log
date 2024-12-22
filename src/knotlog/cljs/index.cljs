(ns knotlog.cljs.index
  (:refer-clojure :exclude [parse-long])
  (:require [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]
            [knotlog.cljs.helper :refer [get-backend-url]]))



(defn index-component []
  (letfn [(get-piece-latest []
            (go
              (let [{:keys [status body]} (<! (http/get (get-backend-url (str "/api/piece-latest"))))]
                (if (= 200 status)
                  (rfe/push-state :piece {:id (:id body)})
                  (js/console.log "error")))))]
    (r/create-class
      {:component-did-mount
       (fn [_]
         (get-piece-latest))

       :reagent-render
       (fn []
         [:section.section])})))