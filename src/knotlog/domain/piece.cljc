(ns knotlog.domain.piece
  (:require [knotlog.domain.protocols :as p]))

(defrecord Piece [id create-time update-time base-year base-month-day content summary knot])

(defn create-piece
  "Creates a new piece with the given content"
  [content base-year base-month-day]
  (map->Piece {:content content
               :base-year base-year
               :base-month-day base-month-day}))

(defn update-piece-content
  "Updates the content of a piece"
  [piece content]
  (assoc piece :content content))

(defn update-piece-knot
  "Updates the knot of a piece"
  [piece knot]
  (assoc piece :knot knot))

(defn update-piece-date
  "Updates the date of a piece"
  [piece base-year base-month-day]
  (assoc piece 
         :base-year base-year
         :base-month-day base-month-day))

(defn piece->knot
  "Converts a piece to a knot"
  [piece knot-id]
  (assoc piece :knot knot-id))