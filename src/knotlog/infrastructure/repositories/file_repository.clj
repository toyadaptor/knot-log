(ns knotlog.infrastructure.repositories.file-repository
  (:require [knotlog.interface.repositories.file-interface :as p]
            [clojure.java.jdbc :as j]
            [honey.sql :as sql]))

(defrecord FileRepositoryImpl [db-config]
  p/FileRepository
  
  (find-files-by-piece [_ piece-id]
    (j/query db-config (sql/format
                         {:select [:id :create_time :uri_path]
                          :from   :knot_file
                          :where  [:= :piece_id piece-id]})))
  
  (save-file [_ piece-id uri-path]
    (j/execute! db-config (sql/format
                            {:insert-into :knot_file
                             :values      [{:uri_path uri-path
                                            :piece_id piece-id}]}))))

(defn create-file-repository [db-config]
  (->FileRepositoryImpl db-config))