(ns knotlog.common.util
  (:require [tick.core :as t]
            [tick.locale-en-us]
            [clojure.java.io :as io]
            [image-resizer.core :refer [resize]]
            [image-resizer.format :as format])
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
  (if (empty? iso-string)
    "o1o1o1o1o1o1o1"
    (time-format
      (parse-iso-date iso-string)
      {:style style})))

(defn inst-to-sql-timestamp
  "Converts a Clojure #inst to java.sql.Timestamp. Only available in Clojure."
  [inst]
  #?(:clj  (Timestamp. (.getTime inst))
     :cljs (throw (js/Error. "inst-to-sql-timestamp is not available in ClojureScript"))))

(defn base-date-str-to [base-year base-month-day]
  (let [base-str (str base-year base-month-day)]
    (clojure.string/replace base-str #"0" "o")))

(defn storage-url [upload-path]
  #?(:cljs
     (str "https://firebasestorage.googleapis.com/v0/b/knotlog-ae3d9.firebasestorage.app/o/"
          (js/encodeURIComponent upload-path) "?alt=media")))

(defn file-extension [file-name]
  (when (re-find #"\." file-name)
    (last (clojure.string/split file-name #"\."))))

(defn resize-image [local-path file-name max-pixel]
  #?(:clj (let [re (resize (io/file local-path) max-pixel max-pixel)
                ext (file-extension file-name)
                filename (str (first (clojure.string/split local-path #"\.")) "_re." ext)]
            (format/as-file re filename))))

(comment
  (file-extension "asdf.png")

  (resize-image "/tmp/ring-multipart-8965039994217530103.tmp" 400)
  (resize-image "/home/snail/mine.png" 700)
  )