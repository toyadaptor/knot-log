(ns knotlog.domain.protocols)

;; Repository protocols
(defprotocol PieceRepository
  "Protocol for piece repository operations"
  (find-piece-by-id [this id] "Find a piece by ID")
  (find-piece-by-knot [this knot] "Find a piece by knot")
  (find-latest-piece [this] "Find the latest piece")
  (find-recent-pieces [this offset limit] "Find recent pieces")
  (find-piece-prev [this update-time] "Find the previous piece")
  (find-piece-next [this update-time] "Find the next piece")
  (find-pieces-by-date [this month-day] "Find pieces by date")
  (find-piece-prev-by-date [this month-day] "Find the previous piece by date")
  (find-piece-next-by-date [this month-day] "Find the next piece by date")
  (save-piece [this piece] "Save a piece")
  (update-piece [this id data] "Update a piece")
  (delete-piece [this id] "Delete a piece"))

(defprotocol LinkRepository
  "Protocol for link repository operations"
  (find-link-by-id [this id] "Find a link by ID")
  (find-links-by-knot [this knot-id] "Find links by knot")
  (find-links-by-piece [this piece-id] "Find links by piece")
  (save-link [this knot-id piece-id] "Save a link")
  (delete-link [this id] "Delete a link"))

(defprotocol FileRepository
  "Protocol for file repository operations"
  (find-files-by-piece [this piece-id] "Find files by piece")
  (save-file [this piece-id uri-path] "Save a file"))

(defprotocol FileStorage
  "Protocol for file storage operations"
  (upload-file [this local-path remote-path] "Upload a file"))