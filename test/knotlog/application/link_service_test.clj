(ns knotlog.application.link-service-test
  (:require [clojure.test :refer :all]
            [knotlog.application.link-service :as sut]
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
  (save-piece [_ piece]
    (assoc piece :id (str (java.util.UUID/randomUUID))))
  (update-piece [this id data]
    (when-let [piece (p/find-piece-by-id this id)]
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

;; Tests
(deftest test-get-links-by-piece
  (testing "Get links by piece"
    (let [link-repo (->MockLinkRepository test-links)]
      (is (empty? (sut/get-links-by-piece! link-repo "1")))
      (is (= 1 (count (sut/get-links-by-piece! link-repo "2"))))
      (is (empty? (sut/get-links-by-piece! link-repo "3"))))))

(deftest test-get-links-by-knot
  (testing "Get links by knot"
    (let [link-repo (->MockLinkRepository test-links)]
      (is (= 1 (count (sut/get-links-by-knot! link-repo "test-knot-1"))))
      (is (empty? (sut/get-links-by-knot! link-repo "test-knot-2")))
      (is (empty? (sut/get-links-by-knot! link-repo "non-existent-knot"))))))

(deftest test-delete-link
  (testing "Delete link"
    (let [link-repo (->MockLinkRepository test-links)]
      (is (nil? (sut/delete-link! link-repo "1"))))))

(deftest test-create-link-existing-knot
  (testing "Create link with existing knot"
    (let [piece-repo (->MockPieceRepository test-pieces)
          link-repo (->MockLinkRepository {})
          piece-id "2"
          knot "test-knot-1"
          result (sut/create-link! piece-repo link-repo piece-id knot)]
      (is (= "test-knot-1" (:knot-id result)))
      (is (= "2" (:piece-id result)))
      (is (string? (:id result))))))

(deftest test-create-link-new-knot
  (testing "Create link with new knot"
    (with-redefs [knotlog.common.util/now-time-str (fn [options]
                                                     (if (= (:style options) :y)
                                                       "2023"
                                                       "0101"))]
      (let [piece-repo (->MockPieceRepository test-pieces)
            link-repo (->MockLinkRepository {})
            piece-id "2"
            knot "new-knot"
            result (sut/create-link! piece-repo link-repo piece-id knot)]
        (is (string? (:id result)))
        (is (= "new-knot" (:knot-id result)))
        (is (= "2" (:piece-id result)))))))
