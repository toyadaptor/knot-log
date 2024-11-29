(ns knotlog.clj.util
  (:require [tick.core :as t]))

(defn time-format [zoned-date-time & {:keys [style] :or {style :ymd}}]
  (cond
    (= :ymd style)
    (t/format (t/formatter "yyyyMMdd") zoned-date-time)

    (= :md style)
    (t/format (t/formatter "MMdd") zoned-date-time)))

(defn now-time-str [options]
  (time-format (t/zoned-date-time (t/now)) options))

