(ns knotlog.domain.piece
  (:require [knotlog.common.util :refer [now-time-str]]))

(defrecord Piece [id create-time update-time base-year base-month-day content summary knot])

(defn create-piece
  [{:keys [content knot base-year base-month-day]}]
  (map->Piece {:content        content
               :knot           (if (empty? knot) nil knot)
               :base-year      (if (empty? base-year) (now-time-str {:style :y}) base-year)
               :base-month-day (if (empty? base-month-day) (now-time-str {:style :md}) base-month-day)
               :create-time    :%
               :update-time    :%}))

(defn update-piece
  [{:keys [content knot base-year base-month-day]}]
  (map->Piece {:content        content
               :knot           (if (empty? knot) nil knot)
               :base-year      (if (empty? base-year) (now-time-str {:style :y}) base-year)
               :base-month-day (if (empty? base-month-day) (now-time-str {:style :md}) base-month-day)
               :update-time    :%}))

(defn piece-with-links-data
  "Prepare piece with links data structure"
  [piece links-out links-in prev-date next-date files]
  {:piece     piece
   :link-out  links-out
   :link-in   links-in
   :prev-date prev-date
   :next-date next-date
   :files     files})

(defn not-found-piece-data
  "Create a not found piece data structure"
  []
  {:knot           "4o4"
   :content        "no piece"
   :base_year      (now-time-str {:style :y})
   :base_month_day (now-time-str {:style :md})})

(defn knots-search-result
  "Format knots search result"
  [knots]
  {:knots (map :knot knots)})


