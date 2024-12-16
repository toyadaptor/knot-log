(ns knotlog.cljs.component.link
  (:require [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [knotlog.cljc.util :refer [iso-str-to base-date-str-to]]
            [knotlog.cljs.action :as s]
            [ajax.core :refer [POST]]))



(defn link-component []
  (let [knot-name (r/atom "")]
    (letfn [(save []
              (let [id (-> @s/state-piece :piece :id)]
                (js/console.log "**" id @knot-name)
                (POST (str "http://localhost:8000/api/knot-links")
                      {:response-format :json
                       :keywords?       true
                       :params          {:piece_id id
                                         :knot     @knot-name}
                       :handler         (fn [_]
                                          (s/toggle-modal s/link-modal)
                                          (s/get-piece id))
                       :error-handler   (fn [error]
                                          (js/console.error "Error:" error))})
                ))]
      (fn []
        [:div.modal {:class @s/link-modal}
         [:div.modal-background]
         [:div.modal-content
          [:div.box
           [:div.field
            [:div.control
             [:input.input {:type      "text"
                            :value     @knot-name
                            :on-change #(reset! knot-name (-> % .-target .-value))}]]]
           [:div.field.is-grouped
            [:div.control
             [:button.button.is-danger
              {:on-click #(save)}
              "Submit"]]
            [:div.control
             [:button.button.is-danger.is-light
              {:on-click #(s/toggle-modal s/link-modal)}
              "Cancel"]]]]]
         [:button.modal-close.is-large {:aria-label "close"
                                        :on-click   #(s/toggle-modal s/link-modal)}]]))))



