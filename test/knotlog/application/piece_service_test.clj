(ns knotlog.application.piece-service-test
  (:require [clojure.test :refer :all]
            [knotlog.application.piece-service :as sut]
            [knotlog.domain.piece :as piece]
            [knotlog.domain.protocols :as p]))

;; Mock implementations of the required protocols
(defrecord MockPieceRepository [pieces]
  p/PieceRepository
  (find-piece-by-id [_ id]
    (get pieces id))
  (find-piece-by-knot [_ knot]
    (first (filter #(= (:knot %) knot) (vals pieces))))
  (find-latest-piece [_]
    (last (sort-by :update-time (vals pieces))))
  (find-recent-pieces [_ offset limit]
    (take limit (drop offset (reverse (sort-by :update-time (vals pieces))))))
  (find-piece-prev [_ update-time]
    (last (sort-by :update-time (filter #(< (:update-time %) update-time) (vals pieces)))))
  (find-piece-next [_ update-time]
    (first (sort-by :update-time (filter #(> (:update-time %) update-time) (vals pieces)))))
  (find-pieces-by-date [_ month-day]
    (filter #(= (:base-month-day %) month-day) (vals pieces)))
  (find-piece-prev-by-date [_ month-day]
    (let [all-month-days (sort (distinct (map :base-month-day (vals pieces))))
          idx (.indexOf all-month-days month-day)]
      (when (and (>= idx 0) (< 0 idx))
        (first (filter #(= (:base-month-day %) (nth all-month-days (dec idx))) (vals pieces))))))
  (find-piece-next-by-date [_ month-day]
    (let [all-month-days (sort (distinct (map :base-month-day (vals pieces))))
          idx (.indexOf all-month-days month-day)]
      (when (and (>= idx 0) (< idx (dec (count all-month-days))))
        (first (filter #(= (:base-month-day %) (nth all-month-days (inc idx))) (vals pieces))))))
  (save-piece [_ piece]
    (assoc piece :id (str (java.util.UUID/randomUUID))))
  (update-piece [this id data]
    (when-let [piece (find-piece-by-id this id)]
      (merge piece data)))
  (delete-piece [_ id]
    nil))

(defrecord MockLinkRepository [links]
  p/LinkRepository
  (find-link-by-id [_ id]
    (get links id))
  (find-links-by-knot [_ knot-id]
    (filter #(= (:knot-id %) knot-id) (vals links)))
  (find-links-by-piece [_ piece-id]
    (filter #(= (:piece-id %) piece-id) (vals links)))
  (save-link [_ knot-id piece-id]
    {:id (str (java.util.UUID/randomUUID))
     :knot-id knot-id
     :piece-id piece-id})
  (delete-link [_ id]
    nil))

(defrecord MockFileRepository [files]
  p/FileRepository
  (find-files-by-piece [_ piece-id]
    (filter #(= (:piece-id %) piece-id) (vals files)))
  (save-file [_ piece-id uri-path]
    {:id (str (java.util.UUID/randomUUID))
     :piece-id piece-id
     :uri-path uri-path}))

;; Test data
(def test-piece1 (piece/map->Piece {:id "1"
                                    :create-time "2023-01-01T00:00:00Z"
                                    :update-time "2023-01-01T00:00:00Z"
                                    :base-year "2023"
                                    :base-month-day "0101"
                                    :content "Test content 1"
                                    :summary "Test summary 1"
                                    :knot "test-knot-1"}))

(def test-piece2 (piece/map->Piece {:id "2"
                                    :create-time "2023-01-02T00:00:00Z"
                                    :update-time "2023-01-02T00:00:00Z"
                                    :base-year "2023"
                                    :base-month-day "0102"
                                    :content "Test content 2"
                                    :summary "Test summary 2"
                                    :knot "test-knot-2"}))

(def test-pieces {"1" test-piece1
                  "2" test-piece2})

(def test-links {"1" {:id "1"
                      :knot-id "test-knot-1"
                      :piece-id "2"}})

(def test-files {"1" {:id "1"
                      :piece-id "1"
                      :uri-path "/test/file1.jpg"}})

;; Tests
(deftest test-get-piece-by-id
  (testing "Get piece by ID"
    (let [repo (->MockPieceRepository test-pieces)]
      (is (= test-piece1 (sut/get-piece-by-id repo "1")))
      (is (= test-piece2 (sut/get-piece-by-id repo "2")))
      (is (nil? (sut/get-piece-by-id repo "3"))))))

(deftest test-get-latest-piece
  (testing "Get latest piece"
    (let [repo (->MockPieceRepository test-pieces)]
      (is (= test-piece2 (sut/get-latest-piece repo))))))

(deftest test-get-piece-with-links
  (testing "Get piece with links"
    (let [piece-repo (->MockPieceRepository test-pieces)
          link-repo (->MockLinkRepository test-links)
          file-repo (->MockFileRepository test-files)
          result (sut/get-piece-with-links piece-repo link-repo file-repo "1")]
      (is (= test-piece1 (:piece result)))
      (is (empty? (:link-out result)))
      (is (= 1 (count (:link-in result))))
      (is (= 1 (count (:files result))))
      (is (nil? (:prev-date result)))
      (is (= test-piece2 (:next-date result))))))

(deftest test-update-piece-content
  (testing "Update piece content"
    (let [repo (->MockPieceRepository test-pieces)
          updated (sut/update-piece-content repo "1" "Updated content")]
      (is (= "Updated content" (:content updated))))))

(deftest test-update-piece-knot
  (testing "Update piece knot"
    (let [repo (->MockPieceRepository test-pieces)
          updated (sut/update-piece-knot repo "1" "updated-knot")]
      (is (= "updated-knot" (:knot updated))))))

(deftest test-update-piece-date
  (testing "Update piece date"
    (let [repo (->MockPieceRepository test-pieces)
          updated (sut/update-piece-date repo "1" "2024" "0202")]
      (is (= "2024" (:base-year updated)))
      (is (= "0202" (:base-month-day updated))))))

(deftest test-get-or-create-knot
  (testing "Get existing knot"
    (let [repo (->MockPieceRepository test-pieces)
          result (sut/get-or-create-knot repo "test-knot-1")]
      (is (= test-piece1 result))))
  
  (testing "Create new knot"
    (let [repo (->MockPieceRepository test-pieces)
          result (sut/get-or-create-knot repo "new-knot")]
      (is (= "new-knot" (:knot result))))))

;; With-redefs test for create-piece to mock now-time-str
(deftest test-create-piece
  (testing "Create piece"
    (with-redefs [knotlog.common.util/now-time-str (fn [options]
                                                     (if (= (:style options) :y)
                                                       "2023"
                                                       "0101"))]
      (let [repo (->MockPieceRepository {})
            result (sut/create-piece repo "New content")]
        (is (string? (:id result)))
        (is (= "2023" (:base-year result)))
        (is (= "0101" (:base-month-day result)))
        (is (= "New content" (:content result)))))))