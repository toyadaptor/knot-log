(ns knotlog.domain.link)

(defrecord Link [id create-time knot-id piece-id])

(defn create-link
  "Creates a new link between a knot and a piece"
  [knot-id piece-id]
  (map->Link {:knot-id knot-id
              :piece-id piece-id}))