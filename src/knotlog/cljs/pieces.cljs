(ns knotlog.cljs.pieces
  (:require [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [ajax.core :refer [GET DELETE]]
            [knotlog.cljc.util :refer [iso-str-to base-date-str-to]]
            [knotlog.cljs.action :as s]
            [knotlog.cljs.component.link :refer [link-component]]
            [knotlog.cljs.component.piece-content :refer [piece-content-component]]
            [knotlog.cljs.component.piece-knot :refer [piece-knot-component]]))


(defn pieces-component [{:keys [id]}]
  (let [piece-content-modal (r/atom nil)
        piece-knot-modal (r/atom nil)
        piece-link-modal (r/atom nil)
        state-piece (r/atom nil)]
    (letfn [(get-piece [piece-id]
              (GET (str "http://localhost:8000/api/pieces/" piece-id)
                   {:response-format :json
                    :keywords?       true
                    :handler         (fn [response]
                                       (reset! state-piece response))
                    :error-handler   (fn [error]
                                       (js/console.error "Error:" error))}))
            (delete-link [link-id]
              (DELETE (str "http://localhost:8000/api/knot-links/" link-id)
                      {:response-format :json
                       :keywords?       true
                       :handler         (fn [_]
                                          (reload))
                       :error-handler   (fn [error]
                                          (js/console.error "Error:" error))}))
            (reload []
              (get-piece id))]
      (r/create-class
        {:component-did-mount
         (fn [_]
           (get-piece id))

         :reagent-render
         (fn []
           [:section.section
            ^{:key id}
            [piece-content-component {:is-open     piece-content-modal
                                      :state-piece state-piece
                                      :reload      reload}]
            [piece-knot-component {:is-open     piece-knot-modal
                                   :state-piece state-piece
                                   :reload      reload}]
            [link-component {:is-open     piece-link-modal
                             :state-piece state-piece
                             :reload      reload}]
            (if-let [p @state-piece]
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
                   [:button.delete
                    {:on-click #(delete-link (:id link))}]])
                [:span.tag
                 [:a.has-text-black {:on-click #(reset! piece-link-modal "is-active")}
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
                [:button.button.is-small {:on-click #(reset! piece-content-modal "is-active")}
                 "content"]
                [:button.button.is-small {:on-click #(reset! piece-knot-modal "is-active")}
                 "knot"]]]


              [:h1.is-size-3 ""])])}))))