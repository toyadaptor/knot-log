(ns knotlog.application.piece-service
  (:require [knotlog.interface.repositories.piece-interface :as piece-i]
            [knotlog.interface.repositories.link-interface :as link-i]
            [knotlog.interface.repositories.file-interface :as file-i]
            [knotlog.domain.piece :as piece]))

(defn create-piece!
  [piece-repository piece]
  (let [new-piece (piece/create-piece piece)]
    (piece-i/save-piece piece-repository new-piece)))

(defn piece-update!
  [piece-repository id piece]
  (let [updated-piece (piece/update-piece piece)]
    (piece-i/update-piece piece-repository id updated-piece)))

(defn get-or-create-knot!
  "Get a knot by ID or create it if it doesn't exist"
  [piece-repository knot]
  (if-let [knot-piece (piece-i/find-piece-by-knot piece-repository knot)]
    knot-piece
    (let [{:keys [id] :as new-piece} (create-piece! piece-repository ".")
          updated-piece (piece-i/update-piece id piece-repository {:knot knot})]
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



(defn handle-knots-search!
  "Handle request to search for knots by prefix"
  [piece-repository prefix]
  (let [knots (piece-i/find-knots-by-prefix piece-repository prefix)]
    (piece/knots-search-result knots)))
