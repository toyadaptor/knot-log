(ns knotlog.cljs.component.piece-new
  (:refer-clojure :exclude [parse-long])
  (:require [knotlog.cljs.helper :refer [get-backend-url update-query-path api-request]]
            [cljs.core.async :refer [go <!]]
            [reagent.core :as r]))


(defn piece-new-component [{:keys [is-open get-piece]}]
  (let [content (r/atom nil)
        base-year (r/atom nil)
        base-month-day (r/atom nil)
        knot (r/atom nil)]
    (letfn [(save []
              (go
                (let [{:keys [status body]} (<! (api-request :post 
                                                            "/api/private/pieces"
                                                            {:with-credentials? true
                                                             :json-params       {:content        @content
                                                                                 :base-year      @base-year
                                                                                 :base-month-day @base-month-day
                                                                                 :knot           @knot}}))]
                  (if (= 200 status)
                    (do
                      (reset! content "")
                      (update-query-path (str "/pieces/" (:id body)))
                      (when get-piece (get-piece (:id body)))
                      (reset! is-open nil))))))
            (cancel []
              (reset! content "")
              (reset! is-open nil))]
      (fn []
        [:div.modal {:class @is-open}
         [:div.modal-background]
         [:div.modal-content
          [:div.box
           ;; Content section
           [:div.field
            [:div.control
             [:label.label "content"]
             [:textarea.textarea {:value     @content
                                  :on-change (fn [e]
                                               (let [is-composing (.-isComposing (.-nativeEvent e))]
                                                 (when-not is-composing
                                                   (reset! content (-> e .-target .-value)))))}]]
            [:div.field.is-grouped
             [:div.control
              [:label.label "year"]
              [:input.input {:type      "text"
                             :value     @base-year
                             :on-change (fn [e]
                                          (reset! base-year (-> e .-target .-value)))}]]
             [:div.control
              [:label.label "month + day"]
              [:input.input {:type      "text"
                             :value     @base-month-day
                             :on-change (fn [e]
                                          (reset! base-month-day (-> e .-target .-value)))}]]]

            [:div.field
             [:div.control
              [:label.label "knot"]
              [:input.input {:type      "text"
                             :value     @knot
                             :on-change (fn [e]
                                          (reset! knot (-> e .-target .-value)))}]]]]

           [:div.field.is-grouped
            [:div.control
             [:button.button.is-danger
              {:on-click #(save)} "Submit"]]
            [:div.control
             [:button.button.is-danger.is-light
              {:on-click #(cancel)}
              "Cancel"]]]]]
         [:button.modal-close.is-large {:aria-label "close"
                                        :on-click   #(cancel)}]]))))
