(ns gate-keeper.request-proxy
  (:require [taoensso.timbre :as timbre :refer [log debug info error]]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(def not-found-response {:status 417
                         :body (json/write-str {:message "Not found"})})

(defn- build-url [host path]
  (.toString (java.net.URL. (java.net.URL. host) path)))

(defn proxy-request-by-server [server-to-host request]
  (let [domain (:server-name request)
        host (get server-to-host domain)]
    (if host
      (select-keys (client/request {:url (build-url host (:uri request))
                                    :method (:request-method request)
                                    :body (:body request)
                                    :headers (:headers request)
                                    :query-params (:query-params request)
                                    :throw-exceptions false})
                   [:status :body])
      not-found-response)))
