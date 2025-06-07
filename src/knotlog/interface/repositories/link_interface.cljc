(ns knotlog.interface.repositories.link-interface)

(defprotocol LinkRepository
  "Protocol for link repository operations"
  (find-link-by-id [this id] "Find a link by ID")
  (find-links-by-knot [this knot-id] "Find links by knot")
  (find-links-by-piece [this piece-id] "Find links by piece")
  (save-link [this knot-id piece-id] "Save a link")
  (delete-link [this id] "Delete a link"))

