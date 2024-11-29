(ns knotlog.clj.mapper
  (:require [clojure.java.jdbc :as j]
            [honey.sql :as sql]
            [knotlog.clj.config :refer [db-config]]
            [knotlog.clj.util :refer :all]))



(defn select-piece [piece-id]
  (j/query db-config (sql/format
                       {:select :*
                        :from :knot_piece
                        :where [:= :id piece-id]})))
(defn insert-piece [content]
  (j/execute! db-config (sql/format
                          {:insert-into :knot_piece
                           :values      [{:content  content
                                          :subject  nil
                                          :base_ymd (now-time-str {:style :ymd})
                                          :base_md  (now-time-str {:style :md})}]})))

(defn update-piece [piece-id set-map]
  (j/execute! db-config (sql/format
                          {:update :knot_piece
                           :set set-map
                           :where [:= :id piece-id]})))

(comment
  (insert-piece "alla")
  (update-piece 1 {:base_ymd "20220101"
                   :base_md "0101"
                   :subject "sj"})

  (select-piece 1)

  )
(defn delete-piece [piece-id]
  (j/execute! db-config (sql/format
                          {:delete-from :knot_piece
                           :where       [:= :id piece-id]})))

(defn insert-link [knot-id piece-id]
  (j/execute! db-config (sql/format
                          {:insert-into :knot_link
                           :values      {:knot_id  knot-id
                                         :piece_id piece-id}}))
  )
(defn delete-link [link-id]
  (j/execute! db-config (sql/format
                          {:delete-from :knot_link
                           :where       [:= :id link-id]})))

(comment
  (j/execute! db-config
              (sql/format
                {:raw ["CREATE TABLE knot_piece ("
                       "id BIGINT GENERATED ALWAYS AS IDENTITY NOT NULL, "
                       "create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                       "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, "
                       "base_ymd CHAR(8) NOT NULL, "
                       "base_md CHAR(4) NOT NULL, "
                       "content TEXT NULL, "
                       "subject VARCHAR(200) NULL, "
                       "PRIMARY KEY (id))"]}))

  (j/execute! db-config
              (sql/format
                {:raw ["CREATE UNIQUE INDEX ux_knot_piece ON knot_piece (subject)"]}))


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

  )
