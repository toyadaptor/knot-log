(ns knotlog.common.util
  (:require [tick.core :as t]
            [tick.locale-en-us])
  #?(:clj (:import [java.sql Timestamp])))

(defn time-format [zoned-date-time {:keys [style] :or {style :ymd}}]
  (cond
    (= :ymd style)
    (t/format (t/formatter "yyyyMMdd") zoned-date-time)

    (= :korean-full style)
    (t/format (t/formatter "yyyy년 MM월 dd일 hh시 mm분") zoned-date-time)

    (= :knot-full style)
    (clojure.string/replace (t/format (t/formatter "yyyyMMddhhmmss") zoned-date-time)
                            #"0" "o")

    (= :y style)
    (t/format (t/formatter "yyyy") zoned-date-time)

    (= :md style)
    (t/format (t/formatter "MMdd") zoned-date-time)))

(defn now-time-str [options]
  (time-format (t/zoned-date-time (t/now)) options))

(defn parse-iso-date [iso-string]
  (t/zoned-date-time
    (t/instant iso-string)))


(defn iso-str-to [iso-string & {:keys [style] :or {style :korean-full}}]
  (time-format
    (parse-iso-date iso-string)
    {:style style}))


(defn inst-to-sql-timestamp
  "Converts a Clojure #inst to java.sql.Timestamp. Only available in Clojure."
  [inst]
  #?(:clj  (Timestamp. (.getTime inst))
     :cljs (throw (js/Error. "inst-to-sql-timestamp is not available in ClojureScript"))))

(defn base-date-str-to [base-year base-month-day]
  (let [month (subs base-month-day 0 2)
        day (subs base-month-day 2 4)]
    (str base-year "년 " month "월 " day "일")))

(defn storage-url [upload-path]
  #?(:cljs
     (str "https://firebasestorage.googleapis.com/v0/b/knotlog-ae3d9.firebasestorage.app/o/"
          (js/encodeURIComponent upload-path) "?alt=media")))
