(ns knotlog.clj.service
  (:require [knotlog.cljc.util :refer :all]
            [knotlog.clj.mapper :as mapper]))


(defn knot-get-or-create [knot]
  (if-let [knot (mapper/select-piece-by-knot knot)]
    knot
    (let [{:keys [id]} (mapper/insert-piece nil)]
      (mapper/update-piece id {:knot knot})
      (mapper/select-piece-by-id id))))

(defn link-create [piece-id knot-name]
  (let [knot (knot-get-or-create knot-name)
        {:keys [id]} (mapper/insert-link (:id knot) piece-id)]
    (mapper/select-link id)))

(defn piece-create [content]
  (let [piece-id (mapper/insert-piece content)]
    (link-create piece-id (now-time-str {:style :md}))))

(defn link-list-in [piece-id]
  (mapper/select-link-list-by-piece piece-id))

(defn link-list-out [knot-id]
  (mapper/select-link-list-by-knot knot-id))

(defn handle-piece [piece-id]
  (if-let [piece (mapper/select-piece-by-id piece-id)]
    (let [update-time (inst-to-sql-timestamp (:update_time piece))]
      {:piece     piece
       :link-out  (link-list-out piece-id)
       :link-in   (link-list-in piece-id)
       :prev-date (mapper/select-piece-prev update-time)
       :next-date (mapper/select-piece-next update-time)
       :todays    (mapper/select-piece-todays (:base_month_day piece))})))


(defn handle-piece-update [id data]
  (mapper/update-piece id data))

(comment

  (link-create 2 "knot")

  (handle-piece 2))

(defn handle-piece-recent-list [{:keys [offset limit]}]
  (mapper/select-piece-recent-list offset limit))

(defn handle-knot-link-create [knot_id piece_id]
  (mapper/insert-link knot_id piece_id))

(defn handle-link-create [{:keys [piece-id knot-name]}]
  (link-create piece-id knot-name))


(defn handle-piece-create [{:keys [content]}]
  (piece-create content))

(comment
  (mapper/insert-piece "asdf")
  (mapper/update-piece 1 {:knot "knot"})
  (mapper/update-piece 1 {:base_year "2025"})
  (mapper/select-piece-by-knot "knot")

  (now-time-str {:style :md})

  (piece-create "asdf")

  (link-list-out 6)
  (link-list-in 2)

  (mapper/insert-link 6 2)
  (mapper/insert-link 6 3)



  (link-create {:piece-id 2 :knot-name "soso"})
  (link-create {:piece-id 4 :knot-name "hehe"})

  (knot-get-or-create "ping")
  (handle-piece-recent-list {:offset 2 :limit 2}))














