(ns knotlog.application.piece-service
  (:require [knotlog.interface.repositories.piece-interface :as piece-i]
            [knotlog.interface.repositories.link-interface :as link-i]
            [knotlog.interface.repositories.file-interface :as file-i]
            [knotlog.domain.piece :as piece]
            [knotlog.common.util :refer [now-time-str]]))

(defn create-piece!
  "Create a new piece"
  [piece-repository content]
  (let [base-year (now-time-str {:style :y})
        base-month-day (now-time-str {:style :md})
        new-piece (piece/create-piece content base-year base-month-day)]
    (piece-i/save-piece piece-repository new-piece)))

(defn get-or-create-knot!
  "Get a knot by ID or create it if it doesn't exist"
  [piece-repository knot]
  (if-let [knot-piece (piece-i/find-piece-by-knot piece-repository knot)]
    knot-piece
    (let [{:keys [id] :as new-piece} (create-piece! piece-repository ".")
          updated-piece (piece-i/update-piece piece-repository id {:knot knot})]
      (or updated-piece (assoc new-piece :knot knot)))))

(defn handle-piece-latest!
  "Handle request for the latest piece"
  [piece-repository]
  (if-let [piece (piece-i/find-latest-piece piece-repository)]
    {:id (:id piece)}
    (create-piece! piece-repository "hi")))

(defn handle-piece!
  "Handle request for a piece by ID"
  [piece-repository link-repository file-repository piece-id]
  (if-let [result (when-let [piece (piece-i/find-piece-by-id piece-repository piece-id)]
                    (piece/piece-with-links-data
                      piece
                      (link-i/find-links-by-knot link-repository piece-id)
                      (link-i/find-links-by-piece link-repository piece-id)
                      (piece-i/find-piece-prev piece-repository (:update_time piece))
                      (piece-i/find-piece-next piece-repository (:update_time piece))
                      (file-i/find-files-by-piece file-repository piece-id)))]
    result
    {:piece (piece/not-found-piece-data)}))

(defn handle-piece-update!
  "Handle request to update a piece"
  [piece-repository id data]
  (cond
    (:content data)
    (piece-i/update-piece piece-repository id {:content (:content data)})

    (:knot data)
    (piece-i/update-piece piece-repository id {:knot (when-not (empty? (:knot data)) (:knot data))})

    (and (:base_year data) (:base_month_day data))
    (piece-i/update-piece piece-repository id {:base-year      (:base_year data)
                                               :base-month-day (:base_month_day data)})))

(defn handle-knots-search!
  "Handle request to search for knots by prefix"
  [piece-repository prefix]
  (let [knots (piece-i/find-knots-by-prefix piece-repository prefix)]
    (piece/knots-search-result knots)))
