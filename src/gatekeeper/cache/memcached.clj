(ns gatekeeper.cache.memcached
  (:require [clojurewerkz.spyglass.client :as memcached-client]
            [gatekeeper.cache.core :as core]))

(deftype Memcached [client]
  core/Cache

  (get [this key]
    (memcached-client/get client key))

  (set [this key expire value]
    (memcached-client/set client key expire value))

  (get-all [this keys]
    (memcached-client/get-multi client keys))

  (inc-or-set [this key inc-amount expire value]
    (memcached-client/incr client key inc-amount value expire)))

(defn new-cache [client]
  (Memcached. client))
