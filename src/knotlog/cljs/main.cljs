(ns knotlog.cljs.main
  (:require [reagent.core :as r]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [reitit.coercion.spec :as rss]
            [knotlog.cljs.action :as s]
            [knotlog.cljs.pieces :as pieces]
            ["react-dom/client" :refer [createRoot]]))

(defonce root (r/atom nil))
(defonce app-state (r/atom nil))

(defn not-found []
  [:section.section
   [:h1.is-size-3 "4o4 not found"]])

(defn template [{:keys [key view]}]
  (r/create-class
    {:reagent-render
     (fn []
       [:div.container.is-max-tablet
        [:header [:h1.is-size-3 "KNOT"]]

        [view {:id key}]

        [:br]

        [:form {:on-submit (fn [e]
                             (.preventDefault e))}
         [:input.input {:type        @s/search-type
                        :value       @s/search-text
                        :on-change   #(reset! s/search-text (-> % .-target .-value))
                        :on-key-down (fn [e]
                                       (when (= "Enter" (.-key e)) ;; 엔터 키 감지
                                         (s/search-action)))
                        }]]

        [:span.icon-text [:i.fa-solid.fa-angles-right]]
        [:footer [:small [:i "powered by knotlog"]]]])}))

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
      (rf/router routes {:data {:coercion rss/coercion}})
      (fn [new-match]
        (reset! app-state new-match))
      {:use-fragment false}))


  (js/console.log "main after-load.")
  (let [container (.getElementById js/document "app")]
    (if-not @root
      (reset! root (createRoot container))
      nil)
    (.render @root (r/as-element [current-page]))))


(defn ^:export main []
  (mount-root))

