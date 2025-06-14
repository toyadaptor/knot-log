(ns knotlog.application.file-service
  (:require [knotlog.common.util :as util]
            [knotlog.interface.repositories.file-storage-interface :as file-storage-i]
            [knotlog.interface.repositories.file-interface :as file-i]
            [taoensso.timbre :as log]))

(defn handle-file-upload!
  [file-repository file-storage piece-id files]
  (try
    (doseq [file (if (map? files) [files] files)]
      (let [file-name (:filename file)
            local-path (.getAbsolutePath (:tempfile file))
            resize-path (util/resize-image local-path file-name 500)
            destination-path (str "uploads/" piece-id "/" file-name)]
        (file-storage-i/upload-file file-storage resize-path destination-path)
        (log/info "uploaded to firebase storage: " destination-path)
        (file-i/save-file file-repository piece-id destination-path)
        (log/info "saved to repository: " destination-path)))
    {:success true :message "File uploaded successfully"}
    (catch Exception e
      {:success false :message (.getMessage e)})))

(defn handle-file-delete!
  [file-repository file-storage piece-id file-id]
  (try
    (let [file (file-i/find-file-by-id file-repository file-id)]
      (if (and file
               (= (:piece-id file) piece-id))
        (let [uri-path (:uri-path file)]
          (file-storage-i/remove-file file-storage uri-path)
          (log/info "deleted from storage: " uri-path)
          (file-i/remove-file file-repository file-id)
          (log/info "deleted from repository: " file-id)
          {:success true :message "File deleted successfully"})
        {:success false :message "File not found"}))
    (catch Exception e
      (log/error "Error deleting file: " (.getMessage e))
      {:success false :message (.getMessage e)})))
