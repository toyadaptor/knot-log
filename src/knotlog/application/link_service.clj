(ns knotlog.application.link-service
  (:require [knotlog.domain.link :as link]
            [knotlog.domain.protocols :as p]
            [knotlog.application.piece-service :as piece-service]))

(defn create-link
  "Create a link between a knot and a piece"
  [piece-repository link-repository piece-id knot]
  (let [knot-piece (piece-service/get-or-create-knot piece-repository knot)
        knot-id (:id knot-piece)]
    (p/save-link link-repository knot-id piece-id)))

(defn delete-link
  "Delete a link"
  [link-repository link-id]
  (p/delete-link link-repository link-id))

(defn get-links-by-piece
  "Get links for a piece"
  [link-repository piece-id]
  (p/find-links-by-piece link-repository piece-id))

(defn get-links-by-knot
  "Get links for a knot"
  [link-repository knot-id]
  (p/find-links-by-knot link-repository knot-id))