(ns knotlog.cljs.component.upload
  (:refer-clojure :exclude [parse-long])
  (:require [knotlog.cljs.helper :refer [get-backend-url api-request]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defn upload-component [{:keys [state-piece reload]}]
  (letfn [(uploads [files]
            (let [form-data (js/FormData.)
                  id (-> @state-piece :piece :id)]
              (doseq [file files]
                (.append form-data "files" file))
              (go
                (let [response (<! (api-request :post
                                               (str "/api/private/pieces/" id "/files")
                                               {:body              form-data
                                                :with-credentials? true}))]
                  (if (= 200 (:status response))
                    (reload)
                    (js/console.error "Upload failed!" (:body response)))))))
          (delete-file [file-id]
            (go
              (let [id (-> @state-piece :piece :id)
                    response (<! (api-request :delete
                                             (str "/api/private/pieces/" id "/files/" file-id)
                                             {:with-credentials? true}))]
                (if (= 200 (:status response))
                  (reload)
                  (js/console.error "Delete failed!" (:body response))))))
          (append-img-tag [filename]
            (let [current-content (-> @state-piece :piece :content)
                  new-content (str current-content "@img " filename "@")]
              (swap! state-piece assoc-in [:piece :content] new-content)))]
    (fn []
      [:div
       [:div.file.is-small
        [:label.file-label
         [:input.file-input {:type      "file"
                             :multiple  true
                             :on-change #(let [files (array-seq (.-files (.-target %)))]
                                           (uploads files))}]
         [:span.file-cta
          [:span.file-icon
           [:i.fas.fa-upload]]
          [:span.file-label "Small file…"]]]]

       [:ul
        (for [file (-> @state-piece :files)]
          ^{:key (:id file)}
          [:li
           [:small
            [:span.icon-text
             [:span.has-text-grey {:style {:cursor "pointer"}
                              :on-click #(append-img-tag (:uri-path file))} (:uri-path file)]
             [:a.icon.has-text-black {:on-click #(delete-file (:id file))}
              [:i.fas.fa-circle-xmark]]]]])]])))
