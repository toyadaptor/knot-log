(ns knotlog.interface.repositories.piece-repository
  (:require [knotlog.domain.protocols :as p]
            [knotlog.domain.piece :as piece]
            [clojure.java.jdbc :as j]
            [honey.sql :as sql]
            [clojure.set :as set]))

(defrecord PieceRepositoryImpl [db-config]
  p/PieceRepository

  (find-piece-by-id [_ id]
    (first (j/query db-config (sql/format
                                {:select :*
                                 :from   :knot_piece
                                 :where  [:= :id id]}))))

  (find-piece-by-knot [_ knot]
    (first
      (j/query db-config (sql/format
                           {:select :*
                            :from   :knot_piece
                            :where  [:= :knot knot]}))))

  (find-latest-piece [_]
    (first (j/query db-config (sql/format
                                {:select   :*
                                 :from     :knot_piece
                                 :where    [:= :knot nil]
                                 :order-by [[:update_time :desc]]
                                 :limit    1}))))

  (find-recent-pieces [_ offset limit]
    (j/query db-config (sql/format
                         {:select   :*
                          :from     :knot_piece
                          :where    [:= :knot nil]
                          :order-by [[:update_time :desc]]
                          :offset   offset
                          :limit    limit})))

  (find-piece-prev [_ update-time]
    (first
      (j/query db-config (sql/format
                           {:select   [:id]
                            :from     :knot_piece
                            :where    [:and [:= :knot nil]
                                       [:< :update_time update-time]]
                            :order-by [[:update_time :desc]]
                            :limit    1}))))

  (find-piece-next [_ update-time]
    (first
      (j/query db-config (sql/format
                           {:select   [:id]
                            :from     :knot_piece
                            :where    [:and [:= :knot nil]
                                       [:> :update_time update-time]]
                            :order-by [[:update_time :asc]]
                            :limit    1}))))

  (find-pieces-by-date [_ month-day]
    (j/query db-config (sql/format
                         {:select   [:base_year
                                     [[:max :id] :id]]
                          :from     :knot_piece
                          :where    [:= :base_month_day month-day]
                          :group-by [:base_year]
                          :order-by [[:base_year :desc]]})))

  (save-piece [_ piece]
    (first
      (j/query db-config (sql/format
                           {:insert-into :knot_piece
                            :values      [{:content        (:content piece)
                                           :base_year      (:base-year piece)
                                           :base_month_day (:base-month-day piece)}]
                            :returning   [:id]}))))

  (update-piece [_ id data]
    (j/execute! db-config (sql/format
                            {:update :knot_piece
                             :set    (merge (set/rename-keys data {:base-year :base_year
                                                                     :base-month-day :base_month_day})
                                            {:update_time :%now})
                             :where  [:= :id id]})))

  (delete-piece [_ id]
    (j/execute! db-config (sql/format
                            {:delete-from :knot_piece
                             :where       [:= :id id]}))))

(defn create-piece-repository [db-config]
  (->PieceRepositoryImpl db-config))
