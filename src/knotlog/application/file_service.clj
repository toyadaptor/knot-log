(ns knotlog.application.file-service
  (:require [knotlog.domain.protocols :as p]))

(defn upload-file
  "Upload a file and save its metadata"
  [file-repository file-storage piece-id local-path destination-path]
  (p/upload-file file-storage local-path destination-path)
  (p/save-file file-repository piece-id destination-path))

(defn format-upload-result
  "Format the result of a file upload operation (pure function)"
  [files]
  {:files (map :filename files)})

(defn format-error-result
  "Format an error result (pure function)"
  [error-message]
  {:message error-message})
(defn get-files-by-piece!
  "Get files for a piece"
  [file-repository piece-id]
  (p/find-files-by-piece file-repository piece-id))

(defn handle-file-upload!
  "Handle request to upload a file"
  [file-repository file-storage piece-id files]
  (try
    (doseq [file (if (map? files) [files] files)]
      (let [file-name (:filename file)
            temp-file (.getAbsolutePath (:tempfile file))
            destination (str "uploads/" piece-id "/" file-name)]
        (upload-file file-repository file-storage piece-id temp-file destination)))
    (format-upload-result files)
    (catch Exception e
      (format-error-result (.getMessage e)))))
