(ns knotlog.cljs.component.piece-date
  (:refer-clojure :exclude [parse-long])
  (:require [knotlog.cljs.helper :refer [get-backend-url]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defn piece-date-component [{:keys [is-open state-piece reload]}]
  (letfn [(save []
            (let [id (-> @state-piece :piece :id)]
              (go
                (let [{:keys [status]} (<! (http/put (get-backend-url (str "/api/private/pieces/" id "/date"))
                                                     {:with-credentials? true
                                                      :json-params       {:base_year      (-> @state-piece :piece :base_year)
                                                                          :base_month_day (-> @state-piece :piece :base_month_day)}}))]
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
         [:div.field.is-grouped
          [:div.control
           [:label.label "year"]
           [:input.input {:type      "text"
                          :value     (-> @state-piece :piece :base_year)
                          :on-change #(swap! state-piece assoc-in [:piece :base_year] (-> % .-target .-value))}]]
          [:div.control
           [:label.label "month + day"]
           [:input.input {:type      "text"
                          :value     (-> @state-piece :piece :base_month_day)
                          :on-change #(swap! state-piece assoc-in [:piece :base_month_day] (-> % .-target .-value))}]]]
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