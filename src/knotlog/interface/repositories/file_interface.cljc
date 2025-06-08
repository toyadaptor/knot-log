(ns knotlog.interface.repositories.file-interface)

(defprotocol FileRepository
  "Protocol for file repository operations"
  (find-files-by-piece [this piece-id] "Find files by piece")

  (find-file-by-id [this file-id] "Find file by id")
  (save-file [this piece-id uri-path] "Save a file")
  (remove-file [this file-id] "Save a file"))


