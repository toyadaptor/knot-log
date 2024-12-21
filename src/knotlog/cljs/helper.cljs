(ns knotlog.cljs.helper)

(defn is-cookie-auth []
  (let [cookies (js/document.cookie.split "; ")]
    (some (fn [cookie]
            (let [[k v] (clojure.string/split cookie #"=")]
              (when (= k "login")
                (= "1" v))))
          cookies)))

(defn update-query-path [new-query-path]
  (.pushState js/history nil "" new-query-path))