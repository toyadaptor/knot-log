(ns knotlog.clj.router
  (:require
    [taoensso.timbre :as timbre]
    [knotlog.clj.auth :as auth]
    [muuntaja.core :as muun]
    [reitit.ring :as ring]
    [reitit.ring.coercion :as rrc]
    [reitit.ring.middleware.muuntaja :as rrm-muuntaja]
    [reitit.ring.middleware.parameters :as rrm-parameter]
    [reitit.coercion.spec]
    [clj-time.core :as time]
    [buddy.sign.jwt :as jwt]
    [ring.adapter.jetty :as jetty]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.middleware.cookies :refer [wrap-cookies]])
  (:import [org.eclipse.jetty.util.log Log Logger StdErrLog])

  )


(def app-router
  (ring/router
    [["/" {:get {:handler (fn [_]
                            {:status 200 :body {:res "hi"}})}}]
     ["/api"
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
                                                                 :secure    false
                                                                 :path      "/"}
                                                        "login" {:value     "1"
                                                                 :http-only false
                                                                 :secure    false
                                                                 :path      "/"}}})

                                           {:status 400 :body {:error-text "nop!"}})))}}]

      ["/private"
       {:middleware [[auth/wrap-role-authorization [:admin]]]}
       ["/logout" {:post {:handler (fn [_]
                                     {:status  200
                                      :cookies {"token" {:value "" :max-age 0 :path "/"}
                                                "login" {:value "" :max-age 0 :path "/"}}})}}]


       ["/main" {:get {:parameters {}
                       :handler    (fn [_]
                                     {:status 200
                                      :body   {"res" "main"}})}}]
       ]]]


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
      (wrap-cors :access-control-allow-origin [#".*"]
                 :access-control-allow-methods [:get :post :put :delete :options]
                 :access-control-allow-headers ["Content-Type" "Authorization"]
                 :access-control-allow-credentials "true")))

(defn configure-jetty-logging []
  (let [logger (StdErrLog.)]
    (.setDebugEnabled logger false)
    (Log/setLog logger)))
(defn start []
  (configure-jetty-logging)
  ;(configure-logging)
  (jetty/run-jetty app {:port 8000, :join? false})
  (println "start!"))