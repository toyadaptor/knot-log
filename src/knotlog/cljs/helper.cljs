(ns knotlog.cljs.helper
  (:require [knotlog.cljs.config :refer [backend-url]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [go <!]]))

(defn is-cookie-auth []
  (let [cookies (js/document.cookie.split "; ")]
    (some (fn [cookie]
            (let [[k v] (clojure.string/split cookie #"=")]
              (when (= k "login")
                (= "1" v))))
          cookies)))

(defn update-query-path [new-query-path]
  (.pushState js/history nil "" new-query-path))

(defn delete-cookies []
  (let [cookies (js/document.cookie.split "; ")]
    (doseq [cookie cookies]
      (let [[k _] (clojure.string/split cookie #"=")]
        (set! js/document.cookie (str k "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;"))))))

(defn get-backend-url [path]
  (let [url (str backend-url "/" path)]
    (clojure.string/replace url #"(?<!:)/{2,}" "/")))

(defn api-request 
  "A wrapper function for API requests that handles 403 responses by deleting cookies.

   Parameters:
   - method: HTTP method (:get, :post, :put, :delete)
   - path: API endpoint path (without backend URL)
   - options: Request options map

   Returns:
   - A channel that will receive the response"
  [method path options]
  (go
    (let [url (get-backend-url path)
          response (<! (case method
                          :get (http/get url options)
                          :post (http/post url options)
                          :put (http/put url options)
                          :delete (http/delete url options)
                          (throw (js/Error. (str "Unsupported HTTP method: " method)))))]
      (when (= 403 (:status response))
        (js/console.error "403 Forbidden: Deleting cookies and redirecting to login page")
        (delete-cookies)
        (update-query-path "/login"))
      response)))
