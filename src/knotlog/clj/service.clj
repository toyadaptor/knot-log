(ns knotlog.clj.service
  (:require [knotlog.clj.firebase :as fb]
            [knotlog.cljc.util :refer :all]
            [knotlog.clj.mapper :as mapper]))


(defn knot-get-or-create [knot]
  (if-let [knot (mapper/select-piece-by-knot knot)]
    knot
    (let [{:keys [id]} (mapper/insert-piece ".")]
      (mapper/update-piece id {:knot knot})
      (mapper/select-piece-by-id id))))

(defn piece-create [content]
  (mapper/insert-piece content))

(defn link-list-in [piece-id]
  (mapper/select-link-list-by-piece piece-id))

(defn link-list-out [knot-id]
  (mapper/select-link-list-by-knot knot-id))

(defn handle-piece-latest []
  (if-let [piece (mapper/select-piece-latest)]
    {:id (:id piece)}
    (piece-create "hi")))

(defn handle-piece [piece-id]
  (if-let [piece (mapper/select-piece-by-id piece-id)]
    (let [base-month-day (:base_month_day piece)]
      {:piece     piece
       :link-out  (link-list-out piece-id)
       :link-in   (link-list-in piece-id)
       :prev-date (mapper/select-piece-prev-by-date base-month-day)
       :next-date (mapper/select-piece-next-by-date base-month-day)
       :files     (mapper/select-files piece-id)
       ;:todays    (mapper/select-piece-todays base-month-day)
       })
    {:piece {:knot           "4o4" :content "no piece"
             :base_year      (now-time-str {:style :y})
             :base_month_day (now-time-str {:style :md})}}))

(defn handle-piece-create [content]
  (mapper/insert-piece content))

(defn handle-piece-update [id data]
  (mapper/update-piece id data))

(defn handle-knot-link-create [piece_id knot]
  (let [k (knot-get-or-create knot)]
    (mapper/insert-link (:id k) piece_id)))

(defn handle-knot-link-delete [link-id]
  (mapper/delete-link link-id))

(defn handle-file-upload [piece-id files]
  (try
    (doseq [file (if (map? files) [files] files)]
      (let [file-name (:filename file)
            temp-file (.getAbsolutePath (:tempfile file))
            destination (str "uploads/" piece-id "/" file-name)]
        (fb/upload-file temp-file destination)
        (mapper/insert-file piece-id destination)))
    {:files (map :filename files)}
    (catch Exception e
      {:message (.getMessage e)})))
