(ns knotlog.cljs.component.link
  (:require [reagent.core :as r]
            [knotlog.cljs.action :as s]
            [ajax.core :refer [POST]]))



(defn link-component [{:keys [is-open state-piece reload]}]
  (let [knot-name (r/atom "")]
    (letfn [(save []
              (let [id (-> @state-piece :piece :id)]
                (POST (str "http://localhost:8000/api/knot-links")
                      {:response-format :json
                       :keywords?       true
                       :params          {:piece_id id
                                         :knot     @knot-name}
                       :handler         (fn [_]
                                          (reload)
                                          (reset! is-open nil))
                       :error-handler   (fn [error]
                                          (js/console.error "Error:" error))})
                ))
            (cancel []
              (reset! knot-name "")
              (reset! is-open nil))]
      (fn []
        [:div.modal {:class @is-open}
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
              {:on-click #(cancel)}
              "Cancel"]]]]]
         [:button.modal-close.is-large {:aria-label "close"
                                        :on-click   #(cancel)}]]))))


