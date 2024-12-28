(ns knotlog.clj.firebase
  (:require [knotlog.clj.config :refer [firebase-config]])
  (:import [com.google.auth.oauth2 GoogleCredentials]
           [com.google.firebase FirebaseApp FirebaseOptions]
           [com.google.firebase.cloud StorageClient]
           [com.google.cloud.storage Bucket$BlobTargetOption Storage BlobId BlobInfo StorageOptions]
           [java.nio.file Files Paths]
           [java.io FileInputStream]))

(defn initialize-firebase []
  (let [service-account-path (:account-key firebase-config)
        service-account (FileInputStream. service-account-path)
        options (-> (FirebaseOptions/builder)
                    (.setCredentials (GoogleCredentials/fromStream service-account))
                    (.setStorageBucket (:bucket-name firebase-config))
                    .build)]
    (FirebaseApp/initializeApp options)
    (println "Firebase initialized successfully.")))

(defn get-bucket []
  (let [bucket (.bucket (StorageClient/getInstance))]
    (println "Default bucket:" (.getName bucket))
    bucket))


(defn upload-file
  [local-file-path destination-path]
  (let [bucket (.bucket (StorageClient/getInstance)) ;; Firebase Storage 버킷 가져오기
        path (Paths/get local-file-path (into-array String [])) ;; 로컬 경로 처리
        file-bytes (Files/readAllBytes path)] ;; 파일 읽기
    (try
      (let [blob (.create bucket destination-path file-bytes (into-array Bucket$BlobTargetOption []))] ;; 업로드
        (println (str "File uploaded successfully to: " (.getName blob))))
      (catch Exception e
        (println (str "Failed to upload file: " (.getMessage e)))))))

(comment
  (initialize-firebase)
  (upload-file "resources/public/assets/james.mp3" "james.mp3")
  )