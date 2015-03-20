(ns ring-gatekeeper.core
  (:require [clojure.data.json :as json]))

(def find-first (comp first filter))

(def ^:private default-unauthorized-response
  {:status 401
   :body (json/write-str {:message "Not authenticated"})})

(defn authenticate [handler authenticators & [opts]]
  (let [unauthorized-response (get opts :unauthorized-response default-unauthorized-response)]
    (fn [request]
      (if-let [authenticator (find-first #(.handle-request? % request) authenticators)]
        (if-let [authenticated-request (.authenticate authenticator request)]
          (handler authenticated-request)
          unauthorized-response)
        unauthorized-response))))
