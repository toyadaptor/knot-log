(ns knotlog.application.file-service-test
  (:require [clojure.test :refer :all]
            [knotlog.application.file-service :as sut]
            [knotlog.domain.protocols :as p]))

;; Mock implementations of the required protocols
(defrecord MockFileRepository [files]
  p/FileRepository
  (find-files-by-piece [_ piece-id]
    (filter #(= (:piece-id %) piece-id) (vals files)))
  (save-file [_ piece-id uri-path]
    {:id (str (java.util.UUID/randomUUID))
     :piece-id piece-id
     :uri-path uri-path}))

(defrecord MockFileStorage [uploaded-files]
  p/FileStorage
  (upload-file [_ local-path remote-path]
    (swap! uploaded-files assoc remote-path {:local-path local-path
                                            :remote-path remote-path})
    remote-path))

;; Test data
(def test-files {"1" {:id "1"
                      :piece-id "1"
                      :uri-path "/test/file1.jpg"}
                 "2" {:id "2"
                      :piece-id "2"
                      :uri-path "/test/file2.jpg"}})

;; Tests
(deftest test-get-files-by-piece
  (testing "Get files by piece"
    (let [repo (->MockFileRepository test-files)]
      (is (= 1 (count (sut/get-files-by-piece repo "1"))))
      (is (= 1 (count (sut/get-files-by-piece repo "2"))))
      (is (empty? (sut/get-files-by-piece repo "3"))))))

(deftest test-upload-file
  (testing "Upload file"
    (let [uploaded-files (atom {})
          file-repo (->MockFileRepository {})
          file-storage (->MockFileStorage uploaded-files)
          piece-id "1"
          local-path "/local/path/to/file.jpg"
          remote-path "/remote/path/to/file.jpg"
          result (sut/upload-file file-repo file-storage piece-id local-path remote-path)]
      
      ;; Verify file was uploaded to storage
      (is (contains? @uploaded-files remote-path))
      (is (= local-path (:local-path (@uploaded-files remote-path))))
      
      ;; Verify file metadata was saved in repository
      (is (= piece-id (:piece-id result)))
      (is (= remote-path (:uri-path result)))
      (is (string? (:id result))))))