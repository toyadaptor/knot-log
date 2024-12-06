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
    [ring.middleware.cookies :refer [wrap-cookies]]))


(def app-router
  (ring/router
    [["/" {:get {:handler (fn [_]
                            {:status 200 :body {:res "hi"}})}}]

     ["/api"
      ["/pieces/:id" {:get {:path-params {:id int?}
                            :handler     (fn [{{:keys [id]} :path-params}]
                                           {:status 200 :body (service/handle-piece (Long/parseLong id))})}}]

      ["/pieces/:id/content" {:put {:path-params {:id int?}
                                    :body-params {:content string?}
                                    :handler     (fn [{{:keys [id]}      :path-params
                                                       {:keys [content]} :body-params}]
                                                   (service/handle-piece-update (Long/parseLong id) {:content content})
                                                   {:status 200 :body {}})}}]

      ["/pieces/:id/knot" {:post {:path-params {:id int?}
                                  :body-params {:knot string?}
                                  :handler     (fn [{{:keys [id]}   :path-params
                                                     {:keys [knot]} :body-params}]
                                                 (service/handle-piece-update (Long/parseLong id) {:knot knot})
                                                 {:status 200 :body {}})}}]

      ["/knot-link" {:post {:body-params {:knot_id  int?
                                          :piece_id int?}
                            :handler     (fn [{{:keys [knot_id piece_id]} :body-params}]
                                           (service/handle-knot-link-create (Long/parseLong knot_id) (Long/parseLong piece_id))
                                           {:status 200 :body {}})}}]

      ["/login" {:post {:body-params {:password string?}
                        :handler     (fn [{{:keys [password]} :body-params}]
                                       (let [valid? (some-> auth/auth-data
                                                            (get :admin)
                                                            (= password))]
                                         (if valid?
                                           (let [claims {:user :admin
                                                         :exp  (time/plus (time/now) (time/seconds 3600))}
                                                 token (jwt/sign claims auth/secret {:alg :hs512})]
                                             {:status  200 :body {:token token}
                                              :cookies {"token" {:value     token
                                                                 :http-only true
                                                                 :secure    true
                                                                 :path      "/"
                                                                 :same-site :lax}
                                                        "login" {:value     "1"
                                                                 :http-only false
                                                                 :secure    false
                                                                 :path      "/"
                                                                 :same-site :lax}}})

                                           {:status 400 :body {:error-text "nop!"}})))}}]

      ["/private"
       {:middleware [[auth/wrap-role-authorization [:admin]]]}
       ["/logout" {:post {:handler (fn [_]
                                     {:status  200
                                      :cookies {"token" {:value "" :max-age 0 :path "/"}
                                                "login" {:value "" :max-age 0 :path "/"}}})}}]

       ["/piece" {:post {:body-params {:content string?
                                       :knot    string?}
                         :handler     (fn [request]
                                        (service/piece-create (:body-params request)))}}]

       ["/main" {:get {:parameters {}
                       :handler    (fn [_]
                                     {:status 200
                                      :body   {"res" "main"}})}}]]]]


    {:data {:coercion   reitit.coercion.spec/coercion
            :muuntaja   muun/instance
            :middleware [wrap-cookies
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
      (wrap-cors :access-control-allow-origin [#"http://localhost:8888"]
                 :access-control-allow-credentials "true"
                 :access-control-allow-methods [:get :post :put :delete :options]
                 :access-control-allow-headers ["Content-Type" "Authorization"])))

(defn start []
  ;(jetty/run-jetty app {:port 8000, :join? false})
  (run-server app {:port 8000})
  (println "start!"))