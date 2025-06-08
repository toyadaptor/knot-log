(ns knotlog.interface.repositories.piece-interface)

;; Repository protocols
(defprotocol PieceRepository
  "Protocol for piece repository operations"
  (find-piece-by-id [this id] "Find a piece by ID")
  (find-piece-by-knot [this knot] "Find a piece by knot")
  (find-knots-by-prefix [this prefix] "Find knots by prefix")
  (find-latest-piece [this] "Find the latest piece")
  (find-recent-pieces [this offset limit] "Find recent pieces")
  (find-piece-prev [this update-time] "Find the previous piece")
  (find-piece-next [this update-time] "Find the next piece")
  (save-piece [this piece] "Save a piece")
  (update-piece [this id piece] "Update a piece")
  (delete-piece [this id] "Delete a piece"))

