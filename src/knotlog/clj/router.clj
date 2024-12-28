(ns knotlog.clj.router
  (:require
    [knotlog.clj.auth :as auth]
    [knotlog.clj.service :as service]
    [muuntaja.core :as muun]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as rrc]
    [reitit.ring.middleware.muuntaja :as rrm-muuntaja]
    [reitit.ring.middleware.parameters :as rrm-parameter]
    [reitit.coercion.spec]
    [clj-time.core :as time]
    [buddy.sign.jwt :as jwt]
    [org.httpkit.server :refer [run-server]]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.cookies :refer [wrap-cookies]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]
    ))



(def app-router
  (ring/router
    [["/" {:get {:handler (fn [_]
                            {:status 200 :body {:message "hi"}})}}]

     ["/api"


      ["/piece-latest" {:get {:handler (fn [_]
                                         {:status 200 :body (service/handle-piece-latest)})}}]

      ["/pieces/:id" {:get {:path-params {:id int?}
                            :handler     (fn [{{:keys [id]} :path-params}]
                                           {:status 200 :body (service/handle-piece (Long/parseLong id))})}}]

      ["/login" {:post {:body-params {:password string?}
                        :handler     (fn [{{:keys [password]} :body-params}]
                                       (let [valid? (some-> auth/auth-data
                                                            (get :admin)
                                                            (= password))]
                                         (if valid?
                                           (let [claims {:user :admin
                                                         :exp  (time/plus (time/now) (time/seconds 3600))}
                                                 token (jwt/sign claims auth/auth-secret {:alg :hs512})]
                                             {:status  200 :body {:token token}
                                              :cookies {"token" {:value     token
                                                                 :domain    auth/auth-domain
                                                                 :http-only true
                                                                 :secure    true
                                                                 :path      "/"
                                                                 :same-site :lax}
                                                        "login" {:value     "1"
                                                                 :domain    auth/auth-domain
                                                                 :http-only false
                                                                 :secure    false
                                                                 :path      "/"
                                                                 :same-site :lax}}})

                                           {:status 400 :body {:error-text "nop!"}})))}}]

      ["/private"
       {:middleware [[auth/wrap-role-authorization [:admin]]]}

       ["/pieces" {:post {:body-params {:content string?}
                          :handler     (fn [{{:keys [content]} :body-params}]
                                         {:status 200 :body (service/handle-piece-create content)})}}]

       ["/pieces/:id/content" {:put {:path-params {:id int?}
                                     :body-params {:content string?}
                                     :handler     (fn [{{:keys [id]}      :path-params
                                                        {:keys [content]} :body-params}]
                                                    (service/handle-piece-update (Long/parseLong id) {:content content})
                                                    {:status 200 :body {}})}}]

       ["/pieces/:id/knot" {:put {:path-params {:id int?}
                                  :body-params {:knot string?}
                                  :handler     (fn [{{:keys [id]}   :path-params
                                                     {:keys [knot]} :body-params}]
                                                 (service/handle-piece-update (Long/parseLong id) {:knot knot})
                                                 {:status 200 :body {}})}}]

       ["/pieces/:id/date" {:put {:path-params {:id int?}
                                  :body-params {:base_year      string?
                                                :base_month_day string?}
                                  :handler     (fn [{{:keys [id]}                       :path-params
                                                     {:keys [base_year base_month_day]} :body-params}]
                                                 (service/handle-piece-update (Long/parseLong id) {:base_year      base_year
                                                                                                   :base_month_day base_month_day})
                                                 {:status 200 :body {}})}}]

       ["/pieces/:id/files" {:post {:path-params {:id int?}
                                    :handler     (fn [request]
                                                   (let [id (Long/parseLong (-> request :path-params :id))
                                                         files (get (:multipart-params request) "files")]
                                                     {:status 200 :body (service/handle-file-upload id files)}))}}]

       ["/knot-links" {:post {:body-params {:piece_id int?
                                            :knot     string?}
                              :handler     (fn [{{:keys [piece_id knot]} :body-params}]
                                             (service/handle-knot-link-create piece_id knot)
                                             {:status 200 :body {}})}}]
       ["/knot-links/:id" {:delete {:path-params {:id int?}
                                    :handler     (fn [{{:keys [id]} :path-params}]
                                                   (service/handle-knot-link-delete (Long/parseLong id))
                                                   {:status 200 :body {}})}}]
       ["/logout" {:post {:handler (fn [_]
                                     {:status  200
                                      :cookies {"token" {:value "" :max-age 0 :path "/"}
                                                "login" {:value "" :max-age 0 :path "/"}}})}}]
       ]]]


    {:data {:coercion   reitit.coercion.spec/coercion
            :muuntaja   muun/instance
            :middleware [wrap-cookies
                         wrap-multipart-params
                         auth/wrap-jwt-cookie-auth
                         auth/wrap-jwt-authentication
                         rrm-muuntaja/format-middleware
                         rrm-parameter/parameters-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-request-middleware
                         rrc/coerce-response-middleware]}}))



(def app-route
  (ring/ring-handler
    app-router
    (ring/create-resource-handler {:path "/"})
    (ring/create-default-handler)))

(def app
  (-> app-route
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-credentials "true"
                 :access-control-allow-methods [:get :post :put :delete :options]
                 :access-control-allow-headers ["Content-Type" "Authorization"])))

(defn start []
  (run-server app {:port 8000})
  (println "start!"))