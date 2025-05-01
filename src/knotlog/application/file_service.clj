(ns knotlog.application.file-service
  (:require [knotlog.domain.file :as file]
            [knotlog.domain.protocols :as p]))

(defn upload-file
  "Upload a file and save its metadata"
  [file-repository file-storage piece-id local-path destination-path]
  (p/upload-file file-storage local-path destination-path)
  (p/save-file file-repository piece-id destination-path))

(defn get-files-by-piece
  "Get files for a piece"
  [file-repository piece-id]
  (p/find-files-by-piece file-repository piece-id))