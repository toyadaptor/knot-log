(ns knotlog.cljs.component.piece-knot
  (:refer-clojure :exclude [parse-long])
  (:require [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defn piece-knot-component [{:keys [is-open state-piece reload]}]
  (letfn [(save []
            (let [id (-> @state-piece :piece :id)]
              (go
                (let [{:keys [status]} (<! (http/put (str "http://localhost:8000/api/private/pieces/" id "/knot")
                                                     {:with-credentials? true
                                                      :json-params       {:knot (-> @state-piece :piece :knot)}}))]
                  (if (= 200 status)
                    (do
                      (reload)
                      (reset! is-open nil)))))))
          (cancel []
            (reload)
            (reset! is-open nil))]
    (fn []
      [:div.modal {:class @is-open}
       [:div.modal-background]
       [:div.modal-content
        [:div.box
         [:div.field
          [:div.control
           [:input.input {:type      "text"
                          :value     (-> @state-piece :piece :knot)
                          :on-change #(swap! state-piece assoc-in [:piece :knot] (-> % .-target .-value))}]]]
         [:div.field.is-grouped
          [:div.control
           [:button.button.is-danger
            {:on-click #(save)} "Submit"]]
          [:div.control
           [:button.button.is-danger.is-light
            {:on-click #(cancel)}
            "Cancel"]]]]]
       [:button.modal-close.is-large {:aria-label "close"
                                      :on-click   #(cancel)}]])))