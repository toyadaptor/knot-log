(ns knotlog.application.piece-service
  (:require [knotlog.domain.piece :as piece]
            [knotlog.domain.protocols :as p]
            [knotlog.common.util :refer [now-time-str]]
            [knotlog.infrastructure.config :as config]))

(defn get-piece-by-id
  "Get a piece by ID"
  [piece-repository id]
  (p/find-piece-by-id piece-repository id))

(defn get-latest-piece
  "Get the latest piece"
  [piece-repository]
  (p/find-latest-piece piece-repository))

(defn get-piece-with-links
  "Get a piece with its links"
  [piece-repository link-repository file-repository id]
  (when-let [piece (p/find-piece-by-id piece-repository id)]
    {:piece     piece
     :link-out  (p/find-links-by-knot link-repository id)
     :link-in   (p/find-links-by-piece link-repository id)
     :prev-date (p/find-piece-prev piece-repository (:update_time piece))
     :next-date (p/find-piece-next piece-repository (:update_time piece))
     :files     (p/find-files-by-piece file-repository id)}))

(defn create-piece
  "Create a new piece"
  [piece-repository content]
  (let [base-year (now-time-str {:style :y})
        base-month-day (now-time-str {:style :md})
        new-piece (piece/create-piece content base-year base-month-day)]
    (p/save-piece piece-repository new-piece)))

(defn update-piece-content
  "Update a piece's content"
  [piece-repository id content]
  (p/update-piece piece-repository id {:content content}))

(defn update-piece-knot
  "Update a piece's knot"
  [piece-repository id knot]
  (p/update-piece piece-repository id {:knot (when-not (empty? knot) knot)}))

(defn update-piece-date
  "Update a piece's date"
  [piece-repository id base-year base-month-day]
  (p/update-piece piece-repository id {:base-year base-year
                                       :base-month-day base-month-day}))

(defn get-or-create-knot
  "Get a knot by ID or create it if it doesn't exist"
  [piece-repository knot]
  (if-let [knot-piece (p/find-piece-by-knot piece-repository knot)]
    knot-piece
    (let [{:keys [id] :as new-piece} (create-piece piece-repository ".")
          updated-piece (p/update-piece piece-repository id {:knot knot})]
      (or updated-piece (assoc new-piece :knot knot)))))

(defn search-knots-by-prefix
  "Search for knots that start with the given prefix"
  [piece-repository prefix]
  (p/find-knots-by-prefix piece-repository prefix))
