(ns knotlog.application.file-service
  (:require [knotlog.infrastructure.config :as config]
            [knotlog.interface.repositories.file-storage-interface :as file-storage-i]
            [knotlog.interface.repositories.file-interface :as file-i]
            [knotlog.domain.file :as file]
            [taoensso.timbre :as log]))

(defn handle-file-upload!
  [file-repository file-storage piece-id files]
  (try
    (doseq [file (if (map? files) [files] files)]
      (let [file-name (:filename file)
            local-path (.getAbsolutePath (:tempfile file))
            destination-path (str "uploads/" piece-id "/" file-name)]
        (file-storage-i/upload-file file-storage local-path destination-path)
        (log/info "upload-file firebase : " destination-path)
        (file-i/save-file file-repository piece-id destination-path)
        (log/info "upload-file save repository: " destination-path)))
    (file/format-upload-result files)
    (catch Exception e
      (file/format-error-result (.getMessage e)))))

(defn handle-file-delete!
  [file-repository file-storage id file-id]
  )

(comment
  (let [fire-storage (knotlog.infrastructure.firebase-storage/->FirebaseStorageImpl config/firebase-config)]
    (file-storage-i/upload-file fire-storage "/Users/snail/Desktop/butterfly.png" "uploads/1/butterfly.png")))


