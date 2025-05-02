(ns knotlog.interface.routes.router
  (:require
    [knotlog.interface.controllers.piece-controller :as controller]
    [knotlog.infrastructure.config :as config]
    [muuntaja.core :as muun]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as rrc]
    [reitit.ring.middleware.muuntaja :as rrm-muuntaja]
    [reitit.ring.middleware.parameters :as rrm-parameter]
    [reitit.coercion.spec]
    [clj-time.core :as time]
    [buddy.auth :refer [authenticated?]]
    [buddy.auth.backends.token :refer [jws-backend]]
    [buddy.auth.middleware :refer [wrap-authentication]]
    [buddy.sign.jwt :as jwt]
    [org.httpkit.server :refer [run-server]]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.cookies :refer [wrap-cookies]]
    [ring.middleware.multipart-params :refer [wrap-multipart-params]]))

;; Authentication middleware
(defn wrap-jwt-cookie-auth [handler]
  (fn [request]
    (let [token (get-in request [:cookies "token" :value])]
      (if token
        (let [updated-request (assoc-in request [:headers "authorization"] (str "Token " token))]
          (handler updated-request))
        (handler request)))))

(def auth-backend
  (jws-backend {:secret config/auth-secret :options {:alg :hs512}}))

(defn wrap-jwt-authentication [handler]
  (wrap-authentication handler auth-backend))

(defn wrap-role-authorization [handler roles]
  (fn [request]
    (let [user (:identity request)]
      (if (and (authenticated? request)
               (contains? (set roles) (keyword (:user user))))
        (handler request)
        {:status 403 :body {:message "Forbidden"}}))))

;; Router definition
(def app-router
  (ring/router
    [["/" {:get {:handler (fn [_]
                            {:status 200 :body {:message "hi"}})}}]

     ["/api"
      ["/piece-latest" {:get {:handler (fn [_]
                                         {:status 200 :body (controller/handle-piece-latest config/piece-repository)})}}]

      ["/pieces/:id" {:get {:path-params {:id int?}
                            :handler     (fn [{{:keys [id]} :path-params}]
                                           {:status 200 :body (controller/handle-piece 
                                                               config/piece-repository 
                                                               config/link-repository 
                                                               config/file-repository 
                                                               (Long/parseLong id))})}}]

      ["/login" {:post {:body-params {:password string?}
                        :handler     (fn [{{:keys [password]} :body-params}]
                                       (let [valid? (some-> config/auth-data
                                                            (get :admin)
                                                            (= password))]
                                         (if valid?
                                           (let [claims {:user :admin
                                                         :exp  (time/plus (time/now) (time/seconds 3600))}
                                                 token (jwt/sign claims config/auth-secret {:alg :hs512})]
                                             {:status  200 :body {:token token}
                                              :cookies {"token" {:value     token
                                                                 :domain    config/auth-domain
                                                                 :http-only true
                                                                 :secure    true
                                                                 :path      "/"
                                                                 :same-site :lax}
                                                        "login" {:value     "1"
                                                                 :domain    config/auth-domain
                                                                 :http-only false
                                                                 :secure    false
                                                                 :path      "/"
                                                                 :same-site :lax}}})

                                           {:status 400 :body {:error-text "nop!"}})))}}]

      ["/private"
       {:middleware [[wrap-role-authorization [:admin]]]}

       ["/pieces" {:post {:body-params {:content string?}
                          :handler     (fn [{{:keys [content]} :body-params}]
                                         {:status 200 :body (controller/handle-piece-create config/piece-repository content)})}}]

       ["/pieces/:id/content" {:put {:path-params {:id int?}
                                     :body-params {:content string?}
                                     :handler     (fn [{{:keys [id]}      :path-params
                                                        {:keys [content]} :body-params}]
                                                    (controller/handle-piece-update config/piece-repository (Long/parseLong id) {:content content})
                                                    {:status 200 :body {}})}}]

       ["/pieces/:id/knot" {:put {:path-params {:id int?}
                                  :body-params {:knot string?}
                                  :handler     (fn [{{:keys [id]}   :path-params
                                                     {:keys [knot]} :body-params}]
                                                 (controller/handle-piece-update config/piece-repository (Long/parseLong id) {:knot knot})
                                                 {:status 200 :body {}})}}]

       ["/pieces/:id/date" {:put {:path-params {:id int?}
                                  :body-params {:base_year      string?
                                                :base_month_day string?}
                                  :handler     (fn [{{:keys [id]}                       :path-params
                                                     {:keys [base_year base_month_day]} :body-params}]
                                                 (controller/handle-piece-update config/piece-repository (Long/parseLong id) 
                                                                                {:base_year base_year
                                                                                 :base_month_day base_month_day})
                                                 {:status 200 :body {}})}}]

       ["/pieces/:id/files" {:post {:path-params {:id int?}
                                    :handler     (fn [request]
                                                   (let [id (Long/parseLong (-> request :path-params :id))
                                                         files (get (:multipart-params request) "files")
                                                         file-storage (config/init-firebase-storage)]
                                                     {:status 200 :body (controller/handle-file-upload 
                                                                         config/file-repository 
                                                                         file-storage 
                                                                         id 
                                                                         files)}))}}]

       ["/knot-links" {:post {:body-params {:piece_id int?
                                            :knot     string?}
                              :handler     (fn [{{:keys [piece_id knot]} :body-params}]
                                             (controller/handle-knot-link-create 
                                              config/piece-repository 
                                              config/link-repository 
                                              piece_id 
                                              knot)
                                             {:status 200 :body {}})}}]
       ["/knot-links/:id" {:delete {:path-params {:id int?}
                                    :handler     (fn [{{:keys [id]} :path-params}]
                                                   (controller/handle-knot-link-delete config/link-repository (Long/parseLong id))
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
                         wrap-jwt-cookie-auth
                         wrap-jwt-authentication
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
  (println "Server started on port 8000"))
