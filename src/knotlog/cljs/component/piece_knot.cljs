(ns knotlog.cljs.component.piece-knot
  (:require [reagent.core :as r]
            [ajax.core :refer [PUT]]
            [knotlog.cljs.action :as s]))

(defn piece-knot-component [{:keys [is-open state-piece reload]}]
  (letfn [(save []
            (let [id (-> @state-piece :piece :id)]
              (PUT (str "http://localhost:8000/api/pieces/" id "/knot")
                   {:response-format :json
                    :keywords?       true
                    :params          {:knot (-> @state-piece :piece :knot)}
                    :handler         (fn [_]
                                       (reload)
                                       (reset! is-open nil))
                    :error-handler   (fn [error]
                                       (js/console.error "Error:" error))})))
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