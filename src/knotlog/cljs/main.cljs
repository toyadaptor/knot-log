(ns knotlog.cljs.main
  (:refer-clojure :exclude [parse-long])
  (:require [reagent.core :as r]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]
            [knotlog.cljs.helper :refer [is-cookie-auth]]
            [knotlog.cljs.pieces :refer [pieces-component]]
            [knotlog.cljs.index :refer [index-component]]
            [knotlog.cljs.helper :refer [get-backend-url]]
            [knotlog.cljs.component.sound :refer [sound-component]]
            ["react-dom/client" :refer [createRoot]]))

(defonce root (r/atom nil))
(defonce app-state (r/atom nil))
(def is-login (r/atom false))
(def search-type (r/atom "text"))
(def search-text (r/atom ""))
(def knot-suggestions (r/atom []))
(def show-suggestions (r/atom false))


(defn not-found []
  [:section.section
   [:h1.is-size-3 "4o4 not found"]])

(defn view-page [{:keys [key view]}]
  (fn []
    [view {:id       key
           :is-login is-login}]))

(def routes
  [["/" {:name :index
         :view index-component}]
   ["/pieces/:id" {:name       :piece
                   :parameters {:path {:id string?}}
                   :view       pieces-component}]
   ["/4o4" {:name ::not-found
            :view not-found}]])

(defn template-page []
  (if @app-state
    (let [match @app-state
          view (get-in match [:data :view])
          id (get-in match [:parameters :path :id])

          ]
      (letfn [(login [password]
                (go (let [{:keys [status]} (<! (http/post (get-backend-url "/api/login")
                                                          {:json-params {:password password}}))]
                      (if (= 200 status)
                        (do (reset! is-login true)
                            (reset! search-type "text")
                            (reset! search-text "")
                            (reset! show-suggestions false))
                        (js/console.log "error")))))
              (logout []
                (go (let [{:keys [status]} (<! (http/post (get-backend-url "/api/private/logout")))]
                      (if (= 200 status)
                        (do (reset! is-login false)
                            (reset! search-type "text")
                            (reset! search-text "")
                            (reset! show-suggestions false))
                        (js/console.log "error")))))
              (fetch-knot-suggestions [prefix]
                (cond
                  (and @is-login
                           (not= "." (first prefix))
                           (not= "password" @search-type)
                           (not (empty? prefix)))
                  (go (let [response (<! (http/get (get-backend-url "/api/private/knots/search")
                                                   {:query-params {:prefix prefix}
                                                    :with-credentials true}))
                            status (:status response)]
                        (if (= 200 status)
                          (do
                            (reset! knot-suggestions (get-in response [:body :knots]))
                            (reset! show-suggestions (not (empty? @knot-suggestions))))
                          (do
                            (reset! knot-suggestions [])
                            (reset! show-suggestions false)))))

                  (empty? prefix)
                  (reset! show-suggestions false)))
              (select-suggestion [suggestion]
                (reset! search-text suggestion)
                (reset! show-suggestions false))
              (search []
                (cond (= "." @search-text)
                      (do
                        (reset! search-type "password")
                        (reset! search-text "")
                        (reset! show-suggestions false))

                      (= ".bi" @search-text)
                      (logout)

                      (and (= "password" @search-type)
                           (not (nil? @search-text)))
                      (login @search-text)

                      :else nil))]
        [:div.container.is-max-tablet
         [:section.section
          [:header [:h1.is-size-5
                    [:a.has-text-grey {:on-click #(rfe/push-state :index)}
                     "@KNOT"]]]

          [sound-component]

          [view-page {:view view
                      :key  id}]

          [:br]

          [:form {:on-submit (fn [e]
                               (.preventDefault e))}
           [:div.field
            [:div.control
             [:input.input {:type        @search-type
                            :value       @search-text
                            :on-change   (fn [e]
                                           (let [value (-> e .-target .-value)]
                                             (reset! search-text value)
                                             (fetch-knot-suggestions value)))
                            :on-key-down (fn [e]
                                           (when (= "Enter" (.-key e)) ;; 엔터 키 감지
                                             (reset! show-suggestions false)
                                             (search)))
                            :on-blur     (fn [_] 
                                           (js/setTimeout 
                                             (fn [] 
                                               (reset! show-suggestions false)) 
                                             200))
                            }]]
            (when (and @show-suggestions (seq @knot-suggestions))
              [:div.dropdown.is-active {:style {:width "100%"}}
               [:div.dropdown-menu {:style {:width "100%"}}
                [:div.dropdown-content
                 (for [suggestion @knot-suggestions]
                   ^{:key suggestion}
                   [:a.dropdown-item {:on-click #(select-suggestion suggestion)}
                    suggestion])]]])]]
          [:footer [:small.has-text-grey "powered by knot-log"]
           ]]]))
    not-found))

(defonce initialized? (r/atom false))

(defn ^:dev/after-load mount-root []
  (when-not @initialized?
    (reset! initialized? true)
    (rfe/start!
      (rf/router routes)
      (fn [new-match]
        (reset! app-state new-match))
      {:use-fragment false}))

  (let [container (.getElementById js/document "app")]
    (if-not @root
      (reset! root (createRoot container))
      nil)
    (.render @root (r/as-element [template-page])))
  (reset! is-login (is-cookie-auth)))

(defn ^:export main []
  (mount-root))
