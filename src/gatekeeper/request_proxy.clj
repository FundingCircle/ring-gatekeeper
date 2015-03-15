(ns gatekeeper.request-proxy
  (:require [taoensso.timbre :as timbre :refer [log debug info error]]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(def ^:private not-found-response {:status 404
                                   :body (json/write-str {:message "Not found"})})

(defn- build-url [host path]
  (.toString (java.net.URL. (java.net.URL. host) path)))

(defn- handle-not-found [request]
  not-found-response)

(defn- proxy-request-by-server* [handler server-to-host request]
  (let [domain (:server-name request)
        host (get server-to-host domain)]
    (if host
      (select-keys (client/request {:url (build-url host (:uri request))
                                    :method (:request-method request)
                                    :body (:body request)
                                    :headers (:headers request)
                                    :query-params (:query-params request)
                                    :throw-exceptions false
                                    :as :stream})
                   [:status :body])
      (handler request))))

(defn proxy-request-by-server
  ([server-to-host]
   (proxy-request-by-server handle-not-found server-to-host))
  ([handler server-to-host]
   (partial proxy-request-by-server* handler server-to-host)))
