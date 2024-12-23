(ns knotlog.cljs.component.piece-content-new
  (:refer-clojure :exclude [parse-long])
  (:require [reagent.core :as r]
            [knotlog.cljs.helper :refer [update-query-path get-backend-url]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defn piece-content-component-new [{:keys [is-open get-piece]}]
  (let [content-new (r/atom "")]
    (letfn [(save []
              (go
                (let [{:keys [status body]} (<! (http/post (get-backend-url "/api/private/pieces")
                                                      {:with-credentials? true
                                                       :json-params       {:content @content-new}}))]
                  (if (= 200 status)
                    (do
                      (reset! content-new "")
                      (update-query-path (str "/pieces/" (:id body)))
                      (get-piece (:id body))
                      (reset! is-open nil))))))
            (cancel []
              (reset! content-new "")
              (reset! is-open nil))]
      (fn []
        [:div.modal {:class @is-open}
         [:div.modal-background]
         [:div.modal-content
          [:div.box
           [:div.field
            [:div.control
             [:label.label "content"]
             [:textarea.textarea {:value       @content-new
                                  :on-change   #(reset! content-new (-> % .-target .-value))}]]]

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


