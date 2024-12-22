(ns knotlog.clj.auth
  (:require
    [environ.core :refer [env]]
    [reitit.coercion.spec]
    [buddy.auth :refer [authenticated?]]
    [buddy.auth.backends.token :refer [jws-backend]]
    [buddy.auth.middleware :refer [wrap-authentication]]))



(def auth-secret (env :auth-secret))
(def auth-data {:admin (env :auth-password)})
(def auth-domain (env :auth-domain))

(defn wrap-role-authorization [handler roles]
  (fn [request]
    (let [user (:identity request)]
      (if (and (authenticated? request)
               (contains? (set roles) (keyword (:user user))))
        (handler request)
        {:status 403 :body {:message "Forbidden"}}))))


(defn wrap-jwt-cookie-auth [handler]
  (fn [request]
    (let [token (get-in request [:cookies "token" :value])]
      (if token
        (let [updated-request (assoc-in request [:headers "authorization"] (str "Token " token))]
          (handler updated-request))
        (handler request)))))

(def auth-backend
  (jws-backend {:secret auth-secret :options {:alg :hs512}}))

(defn wrap-jwt-authentication [handler]
  (wrap-authentication handler auth-backend))