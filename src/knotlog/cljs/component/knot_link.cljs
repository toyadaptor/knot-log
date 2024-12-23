(ns knotlog.cljs.component.knot-link
  (:refer-clojure :exclude [parse-long])
  (:require [knotlog.cljs.helper :refer [get-backend-url]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]
            [reagent.core :as r]))

(defn knot-link-component [{:keys [is-open state-piece reload]}]
  (let [knot-name (r/atom "")]
    (letfn [(save []
              (let [id (-> @state-piece :piece :id)]
                (go
                  (let [{:keys [status]} (<! (http/post (get-backend-url (str "/api/private/knot-links"))
                                                        {:with-credentials? true
                                                         :json-params       {:piece_id id
                                                                             :knot     @knot-name}}))]
                    (if (= 200 status)
                      (do
                        (reload)
                        (reset! is-open nil)))))))
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
             [:label.label "knot link"]
             [:input.input {:type        "text"
                            :placeholder "knot"
                            :value       @knot-name
                            :on-change   #(reset! knot-name (-> % .-target .-value))}]]]
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


