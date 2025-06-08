(ns knotlog.cljs.pieces
  (:refer-clojure :exclude [parse-long])
  (:require [reagent.core :as r]
            [reitit.frontend.easy :as rfe]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]
            [knotlog.cljs.helper :refer [get-backend-url]]
            [knotlog.common.util :refer [iso-str-to base-date-str-to storage-url]]
            [knotlog.cljs.component.knot-link :refer [knot-link-component]]
            [knotlog.cljs.component.piece-new :refer [piece-new-component]]
            [knotlog.cljs.component.piece-edit :refer [piece-edit-component]]
            [knotlog.cljs.convert :refer [process-content]]))

(defn pieces-component [{:keys [id key is-login]}]
  (let [piece-new-modal (r/atom nil)
        piece-edit-modal (r/atom nil)
        piece-link-modal (r/atom nil)
        state-piece (r/atom nil)
        touch-start (r/atom nil)
        touch-end (r/atom nil)]
    (letfn [(get-piece [piece-id]
              (go
                (let [{:keys [status body]} (<! (http/get (get-backend-url (str "/api/pieces/" piece-id))))]
                  (if (= 200 status)
                    (reset! state-piece (process-content body))
                    (js/console.log "error")
                    ))))
            (delete-link [link-id]
              (let [{:keys [status]} (<! (http/delete (get-backend-url (str "/api/knot-links/" link-id))))]
                (if (= 200 status)
                  nil
                  (js/console.log "error"))))
            (reload []
              (get-piece id))
            (handle-swipe []
              (when (and @touch-start @touch-end)
                (let [swipe-distance (- (.-clientX @touch-start) (.-clientX @touch-end))
                      min-swipe-distance 50] ; Minimum distance to consider it a swipe
                  (when (> (js/Math.abs swipe-distance) min-swipe-distance)
                    (if (> swipe-distance 0)
                      ; Swipe left -> go to next
                      (when (-> @state-piece :next-date)
                        (get-piece (-> @state-piece :next-date :id))
                        (js/history.pushState nil nil (str "/pieces/" (-> @state-piece :next-date :id))))
                      ; Swipe right -> go to prev
                      (when (-> @state-piece :prev-date)
                        (get-piece (-> @state-piece :prev-date :id))
                        (js/history.pushState nil nil (str "/pieces/" (-> @state-piece :prev-date :id)))))
                    ; Reset touch states after handling swipe
                    (reset! touch-start nil)
                    (reset! touch-end nil)))))]
      (let [handle-popstate (fn [_]
                              (let [path-parts (-> js/window.location.pathname (clojure.string/split #"/"))
                                    piece-id (last path-parts)]
                                (when (and (>= (count path-parts) 3)
                                           (= "pieces" (nth path-parts 1)))
                                  (get-piece piece-id))))]
        (r/create-class
          {:component-did-mount
           (fn [_]
             (get-piece id)
             ;; Add popstate event listener for browser back/forward buttons
             (js/window.addEventListener "popstate" handle-popstate))

           :component-will-unmount
           (fn [_]
             ;; Remove popstate event listener when component unmounts
             (js/window.removeEventListener "popstate" handle-popstate))

           :reagent-render
           (fn []
             (if-let [p @state-piece]
               [:section.section
                {:on-touch-start (fn [e]
                                   (reset! touch-start (-> e .-touches (aget 0))))
                 :on-touch-end (fn [e]
                                 (reset! touch-end (-> e .-changedTouches (aget 0)))
                                 (handle-swipe))
                 :on-touch-cancel (fn [_]
                                    (reset! touch-start nil)
                                    (reset! touch-end nil))}
                ^{:key key}
                [piece-new-component {:is-open   piece-new-modal
                                      :get-piece get-piece}]
                [piece-edit-component {:is-open     piece-edit-modal
                                       :state-piece state-piece
                                       :reload      reload}]
                [knot-link-component {:is-open     piece-link-modal
                                      :state-piece state-piece
                                      :reload      reload}]
                [:div


                 [:h1.is-size-3 (or (-> p :piece :knot) "*")]

                 [:div {:dangerouslySetInnerHTML
                        {:__html (process-content (-> p :piece :content))}}]

                 [:br]

                 [:div.tags
                  (doall
                    (for [link (-> p :link-in)]
                      ^{:key (:knot_id link)}
                      [:span.tag.is-black
                       [:a.has-text-white {:on-click #(rfe/push-state :piece {:id (:knot_id link)})}
                        (:knot link)]
                       (if @is-login
                         [:button.delete
                          {:on-click #(delete-link (:id link))}])]))
                  (if @is-login
                    [:div.tags
                     [:span.tag.is-primary
                      [:a.has-text-black {:on-click #(reset! piece-new-modal "is-active")}
                       "piece+"]]
                     [:span.tag.is-success
                      [:a.has-text-black {:on-click #(reset! piece-edit-modal "is-active")}
                       "edit"]]
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
                      (iso-str-to (-> p :piece :update_time) {:style :knot-full})]]

                 [:br]

                 [:div.buttons.is-centered
                  (if (-> p :prev-date)
                    [:button.button.is-small {:on-click (fn []
                                                          (get-piece (-> p :prev-date :id))
                                                          (js/history.pushState nil nil (str "/pieces/" (-> p :prev-date :id))))}
                     "prev"]
                    [:button.button.is-small.is-static
                     "prev"])

                  (if (-> p :next-date)
                    [:button.button.is-small {:on-click (fn []
                                                          (get-piece (-> p :next-date :id))
                                                          (js/history.pushState nil nil (str "/pieces/" (-> p :next-date :id))))}
                     "next"]
                    [:button.button.is-small.is-static
                     "next"])]

                 #_[:div.buttons
                    (for [today (-> p :todays)]
                      ^{:key (:id today)}
                      [:button.button.is-small {:on-click #(rfe/push-state :piece {:id (:id today)})}
                       (:base_year today)])]
                 ]]
               ))})))))
