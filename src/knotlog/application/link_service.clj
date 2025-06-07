(ns knotlog.application.link-service
  (:require [knotlog.interface.repositories.link-interface :as link-i]
            [knotlog.application.piece-service :as piece-service]))

(defn create-link!
  "Create a link between a knot and a piece"
  [piece-repository link-repository piece-id knot-name]
  (let [knot-piece (piece-service/get-or-create-knot! piece-repository knot-name)]
    (link-i/save-link link-repository (:id knot-piece) piece-id)))

(defn delete-link!
  "Delete a link"
  [link-repository link-id]
  (link-i/delete-link link-repository link-id))