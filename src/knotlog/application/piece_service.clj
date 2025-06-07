(ns knotlog.application.piece-service
  (:require [knotlog.domain.protocols :as p]
            [knotlog.domain.piece :as piece]
            [knotlog.common.util :refer [now-time-str]]))

(defn create-piece-data
  "Create a new piece data structure (pure function)"
  [content base-year base-month-day]
  (piece/create-piece content base-year base-month-day))

(defn piece-with-links-data
  "Prepare piece with links data structure (pure function)"
  [piece links-out links-in prev-date next-date files]
  {:piece     piece
   :link-out  links-out
   :link-in   links-in
   :prev-date prev-date
   :next-date next-date
   :files     files})

(defn not-found-piece-data
  "Create a not found piece data structure (pure function)"
  []
  {:knot           "4o4"
   :content        "no piece"
   :base_year      (now-time-str {:style :y})
   :base_month_day (now-time-str {:style :md})})

(defn knots-search-result
  "Format knots search result (pure function)"
  [knots]
  {:knots (map :knot knots)})

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
    (piece-with-links-data
      piece
      (p/find-links-by-knot link-repository id)
      (p/find-links-by-piece link-repository id)
      (p/find-piece-prev piece-repository (:update_time piece))
      (p/find-piece-next piece-repository (:update_time piece))
      (p/find-files-by-piece file-repository id))))

(defn create-piece
  "Create a new piece"
  [piece-repository content]
  (let [base-year (now-time-str {:style :y})
        base-month-day (now-time-str {:style :md})
        new-piece (create-piece-data content base-year base-month-day)]
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

(defn handle-piece-latest!
  "Handle request for the latest piece"
  [piece-repository]
  (if-let [piece (get-latest-piece piece-repository)]
    {:id (:id piece)}
    (create-piece piece-repository "hi")))

(defn handle-piece!
  "Handle request for a piece by ID"
  [piece-repository link-repository file-repository piece-id]
  (if-let [result (get-piece-with-links piece-repository link-repository file-repository piece-id)]
    result
    {:piece (not-found-piece-data)}))

(defn handle-piece-update!
  "Handle request to update a piece"
  [piece-repository id data]
  (cond
    (:content data) (update-piece-content piece-repository id (:content data))
    (:knot data) (update-piece-knot piece-repository id (:knot data))
    (and (:base_year data) (:base_month_day data)) 
    (update-piece-date piece-repository id (:base_year data) (:base_month_day data))))

(defn handle-knots-search!
  "Handle request to search for knots by prefix"
  [piece-repository prefix]
  (let [knots (search-knots-by-prefix piece-repository prefix)]
    (knots-search-result knots)))
