(ns gatekeeper.user-info
  (:require [clojure.data.json :as json]))

(defn assoc-user-info [handler]
  (fn [request]
    (let [user-info (json/read-str (get-in request [:headers "x-user"]) :key-fn keyword)
          updated-request (assoc request :user-info user-info)]
      (handler updated-request))))
