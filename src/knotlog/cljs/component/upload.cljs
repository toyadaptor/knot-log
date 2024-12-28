(ns knotlog.cljs.component.upload
  (:refer-clojure :exclude [parse-long])
  (:require [knotlog.cljs.helper :refer [get-backend-url]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defn upload-component [{:keys [state-piece reload]}]
  (letfn [(uploads [files]
            (let [form-data (js/FormData.)
                  id (-> @state-piece :piece :id)]
              (doseq [file files]
                (.append form-data "files" file))
              (go
                (let [response (<! (http/post (get-backend-url (str "/api/private/pieces/" id "/files"))
                                              {:body              form-data
                                               :with-credentials? true}))]
                  (if (= 200 (:status response))
                    (reload)
                    (js/console.error "Upload failed!" (:body response)))))))]
    (fn []
      [:div.file.is-small
       [:label.file-label
        [:input.file-input {:type      "file"
                            :multiple  true
                            :on-change #(let [files (array-seq (.-files (.-target %)))]
                                          (uploads files))}]
        [:span.file-cta
         [:span.file-icon
          [:i.fas.fa-upload]]
         [:span.file-label "Small file…"]]]])))