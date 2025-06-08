(ns knotlog.infrastructure.repositories.piece-repository
  (:require [knotlog.interface.repositories.piece-interface :as p]
            [clojure.java.jdbc :as j]
            [honey.sql :as sql]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]))

(defrecord PieceRepositoryImpl [db-config]
  p/PieceRepository

  (find-piece-by-id [_ id]
    (->> (first (j/query db-config (sql/format
                                     {:select :*
                                      :from   :knot_piece
                                      :where  [:= :id id]})))
         (cske/transform-keys csk/->kebab-case-keyword)))

  (find-knots-by-prefix [_ prefix]
    (j/query db-config (sql/format
                         {:select   :knot
                          :from     :knot_piece
                          :where    [:and
                                     [:not= :knot nil]
                                     [:like :knot (str prefix "%")]]
                          :order-by [[:knot :asc]]
                          :limit    10})))

  (find-piece-by-knot [_ knot]
    (->> (first (j/query db-config (sql/format
                                     {:select :*
                                      :from   :knot_piece
                                      :where  [:= :knot knot]})))
         (cske/transform-keys csk/->kebab-case-keyword)))

  (find-latest-piece [_]
    (->> (first (j/query db-config (sql/format
                                  {:select   :*
                                   :from     :knot_piece
                                   :where    [:= :knot nil]
                                   :order-by [[:update_time :desc]]
                                   :limit    1})))
      (cske/transform-keys csk/->kebab-case-keyword)))

  (find-recent-pieces [_ offset limit]
    (->> (j/query db-config (sql/format
                              {:select   :*
                               :from     :knot_piece
                               :where    [:= :knot nil]
                               :order-by [[:update_time :desc]]
                               :offset   offset
                               :limit    limit}))
         (cske/transform-keys csk/->kebab-case-keyword)))

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

  (save-piece [_ piece]
    (first
      (j/query db-config (sql/format
                           {:insert-into :knot_piece
                            :values      [{:content        (:content piece)
                                           :knot           (:knot piece)
                                           :base_year      (:base-year piece)
                                           :base_month_day (:base-month-day piece)
                                           :create_time    :%now
                                           :update_time    :%now}]
                            :returning   [:id]}))))

  (update-piece [_ id piece]
    (j/execute! db-config (sql/format
                            {:update :knot_piece
                             :set    {:content        (:content piece)
                                      :knot           (:knot piece)
                                      :base_year      (:base-year piece)
                                      :base_month_day (:base-month-day piece)
                                      :update_time    :%now}
                             :where  [:= :id id]})))

  (delete-piece [_ id]
    (j/execute! db-config (sql/format
                            {:delete-from :knot_piece
                             :where       [:= :id id]}))))

(defn create-piece-repository [db-config]
  (->PieceRepositoryImpl db-config))
