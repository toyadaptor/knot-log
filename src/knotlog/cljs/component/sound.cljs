(ns knotlog.cljs.component.sound
  (:require ["howler" :refer [Howl]]
            [reitit.frontend.easy :as rfe]
            [reagent.core :as r]))

(defn sound-component []
  (let [audio-instance (r/atom nil)
        is-playing (r/atom false)]
    (letfn [(create-audio []
              (when-not @audio-instance
                (reset! audio-instance
                        (Howl. #js {:src      #js ["/assets/james.mp3"]
                                    :autoplay false
                                    :html5    true
                                    :loop     true
                                    :onplay   #(reset! is-playing true)
                                    :onpause  #(reset! is-playing false)
                                    :onstop   #(reset! is-playing false)
                                    :onend    #(reset! is-playing false)}))))
            (play-audio []
              (when js/Howler.ctx
                (when (= (.-state js/Howler.ctx) "suspended")
                  (.resume js/Howler.ctx)))
              (if (nil? @audio-instance)
                (create-audio))
              (.play @audio-instance))

            (pause-audio []
              (when @audio-instance
                (.pause @audio-instance)))]
      (fn []
        [:div.is-pulled-right
         [:div.buttons.has-text-grey
          [:i.fas.fa-music]
          [:a.has-text-grey {:on-click #(rfe/push-state :piece {:id 28})}
           "james - 보수동쿨러"]

          [:a.has-text-grey {:on-click (if @is-playing pause-audio play-audio)}
           [:i.fas {:class (if @is-playing "fa-circle-pause" "fa-circle-play")}]]]]))))