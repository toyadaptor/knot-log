(ns knotlog.cljs.convert
  (:require [clojure.string :as str]))

(def processors
  [{:pattern #"@img\s+([^@]+)@"
    :process (fn [content]
               (str/replace content
                            #"@img\s+([^@]+)@"
                            (fn [[_ img-path]]
                              (str "<img src=\"" (str/trim img-path) "\" alt=\"\" />"))))}

   {:pattern #"@link\s+([^|]+)\|([^@]+)@"
    :process (fn [content]
               (str/replace content
                            #"@link\s+([^|]+)\|([^@]+)@"
                            (fn [[_ url text]]
                              (str "<a href=\"" (str/trim url) "\">" (str/trim text) "</a>"))))}])

(defn process-content [content]
  (cond
    (not (string? content)) content
    (str/blank? content) ""
    :else (reduce (fn [result processor]
                    ((:process processor) result))
                  content
                  processors)))
