(ns knotlog.infrastructure.repositories.link-repository
  (:require [knotlog.interface.repositories.link-interface :as p]
            [clojure.java.jdbc :as j]
            [honey.sql :as sql]))

(defrecord LinkRepositoryImpl [db-config]
  p/LinkRepository
  
  (find-link-by-id [_ id]
    (first
      (j/query db-config (sql/format
                           {:select :*
                            :from   :knot_link
                            :where  [:= :id id]}))))
  
  (find-links-by-knot [_ knot-id]
    (j/query db-config (sql/format
                         {:select   [:link.piece_id
                                     :piece.knot
                                     :piece.update_time]
                          :from     [[:knot_link :link]]
                          :join     [[:knot_piece :piece] [:= :link.piece_id :piece.id]]
                          :order-by [[:link.create_time :desc]]
                          :where    [:= :link.knot_id knot-id]})))
  
  (find-links-by-piece [_ piece-id]
    (j/query db-config (sql/format
                         {:select   [:link.id
                                     :link.knot_id
                                     :piece.knot]
                          :from     [[:knot_link :link]]
                          :join     [[:knot_piece :piece] [:= :link.knot_id :piece.id]]
                          :order-by [[:link.create_time :desc]]
                          :where    [:= :link.piece_id piece-id]})))
  
  (save-link [_ knot-id piece-id]
    (first
      (j/query db-config (sql/format
                           {:insert-into :knot_link
                            :values      [{:knot_id  knot-id
                                           :piece_id piece-id}]
                            :returning   [:id]}))))
  
  (delete-link [_ link-id]
    (j/execute! db-config (sql/format
                            {:delete-from :knot_link
                             :where       [:= :id link-id]}))))

(defn create-link-repository [db-config]
  (->LinkRepositoryImpl db-config))