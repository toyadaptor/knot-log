(ns knotlog.application.file-service
  (:require [knotlog.interface.repositories.file-storage-interface :as file-storage-i]
            [knotlog.interface.repositories.file-interface :as file-i]
            [knotlog.domain.file :as file]))

(defn upload-file
  "Upload a file and save its metadata"
  [file-repository file-storage piece-id local-path destination-path]
  (file-storage-i/upload-file file-storage local-path destination-path)
  (file-i/save-file file-repository piece-id destination-path))

(defn handle-file-upload!
  "Handle request to upload a file"
  [file-repository file-storage piece-id files]
  (try
    (doseq [file (if (map? files) [files] files)]
      (let [file-name (:filename file)
            temp-file (.getAbsolutePath (:tempfile file))
            destination (str "uploads/" piece-id "/" file-name)]
        (upload-file file-repository file-storage piece-id temp-file destination)))
    (file/format-upload-result files)
    (catch Exception e
      (file/format-error-result (.getMessage e)))))
