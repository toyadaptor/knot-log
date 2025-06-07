(ns knotlog.infrastructure.firebase-storage
  (:require [knotlog.interface.repositories.file-storage-interface :as p])
  (:import [com.google.auth.oauth2 GoogleCredentials]
           [com.google.firebase FirebaseApp FirebaseOptions]
           [com.google.firebase.cloud StorageClient]
           [com.google.cloud.storage Bucket$BlobTargetOption]
           [java.nio.file Files Paths]
           [java.io FileInputStream]))

(defrecord FirebaseStorageImpl [firebase-config]
  p/FileStorage

  (upload-file [_ local-file-path destination-path]
    (let [bucket (.bucket (StorageClient/getInstance))
          path (Paths/get local-file-path (into-array String []))
          file-bytes (Files/readAllBytes path)]
      (try
        (let [blob (.create bucket destination-path file-bytes (into-array Bucket$BlobTargetOption []))]
          (println (str "File uploaded successfully to: " (.getName blob))))
        (catch Exception e
          (println (str "Failed to upload file: " (.getMessage e))))))))

(defn initialize-firebase [firebase-config]
  (when (empty? (FirebaseApp/getApps))
    (let [service-account-path (:account-key firebase-config)
          service-account (FileInputStream. service-account-path)
          options (-> (FirebaseOptions/builder)
                      (.setCredentials (GoogleCredentials/fromStream service-account))
                      (.setStorageBucket (:bucket-name firebase-config))
                      .build)]
      (FirebaseApp/initializeApp options)
      (println "Firebase initialized successfully."))))

(defn create-firebase-storage [firebase-config]
  (initialize-firebase firebase-config)
  (->FirebaseStorageImpl firebase-config))
