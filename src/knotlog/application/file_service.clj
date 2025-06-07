(ns knotlog.application.file-service
  (:require [knotlog.infrastructure.config :as config]
            [knotlog.interface.repositories.file-storage-interface :as file-storage-i]
            [knotlog.interface.repositories.file-interface :as file-i]
            [knotlog.domain.file :as file]
            [taoensso.timbre :as log]))

(defn upload-file
  "Upload a file and save its metadata"
  [file-repository file-storage piece-id local-path destination-path]
  (file-storage-i/upload-file file-storage local-path destination-path)
  (log/info "upload-file firebase : " destination-path)
  (file-i/save-file file-repository piece-id destination-path)
  (log/info "upload-file save repository: " destination-path))

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

(comment
  (let [fire-storage (knotlog.infrastructure.firebase-storage/->FirebaseStorageImpl config/firebase-config)]
    (file-storage-i/upload-file fire-storage "/Users/snail/Desktop/butterfly.png" "uploads/1/butterfly.png")))
