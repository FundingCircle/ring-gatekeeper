(ns gatekeeper.core
  (:require [clojure.data.json :as json]))

(def find-first (comp first filter))

(def not-found-response {:status 404
                         :body (json/write-str {:message "Not found"})})

(def not-authenticated-response
  {:status 401
   :body (json/write-str {:message "Not authenticated"})})

(defmacro def-gatekeeper [fn-sym opts]
  (let [{:keys [authenticators proxy-fn]} opts]
    `(defn ~fn-sym [request#]
       (if-let [authenticator# (find-first #(.handle-request? % request#) ~authenticators)]
         (if-let [authenticated-request# (.authenticate authenticator# request#)]
           (if-let [response# (~proxy-fn authenticated-request#)]
             response#
             not-found-response)
           not-authenticated-response)
         not-authenticated-response))))
