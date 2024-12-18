(ns knotlog.cljs.action
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST PUT DELETE]]))


(def search-text (r/atom nil))
(def search-type (r/atom "text"))

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





