(ns knotlog.cljs.component.piece-edit
  (:refer-clojure :exclude [parse-long])
  (:require [knotlog.cljs.helper :refer [get-backend-url]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]
            [knotlog.cljs.component.upload :refer [upload-component]]
            [reagent.core :as r]))

(defn piece-edit-component [{:keys [is-open state-piece reload]}]
  (letfn [(save []
            (let [id (-> @state-piece :piece :id)]
              (go
                (let [{:keys [status]} (<! (http/put (get-backend-url (str "/api/private/pieces/" id))
                                                     {:with-credentials? true
                                                      :json-params       {:content        (-> @state-piece :piece :content)
                                                                          :base-year      (-> @state-piece :piece :base-year)
                                                                          :base-month-day (-> @state-piece :piece :base-month-day)
                                                                          :knot           (-> @state-piece :piece :knot)}}))]
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
           [:label.label "content"]
           [:textarea.textarea {:value     (-> @state-piece :piece :content)
                                :on-change (fn [e]
                                             (let [is-composing (.-isComposing (.-nativeEvent e))]
                                               (when-not is-composing
                                                 (swap! state-piece assoc-in [:piece :content] (-> e .-target .-value)))))}]]]

         [:div.field.is-grouped
          [:div.control
           [:label.label "year"]
           [:input.input {:type      "text"
                          :value     (-> @state-piece :piece :base-year)
                          :on-change #(swap! state-piece assoc-in [:piece :base-year] (-> % .-target .-value))}]]
          [:div.control
           [:label.label "month + day"]
           [:input.input {:type      "text"
                          :value     (-> @state-piece :piece :base-month-day)
                          :on-change #(swap! state-piece assoc-in [:piece :base-month-day] (-> % .-target .-value))}]]]
         [:div.field
          [:div.control
           [:label.label "knot"]
           [:input.input {:type      "text"
                          :value     (-> @state-piece :piece :knot)
                          :on-change #(swap! state-piece assoc-in [:piece :knot] (-> % .-target .-value))}]]]
         [:div.field
          [:label.label "upload"]
          [upload-component {:state-piece state-piece
                             :reload      reload}]]

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
