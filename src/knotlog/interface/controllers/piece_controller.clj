(ns knotlog.interface.controllers.piece-controller
  (:require [knotlog.application.piece-service :as piece-service]
            [knotlog.application.link-service :as link-service]
            [knotlog.application.file-service :as file-service]
            [knotlog.common.util :refer [now-time-str]]))

(defn handle-piece-latest
  "Handle request for the latest piece"
  [piece-repository]
  (if-let [piece (piece-service/get-latest-piece piece-repository)]
    {:id (:id piece)}
    (piece-service/create-piece piece-repository "hi")))

(defn handle-piece
  "Handle request for a piece by ID"
  [piece-repository link-repository file-repository piece-id]
  (if-let [result (piece-service/get-piece-with-links piece-repository link-repository file-repository piece-id)]
    result
    {:piece {:knot           "4o4" :content "no piece"
             :base_year      (now-time-str {:style :y})
             :base_month_day (now-time-str {:style :md})}}))

(defn handle-piece-create
  "Handle request to create a new piece"
  [piece-repository content]
  (piece-service/create-piece piece-repository content))

(defn handle-piece-update
  "Handle request to update a piece"
  [piece-repository id data]
  (cond
    (:content data) (piece-service/update-piece-content piece-repository id (:content data))
    (:knot data) (piece-service/update-piece-knot piece-repository id (:knot data))
    (and (:base_year data) (:base_month_day data)) 
    (piece-service/update-piece-date piece-repository id (:base_year data) (:base_month_day data))))

(defn handle-knot-link-create
  "Handle request to create a link between a knot and a piece"
  [piece-repository link-repository piece-id knot]
  (link-service/create-link piece-repository link-repository piece-id knot))

(defn handle-knot-link-delete
  "Handle request to delete a link"
  [link-repository link-id]
  (link-service/delete-link link-repository link-id))

(defn handle-file-upload
  "Handle request to upload a file"
  [file-repository file-storage piece-id files]
  (try
    (doseq [file (if (map? files) [files] files)]
      (let [file-name (:filename file)
            temp-file (.getAbsolutePath (:tempfile file))
            destination (str "uploads/" piece-id "/" file-name)]
        (file-service/upload-file file-repository file-storage piece-id temp-file destination)))
    {:files (map :filename files)}
    (catch Exception e
      {:message (.getMessage e)})))

(defn handle-knots-search
  "Handle request to search for knots by prefix"
  [piece-repository prefix]
  (let [knots (piece-service/search-knots-by-prefix piece-repository prefix)]
    {:knots (map :knot knots)}))
