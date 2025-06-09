(ns knotlog.interface.routes.router
  (:require
    [clojure.spec.alpha :refer [nilable]]
    [knotlog.application.piece-service :as piece-service]
    [knotlog.application.link-service :as link-service]
    [knotlog.application.file-service :as file-service]
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

(def app-router
  (ring/router
    [["/" {:get {:handler (fn [_]
                            {:status 200 :body {:message "hi"}})}}]

     ["/api"
      ["/piece-latest" {:get {:handler (fn [_]
                                         {:status 200 :body (piece-service/handle-piece-latest! config/piece-repository)})}}]

      ["/pieces/:id" {:get {:parameters {:path {:id int?}}
                            :handler    (fn [{{{:keys [id]} :path} :parameters}]
                                          {:status 200 :body (piece-service/get-piece!
                                                               config/piece-repository
                                                               config/link-repository
                                                               config/file-repository
                                                               id)})}}]

      ["/login" {:post {:parameters {:body {:password string?}}
                        :handler    (fn [{{{:keys [password]} :body} :parameters}]
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

       ["/pieces" {:post {:parameters {:body {:content        (nilable string?)
                                              :knot           (nilable string?)
                                              :base-year      (nilable string?)
                                              :base-month-day (nilable string?)}}
                          :handler    (fn [{{{:keys [content knot base-year base-month-day]} :body} :parameters}]
                                        {:status 200 :body (piece-service/create-piece! config/piece-repository {:content        content
                                                                                                                 :knot           knot
                                                                                                                 :base-year      base-year
                                                                                                                 :base-month-day base-month-day})})}}]

       ["/pieces/:id" {:put {:parameters {:path {:id int?}
                                          :body {:content        (nilable string?)
                                                 :knot           (nilable string?)
                                                 :base-year      (nilable string?)
                                                 :base-month-day (nilable string?)}}
                             :handler    (fn [{{{:keys [id]}                                    :path
                                                {:keys [content knot base-year base-month-day]} :body} :parameters}]

                                           {:status 200 :body (piece-service/piece-update! config/piece-repository id {:content        content
                                                                                                                       :knot           knot
                                                                                                                       :base-year      base-year
                                                                                                                       :base-month-day base-month-day})})}}]

       ["/pieces/:id/files" {:post {:parameters {:path             {:id int?}
                                                 :multipart-params {:files any?}}
                                    :handler    (fn [{:keys [parameters multipart-params]}]
                                                  (let [id (-> parameters :path :id)
                                                        files (get multipart-params "files")
                                                        file-storage (config/firebase-storage)]
                                                    {:status 200 :body (file-service/handle-file-upload!
                                                                         config/file-repository
                                                                         file-storage
                                                                         id
                                                                         files)}))}}]

       ["/pieces/:id/files/{file-id}" {:delete {:parameters {:path {:id      int?
                                                                    :file-id int?}}
                                                :handler    (fn [{:keys [parameters]}]
                                                              (let [id (-> parameters :path :id)
                                                                    file-id (-> parameters :path :file-id)
                                                                    file-storage (config/firebase-storage)]
                                                                {:status 200 :body (file-service/handle-file-delete!
                                                                                     config/file-repository
                                                                                     file-storage
                                                                                     id
                                                                                     file-id)}))}}]

       ["/knot-links" {:post {:parameters {:body {:piece-id int?
                                                  :knot     string?}}
                              :handler    (fn [{{{:keys [piece-id knot]} :body} :parameters}]
                                            (link-service/create-link!
                                              config/piece-repository
                                              config/link-repository
                                              piece-id
                                              knot)
                                            {:status 200 :body {}})}}]
       ["/knot-links/:id" {:delete {:parameters {:path {:id int?}}
                                    :handler    (fn [{{{:keys [id]} :path} :parameters}]
                                                  (link-service/delete-link! config/link-repository id)
                                                  {:status 200 :body {}})}}]
       ["/logout" {:post {:handler (fn [_]
                                     {:status  200
                                      :cookies {"token" {:value "" :max-age 0 :path "/"}
                                                "login" {:value "" :max-age 0 :path "/"}}})}}]

       ["/knots/search" {:get {:parameters {:query {:prefix string?}}
                               :handler    (fn [{{{:keys [prefix]} :query} :parameters}]
                                             {:status 200 :body (piece-service/handle-knots-search! config/piece-repository prefix)})}}]
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
