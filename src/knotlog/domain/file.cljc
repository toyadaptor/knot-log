(ns knotlog.domain.file)

(defrecord File [id create-time uri-path piece-id])

(defn create-file
  "Creates a new file record"
  [uri-path piece-id]
  (map->File {:uri-path uri-path
              :piece-id piece-id}))