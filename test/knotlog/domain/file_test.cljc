(ns knotlog.domain.file-test
  (:require [clojure.test :refer [deftest testing is]]
            [knotlog.domain.file :as file]))

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