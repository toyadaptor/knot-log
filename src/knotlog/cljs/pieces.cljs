(ns knotlog.cljs.pieces
  (:require [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [knotlog.cljc.util :refer [iso-str-to base-date-str-to]]
            [knotlog.cljs.action :as s]
            [knotlog.cljs.modal :as modal]
            [knotlog.cljs.component.link :refer [link-component]]))


(defn pieces-component [{:keys [id]}]
  (r/create-class
    {:component-did-mount
     (fn [_]
       (s/get-piece id))

     :reagent-render
     (fn []
       [:section.section
        ^{:key id}
        [modal/piece-modal]
        [modal/knot-modal]
        [link-component]
        (if-let [p @s/state-piece]
          [:div
           [:h1.is-size-3 (or (-> p :piece :knot) "*")]
           [:div (-> p :piece :content)]
           [:br]

           [:div.tags
            (for [link (-> p :link-in)]
              ^{:key (:knot_id link)}
              [:span.tag.is-black
               [:a.has-text-white {:on-click #(rfe/push-state :piece {:id (:knot_id link)})}
                (:knot link)]
               [:button.delete]])
            [:span.tag
             [:a.has-text-black {:on-click #(s/toggle-modal s/link-modal)}
              "+"]]]

           [:div.content.is-normal
            [:dl
             (for [link (-> p :link-out)]
               ^{:key (:piece_id link)}
               [:dt [:a.has-text-info {:on-click #(rfe/push-state :piece {:id (:piece_id link)})}
                     (iso-str-to (:update_time link)
                                 {:style :knot-full})]])]]

           [:p [:small
                (base-date-str-to (-> p :piece :base_year) (-> p :piece :base_month_day))
                #_(iso-str-to (-> p :piece :update_time))]]

           [:hr]

           [:div.buttons
            (if (-> p :prev-date)
              [:button.button.is-small {:on-click #(rfe/push-state :piece {:id (-> p :prev-date :id)})}
               "prev"]
              [:button.button.is-small.is-static
               "prev"])

            (if (-> p :next-date)
              [:button.button.is-small {:on-click #(rfe/push-state :piece {:id (-> p :next-date :id)})}
               "next"]
              [:button.button.is-small.is-static
               "next"])]

           #_[:div.buttons
              (for [today (-> p :todays)]
                ^{:key (:id today)}
                [:button.button.is-small {:on-click #(rfe/push-state :piece {:id (:id today)})}
                 (:base_year today)])]

           [:div.buttons
            [:button.button.is-small {:on-click #(s/piece-content-edit)}
             "content"]
            [:button.button.is-small {:on-click #(s/piece-knot-edit)}
             "knot"]]]


          [:h1.is-size-3 "no piece"])])})
  )