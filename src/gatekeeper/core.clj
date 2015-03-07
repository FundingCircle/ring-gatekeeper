(ns gatekeeper.core
  (:require [clojure.data.json :as json]))

(def find-first (comp first filter))

(def not-authenticated-response
  {:status 401
   :body (json/write-str {:message "Not authenticated"})})

(defmacro def-gatekeeper [fn-sym opts]
  (let [{:keys [authenticators proxy-fn]} opts]
    `(defn ~fn-sym [request#]
       (if-let [authenticator# (find-first #(.handle-request? % request#) ~authenticators)]
         (if-let [authenticated-request# (.authenticate authenticator# request#)]
           (~proxy-fn authenticated-request#)
           not-authenticated-response)
         not-authenticated-response))))
