(ns ring-gatekeeper.cache.memcached
  (:refer-clojure :exclude [get set])
  (:require [clojurewerkz.spyglass.client :as mem-client]
            [ring-gatekeeper.cache.core :as core]))

(def ^:private default-key-prefix "tokeninfo-")
(def ^:private default-expire-sec 300)

(defn- build-key [prefix key]
  (str prefix key))

(defn- get-prefix [opts]
  (clojure.core/get opts :key-prefix default-key-prefix))

(defn- get-expire-sec [opts]
  (clojure.core/get opts :expire-sec default-expire-sec))

(deftype Memcached [client opts]
  core/Cache

  (get [this key]
    (mem-client/get client
                    (build-key (get-prefix opts) key)))

  (set [this key value]
    (mem-client/set client
                    (build-key (get-prefix opts) key)
                    (get-expire-sec opts)
                    value)))

(defn new-cache
  ([client] (new-cache client {}))
  ([client opts] (Memcached. client opts)))
