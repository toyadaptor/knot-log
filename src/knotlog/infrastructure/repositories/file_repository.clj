(ns knotlog.infrastructure.repositories.file-repository
  (:require [knotlog.interface.repositories.file-interface :as p]
            [clojure.java.jdbc :as j]
            [honey.sql :as sql]
            [camel-snake-kebab.core :as csk]
            [camel-snake-kebab.extras :as cske]))

(defrecord FileRepositoryImpl [db-config]
  p/FileRepository

  (find-files-by-piece [_ piece-id]
    (->> (j/query db-config (sql/format
                              {:select [:id :create_time :uri_path]
                               :from   :knot_file
                               :where  [:= :piece_id piece-id]}))
         (cske/transform-keys csk/->kebab-case-keyword)))

  (find-file-by-id [_ file-id]
    (->> (first (j/query db-config (sql/format
                                     {:select [:id :create_time :uri_path :piece_id]
                                      :from   :knot_file
                                      :where  [:= :id file-id]})))
         (cske/transform-keys csk/->kebab-case-keyword)))

  (save-file [_ piece-id uri-path]
    (j/execute! db-config (sql/format
                            {:insert-into :knot_file
                             :values      [{:uri_path uri-path
                                            :piece_id piece-id}]})))

  (remove-file [_ file-id]
    (j/execute! db-config (sql/format
                            {:delete-from :knot_file
                             :where       [:= :id file-id]}))))

(defn create-file-repository [db-config]
  (->FileRepositoryImpl db-config))
