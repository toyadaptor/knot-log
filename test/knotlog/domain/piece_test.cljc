(ns knotlog.domain.piece-test
  (:require [clojure.test :refer [deftest testing is]]
            [knotlog.domain.piece :as piece]))

(deftest create-piece-test
  (testing "Creating a piece with content and date information"
    (let [content "Test content"
          base-year "2023"
          base-month-day "0101"
          result (piece/create-piece content base-year base-month-day)]
      (is (= content (:content result)))
      (is (= base-year (:base-year result)))
      (is (= base-month-day (:base-month-day result))))))

(deftest piece-with-links-data-test
  (testing "Preparing piece with links data structure"
    (let [piece {:id 1 :content "Test content"}
          links-out [{:id 2 :knot "test-out"}]
          links-in [{:id 3 :knot "test-in"}]
          prev-date {:id 4 :base-year "2022" :base-month-day "1231"}
          next-date {:id 5 :base-year "2023" :base-month-day "0102"}
          files [{:id 6 :uri-path "/test.jpg"}]
          result (piece/piece-with-links-data piece links-out links-in prev-date next-date files)]
      (is (= piece (:piece result)))
      (is (= links-out (:link-out result)))
      (is (= links-in (:link-in result)))
      (is (= prev-date (:prev-date result)))
      (is (= next-date (:next-date result)))
      (is (= files (:files result))))))

(deftest not-found-piece-data-test
  (testing "Creating a not found piece data structure"
    (let [result (piece/not-found-piece-data)]
      (is (= "4o4" (:knot result)))
      (is (= "no piece" (:content result)))
      (is (string? (:base_year result)))
      (is (string? (:base_month_day result))))))

(deftest knots-search-result-test
  (testing "Formatting knots search result"
    (let [knots [{:knot "test1"} {:knot "test2"}]
          result (piece/knots-search-result knots)]
      (is (= ["test1" "test2"] (:knots result))))))