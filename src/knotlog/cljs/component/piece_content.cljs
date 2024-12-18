(ns knotlog.cljs.component.piece-content
  (:require [ajax.core :refer [PUT]]
            [knotlog.cljs.action :as s]))


(defn piece-content-component [{:keys [is-open state-piece reload]}]
  (letfn [(save []
            (let [id (-> @state-piece :piece :id)]
              (PUT (str "http://localhost:8000/api/pieces/" id "/content")
                   {:response-format :json
                    :keywords?       true
                    :params          {:content (-> @state-piece :piece :content)}
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
           [:textarea.textarea {:placeholder "Textarea"
                                :value       (-> @state-piece :piece :content)
                                :on-change   #(swap! state-piece assoc-in [:piece :content] (-> % .-target .-value))}]]]

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


