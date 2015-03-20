(ns ring-gatekeeper.cache.memcached
  (:require [clojurewerkz.spyglass.client :as memcached-client]
            [ring-gatekeeper.cache.core :as core]))

(deftype Memcached [client]
  core/Cache

  (get [this key]
    (memcached-client/get client key))

  (set [this key expire value]
    (memcached-client/set client key expire value)))

(defn new-cache [client]
  (Memcached. client))
