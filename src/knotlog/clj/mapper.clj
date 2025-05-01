(ns knotlog.clj.mapper
  (:require [clojure.java.jdbc :as j]
            [honey.sql :as sql]
            [knotlog.clj.config :refer [db-config]]
            [knotlog.cljc.util :refer :all]))



(defn select-piece-by-id [piece-id]
  (first (j/query db-config (sql/format
                              {:select :*
                               :from   :knot_piece
                               :where  [:= :id piece-id]}))))

(defn select-piece-latest []
  (first (j/query db-config (sql/format
                              {:select   :*
                               :from     :knot_piece
                               :where    [:= :knot nil]
                               :order-by [[:update_time :desc]]
                               :limit    1}))))

(defn select-piece-recent-list [offset limit]
  (j/query db-config (sql/format
                       {:select   :*
                        :from     :knot_piece
                        :where    [:= :knot nil]
                        :order-by [[:update_time :desc]]
                        :offset   offset
                        :limit    limit})))

(defn select-piece-by-knot [knot]
  (first
    (j/query db-config (sql/format
                         {:select :*
                          :from   :knot_piece
                          :where  [:= :knot knot]}))))


(defn select-piece-prev [update-time]
  (first
    (j/query db-config (sql/format
                         {:select   [:id]
                          :from     :knot_piece
                          :where    [:and [:= :knot nil]
                                     [:< :update_time update-time]]
                          :order-by [[:update-time :desc]]
                          :limit    1}))))

(defn select-piece-next [update-time]
  (first
    (j/query db-config (sql/format
                         {:select   [:id]
                          :from     :knot_piece
                          :where    [:and [:= :knot nil]
                                     [:> :update_time update-time]]
                          :order-by [[:update-time :asc]]
                          :limit    1}))))

(defn select-piece-todays [month-day]
  (j/query db-config (sql/format
                       {:select   [:base_year
                                   [[:max :id] :id]]
                        :from     :knot_piece
                        :where    [:= :base_month_day month-day]
                        :group-by [:base_year]
                        :order-by [[:base_year :desc]]})))

(defn select-piece-prev-by-date [month-day]
  (first
    (j/query db-config (sql/format
                         {:select   [:id]
                          :from     :knot_piece
                          :where    [:and [:= :knot nil]
                                     [:< :base_month_day month-day]]
                          :order-by [[:base_month_day :desc]]
                          :limit    1}))))

(defn select-piece-next-by-date [month-day]
  (first
    (j/query db-config (sql/format
                         {:select   [:id]
                          :from     :knot_piece
                          :where    [:and [:= :knot nil]
                                     [:> :base_month_day month-day]]
                          :order-by [[:base_month_day :asc]]
                          :limit    1}))))

(defn select-piece-id-lower-base [month-day year]
  (first
    (j/query db-config (sql/format
                         {:select   :*
                          :from     :knot_piece
                          :where    [:and [:= :base_month_day month-day]
                                     [:< :base_year year]]
                          :order-by [[:base_year :desc]
                                     [:update-time :desc]]
                          :limit    1}))))


(defn insert-piece [content]
  (first
    (j/query db-config (sql/format
                         {:insert-into :knot_piece
                          :values      [{:content        content
                                         :base_year      (now-time-str {:style :y})
                                         :base_month_day (now-time-str {:style :md})}]
                          :returning   [:id]}))))

(defn update-piece [piece-id data]
  (j/execute! db-config (sql/format
                          {:update :knot_piece
                           :set    (merge data
                                          {:update_time :%now})
                           :where  [:= :id piece-id]})))


(defn delete-piece [piece-id]
  (j/execute! db-config (sql/format
                          {:delete-from :knot_piece
                           :where       [:= :id piece-id]})))

(defn select-link [id]
  (first
    (j/query db-config (sql/format
                         {:select :*
                          :from   :knot_link
                          :where  [:= :id id]}))))

(defn select-link-list-by-knot [knot-id]
  (j/query db-config (sql/format
                       {:select   [:link.piece_id
                                   :piece.update_time]
                        :from     [[:knot_link :link]]
                        :join     [[:knot_piece :piece] [:= :link.piece_id :piece.id]]
                        :order-by [[:link.create_time :desc]]
                        :where    [:= :link.knot_id knot-id]})))


(defn select-link-list-by-piece [piece-id]
  (j/query db-config (sql/format
                       {:select   [:link.id
                                   :link.knot_id
                                   :piece.knot]
                        :from     [[:knot_link :link]]
                        :join     [[:knot_piece :piece] [:= :link.knot_id :piece.id]]
                        :order-by [[:link.create_time :desc]]
                        :where    [:= :link.piece_id piece-id]})))


(defn insert-link [knot-id piece-id]
  (first
    (j/query db-config (sql/format
                         {:insert-into :knot_link
                          :values      [{:knot_id  knot-id
                                         :piece_id piece-id}]
                          :returning   [:id]}))))

(defn delete-link
  ([link-id]
   (j/execute! db-config (sql/format
                           {:delete-from :knot_link
                            :where       [:= :id link-id]})))
  ([knot-id piece-id]
   (j/execute! db-config (sql/format
                           {:delete-from :knot_link
                            :where       [:and [:= :knot_id knot-id]
                                          [:= :piece_id piece-id]]}))))

(defn insert-file [piece-id uri-path]
  (j/execute! db-config (sql/format
                          {:insert-into :knot_file
                           :values      [{:uri_path uri-path
                                          :piece_id piece-id}]})))

(defn select-files [piece-id]
  (j/query db-config (sql/format
                          {:select [:id :create_time :uri_path]
                           :from   :knot_file
                           :where  [:= :piece_id piece-id]})))

(comment
  (insert-file 33 "uploads/33/x.png")
  (select-files 33))


(comment
  (j/execute! db-config
              (sql/format
                {:raw ["CREATE TABLE knot_piece ("
                       "id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL, "
                       "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                       "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                       "base_year CHAR(4) NOT NULL, "
                       "base_month_day CHAR(4) NOT NULL, "
                       "content TEXT NULL, "
                       "summary VARCHAR(300) NULL, "
                       "knot VARCHAR(100) NULL, "
                       "PRIMARY KEY (id))"]}))

  (j/execute! db-config
              (sql/format
                {:raw ["CREATE UNIQUE INDEX ux_knot_piece ON knot_piece (knot)"]}))


  (j/execute! db-config
              (sql/format
                {:raw ["CREATE TABLE knot_link ("
                       "id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL, "
                       "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                       "knot_id BIGINT NOT NULL, "
                       "piece_id BIGINT NOT NULL, "
                       "PRIMARY KEY (id))"]}))

  (j/execute! db-config
              (sql/format
                {:raw ["CREATE UNIQUE INDEX ux_knot_link ON knot_link (knot_id, piece_id)"]}))

  (j/execute! db-config
              (sql/format
                {:raw ["CREATE TABLE knot_file ("
                       "id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL, "
                       "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                       "uri_path VARCHAR(200) NOT NULL, "
                       "piece_id BIGINT NOT NULL, "
                       "PRIMARY KEY (id))"]}))

  (j/execute! db-config
              (sql/format
                {:raw ["CREATE INDEX ix_knot_file_piece_id ON knot_file (piece_id)"]}))

  )
