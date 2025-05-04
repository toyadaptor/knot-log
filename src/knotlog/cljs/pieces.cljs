(ns knotlog.cljs.pieces
  (:refer-clojure :exclude [parse-long])
  (:require [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]
            [knotlog.cljs.helper :refer [get-backend-url]]
            [knotlog.common.util :refer [iso-str-to base-date-str-to]]
            [knotlog.cljs.component.knot-link :refer [knot-link-component]]
            [knotlog.cljs.component.piece-content :refer [piece-content-component]]
            [knotlog.cljs.component.piece-content-new :refer [piece-content-component-new]]
            [knotlog.cljs.component.piece-knot :refer [piece-knot-component]]
            [knotlog.cljs.component.piece-date :refer [piece-date-component]]
            [knotlog.cljs.component.upload :refer [upload-component]]))

(defn pieces-component [{:keys [id key is-login]}]
  (let [piece-content-modal (r/atom nil)
        piece-content-new-modal (r/atom nil)
        piece-knot-modal (r/atom nil)
        piece-basedate-modal (r/atom nil)
        piece-link-modal (r/atom nil)
        state-piece (r/atom nil)]
    (letfn [(get-piece [piece-id]
              (go
                (let [{:keys [status body]} (<! (http/get (get-backend-url (str "/api/pieces/" piece-id))))]
                  (if (= 200 status)
                    (reset! state-piece body)
                    (js/console.log "error")
                    ))))
            (delete-link [link-id]
              (let [{:keys [status]} (<! (http/delete (get-backend-url (str "/api/knot-links/" link-id))))]
                (if (= 200 status)
                  nil
                  (js/console.log "error"))))
            (reload []
              (get-piece id))]
      (r/create-class
        {:component-did-mount
         (fn [_]
           (get-piece id))

         :reagent-render
         (fn []
           (if-let [p @state-piece]
             [:section.section
              ^{:key key}
              [piece-content-component {:is-open     piece-content-modal
                                        :state-piece state-piece
                                        :reload      reload}]
              [piece-content-component-new {:is-open   piece-content-new-modal
                                            :get-piece get-piece}]
              [piece-knot-component {:is-open     piece-knot-modal
                                     :state-piece state-piece
                                     :reload      reload}]
              [piece-date-component {:is-open     piece-basedate-modal
                                     :state-piece state-piece
                                     :reload      reload}]
              [knot-link-component {:is-open     piece-link-modal
                                    :state-piece state-piece
                                    :reload      reload}]
              [:div
               (if @is-login
                 [:div.tags
                  [:span.tag.is-info
                   [:a.has-text-black {:on-click #(reset! piece-content-modal "is-active")}
                    "content"]]
                  [:span.tag.is-light
                   [:a.has-text-black {:on-click #(reset! piece-basedate-modal "is-active")}
                    "date"]]
                  [:span.tag.is-primary
                   [:a.has-text-black {:on-click #(reset! piece-content-new-modal "is-active")}
                    "piece+"]]])

               [:h1.is-size-3 (or (-> p :piece :knot) "*")]

               [:div (-> p :piece :content)]

               [:br]

               [:div.tags
                (for [link (-> p :link-in)]
                  ^{:key (:knot_id link)}
                  [:span.tag.is-black
                   [:a.has-text-white {:on-click #(rfe/push-state :piece {:id (:knot_id link)})}
                    (:knot link)]
                   (if @is-login
                     [:button.delete
                      {:on-click #(delete-link (:id link))}])])
                (if @is-login
                  [:div.tags
                   [:span.tag.is-warning
                    [:a.has-text-black {:on-click #(reset! piece-knot-modal "is-active")}
                     "knot"]]
                   [:span.tag.is-danger
                    [:a.has-text-black {:on-click #(reset! piece-link-modal "is-active")}
                     "link+"]]])]

               [:div.content.is-normal
                [:dl
                 (for [link (-> p :link-out)]
                   ^{:key (:piece_id link)}
                   [:dt [:a.has-text-info {:on-click #(rfe/push-state :piece {:id (:piece_id link)})}
                         (if (empty? (:knot link))
                           (iso-str-to (:update_time link)
                                       {:style :knot-full})
                           (:knot link))]])]]

               [:p [:small
                    (base-date-str-to (-> p :piece :base_year) (-> p :piece :base_month_day))
                    #_(iso-str-to (-> p :piece :update_time))]]

               [:br]

               (if @is-login
                 [:div.content.is-normal
                  [upload-component {:state-piece state-piece
                                     :reload      reload}]
                  [:ul
                   (for [file (-> p :files)]
                     ^{:key (:id file)}
                     [:li [:small [:a.has-text-grey (:uri_path file)]]])]])

               [:div.buttons
                (if (-> p :prev-date)
                  [:button.button.is-small {:on-click #(rfe/push-state :piece {:id (-> p :prev-date :id)})}
                   "prev"]
                  #_[:button.button.is-small {:on-click #(get-piece (-> p :prev-date :id))}
                     "prev"]
                  [:button.button.is-small.is-static
                   "prev"])

                (if (-> p :next-date)
                  [:button.button.is-small {:on-click #(rfe/push-state :piece {:id (-> p :next-date :id)})}
                   "next"]
                  #_[:button.button.is-small {:on-click #(get-piece (-> p :next-date :id))}
                     "next"]
                  [:button.button.is-small.is-static
                   "next"])]

               #_[:div.buttons
                  (for [today (-> p :todays)]
                    ^{:key (:id today)}
                    [:button.button.is-small {:on-click #(rfe/push-state :piece {:id (:id today)})}
                     (:base_year today)])]
               ]]
             ))}))))
