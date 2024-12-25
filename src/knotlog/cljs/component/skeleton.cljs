(ns knotlog.cljs.component.skeleton)

(defn skeleton-component [{:keys [is-login]}]
  (fn []
    [:section.section
     [:div
      (if @is-login
        [:div.tags
         [:span.tag.is-info
          [:a.has-text-black "content"]]
         [:span.tag.is-warning
          [:a.has-text-black "knot"]]
         [:span.tag.is-light
          [:a.has-text-black "date"]]
         [:span.tag.is-primary
          [:a.has-text-black "piece+"]]])
      [:h1.is-size-3 "*"]
      [:div "loading..."]
      [:br]
      [:p [:small "0000년 00월 00일" ]]
      [:br]
      [:div.buttons
       [:button.button.is-small.is-static "prev"]
       [:button.button.is-small.is-static "next"]]]]))