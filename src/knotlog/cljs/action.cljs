(ns knotlog.cljs.action
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST PUT DELETE]]))


(def state-piece (r/atom nil))
(def state-pieces (r/atom nil))

(def search-text (r/atom nil))
(def search-type (r/atom "text"))
(def edit-piece-content (r/atom ""))
(def edit-piece-knot (r/atom ""))

(def piece-modal (r/atom nil))
(def knot-modal (r/atom nil))


(defn toggle-modal [state]
  (if (empty? @state)
    (reset! state "is-active")
    (reset! state nil)))

(defn toggle-state [state]
  (if (true? state)
    (reset! state false)
    (reset! state true)))






(defn get-piece [id]
  (GET (str "http://localhost:8000/api/pieces/" id)
       {:response-format :json
        :keywords?       true
        :handler         (fn [response]
                           (reset! state-piece response))
        :error-handler   (fn [error]
                           (js/console.error "Error:" error))}))

(defn put-piece-content [id content callback]
  (PUT (str "http://localhost:8000/api/pieces/" id "/content")
       {:response-format :json
        :keywords?       true
        :params          {:content content}
        :handler         (fn [_]
                           (callback))
        :error-handler   (fn [error]
                           (js/console.error "Error:" error))}))

(defn put-piece-knot [id knot callback]
  (POST (str "http://localhost:8000/api/pieces/" id "/knot")
        {:response-format :json
         :keywords?       true
         :params          {:knot knot}
         :handler         (fn [_]
                            (callback))
         :error-handler   (fn [error]
                            (js/console.error "Error:" error))}))


(defn login [password]
  (POST "http://localhost:8000/api/login"
        {:response-format   :json
         :keywords?         true
         :credentials       :include
         :with-credentials? true
         :params            {:password password}
         :handler           (fn [response]
                              (js/console.log response))
         :error-handler     (fn [error]
                              (js/console.error "Error:" error))}))


(defn private-main []
  (GET "http://localhost:8000/api/private/main"
       {:response-format   :json
        :keywords?         true
        :with-credentials? true
        :handler           (fn [response]
                             (js/console.log response)

                             )

        :error-handler     (fn [error]
                             (js/console.error "Error:" error))}))



(defn search-action []
  (cond (= ".nn" @search-text)
        (reset! search-type "password")

        (= ".bi" @search-text)
        (reset! search-type "text")

        (and (= "password" @search-type)
             (not (nil? @search-text)))
        (login @search-text)

        (and (= "text" @search-type)
             (= "pp"))
        (private-main)
        :else nil)
  (reset! search-text ""))



(defn piece-content-edit []
  (reset! edit-piece-content (-> @state-piece :piece :content))
  (toggle-modal piece-modal))

(defn piece-content-save []
  (put-piece-content (-> @state-piece :piece :id)
                     @edit-piece-content
                     (fn []
                       (get-piece (-> @state-piece :piece :id))
                       (toggle-modal piece-modal))))


(defn piece-knot-edit []
  (reset! edit-piece-knot (-> @state-piece :piece :knot))
  (toggle-modal knot-modal))

(defn piece-knot-save []
  (let [id (-> @state-piece :piece :id)]
    (put-piece-knot id
                    @edit-piece-knot
                    (fn []
                      (get-piece id)
                      (toggle-modal knot-modal))))
  )