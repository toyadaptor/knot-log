(ns knotlog.application.link-service
  (:require [knotlog.domain.protocols :as p]
            [knotlog.application.piece-service :as piece-service]
            [knotlog.application.link-core :as link-core]))

;; Repository access functions (side effects)

(defn create-link!
  "Create a link between a knot and a piece"
  [piece-repository link-repository piece-id knot-name]
  (let [knot-piece (piece-service/get-or-create-knot piece-repository knot-name)]
    (p/save-link link-repository (:id knot-piece) piece-id)))

(defn delete-link!
  "Delete a link"
  [link-repository link-id]
  (p/delete-link link-repository link-id))

(defn get-links-by-piece!
  "Get links for a piece"
  [link-repository piece-id]
  (p/find-links-by-piece link-repository piece-id))

(defn get-links-by-knot!
  "Get links for a knot"
  [link-repository knot-id]
  (p/find-links-by-knot link-repository knot-id))
