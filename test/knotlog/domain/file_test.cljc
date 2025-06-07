(ns knotlog.domain.file-test
  (:require [clojure.test :refer [deftest testing is]]
            [knotlog.domain.file :as file]))

(deftest format-upload-result-test
  (testing "Formatting the result of a file upload operation"
    (let [files [{:filename "test1.jpg"} {:filename "test2.png"}]
          result (file/format-upload-result files)]
      (is (= ["test1.jpg" "test2.png"] (:files result))))))

(deftest format-error-result-test
  (testing "Formatting an error result"
    (let [error-message "File upload failed"
          result (file/format-error-result error-message)]
      (is (= error-message (:message result))))))

(deftest file-record-test
  (testing "Creating a File record"
    (let [id 1
          create-time "2023-01-01T12:00:00"
          uri-path "/files/test.jpg"
          piece-id 2
          file-record (file/->File id create-time uri-path piece-id)]
      (is (= id (:id file-record)))
      (is (= create-time (:create-time file-record)))
      (is (= uri-path (:uri-path file-record)))
      (is (= piece-id (:piece-id file-record))))))