(ns knotlog.cljs.main
  (:refer-clojure :exclude [parse-long])
  (:require [reagent.core :as r]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]
            [knotlog.cljs.helper :refer [is-cookie-auth]]
            [knotlog.cljs.pieces :as pieces]
            [knotlog.cljs.helper :refer [get-backend-url]]
            ["react-dom/client" :refer [createRoot]]))

(defonce root (r/atom nil))
(defonce app-state (r/atom nil))
(def is-login (r/atom false))


(defn not-found []
  [:section.section
   [:h1.is-size-3 "4o4 not found"]])

(defn template [{:keys [key view]}]
  (let [search-type (r/atom "text")
        search-text (r/atom "")]
    (letfn [(search []
              (cond (= ".nn" @search-text)
                    (do
                      (reset! search-type "password")
                      (reset! search-text ""))

                    (= ".bi" @search-text)
                    (logout)

                    (and (= "password" @search-type)
                         (not (nil? @search-text)))
                    (login @search-text)

                    :else nil))
            (login [password]
              (go (let [{:keys [status]} (<! (http/post (get-backend-url "/api/login")
                                                        {:json-params {:password password}}))]
                    (if (= 200 status)
                      (do (reset! is-login true)
                          (reset! search-type "text")
                          (reset! search-text ""))
                      (js/console.log "error")))))
            (logout []
              (go (let [{:keys [status]} (<! (http/post (get-backend-url "/api/private/logout")))]
                    (if (= 200 status)
                      (do (reset! is-login false)
                          (reset! search-type "text")
                          (reset! search-text ""))
                      (js/console.log "error")))))
            ]
      (r/create-class
        {:reagent-render
         (fn []
           [:div.container.is-max-tablet
            [:header [:h1.is-size-3 "KNOT"]]

            [view {:id       key
                   :is-login is-login}]

            [:br]

            [:form {:on-submit (fn [e]
                                 (.preventDefault e))}
             [:input.input {:type        @search-type
                            :value       @search-text
                            :on-change   #(reset! search-text (-> % .-target .-value))
                            :on-key-down (fn [e]
                                           (when (= "Enter" (.-key e)) ;; 엔터 키 감지
                                             (search)))
                            }]]

            [:span.icon-text [:i.fa-solid.fa-angles-right]]
            [:footer [:small [:i "powered by knotlog"]]]])}))))

(def routes
  [["/" {:name :home
         :view (fn [] [:div [:h1 "hi."]
                       [:a {:href     "/days"
                            :on-click (fn [_]
                                        (rfe/push-state ::days))} "days"]])}]
   ["/pieces/:id" {:name       :piece
                   :parameters {:path {:id string?}}
                   :view       pieces/pieces-component}]
   ["/4o4" {:name ::not-found
            :view not-found}]])

(defn current-page []
  (if @app-state
    (let [match @app-state
          view (get-in match [:data :view])
          id (get-in match [:parameters :path :id])]
      [template {:view view
                 :key  id}])
    [template not-found nil]))

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
    (.render @root (r/as-element [current-page])))
  (reset! is-login (is-cookie-auth)))

(defn ^:export main []
  (mount-root))

