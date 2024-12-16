(ns knotlog.cljs.modal
  (:require [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [knotlog.cljc.util :refer [iso-str-to base-date-str-to]]
            [knotlog.cljs.action :as s]))


(defn knot-modal []
  [:div.modal {:class @s/knot-modal}
   [:div.modal-background]
   [:div.modal-content
    [:div.box
     [:div.field
      [:div.control
       [:input.input {:type "text"
                      :value       @s/edit-piece-knot
                      :on-change   #(reset! s/edit-piece-knot (-> % .-target .-value))}] ]]
     [:div.field.is-grouped
      [:div.control
       [:button.button.is-danger
        {:on-click #(s/piece-knot-save)} "Submit"]]
      [:div.control
       [:button.button.is-danger.is-light
        {:on-click #(s/toggle-modal s/knot-modal)}
        "Cancel"]]]]]
   [:button.modal-close.is-large {:aria-label "close"
                                  :on-click   #(s/toggle-modal s/knot-modal)}]])

(defn piece-modal []
  [:div.modal {:class @s/piece-modal}
   [:div.modal-background]
   [:div.modal-content
    [:div.box
     [:div.field
      [:div.control
       [:textarea.textarea {:placeholder "Textarea"
                            :value       @s/edit-piece-content
                            :on-change   #(reset! s/edit-piece-content (-> % .-target .-value))}]]]

     [:div.field.is-grouped
      [:div.control
       [:button.button.is-danger
        {:on-click #(s/piece-content-save)} "Submit"]]
      [:div.control
       [:button.button.is-danger.is-light
        {:on-click #(s/toggle-modal s/piece-modal)}
        "Cancel"]]]]]
   [:button.modal-close.is-large {:aria-label "close"
                                  :on-click   #(s/toggle-modal s/piece-modal)}]])

