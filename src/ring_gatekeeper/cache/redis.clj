(ns ring-gatekeeper.cache.redis
  (:refer-clojure :exclude [get set])
  (:require [ring-gatekeeper.cache.core :as core]
            [taoensso.carmine :as car]))

(def ^:private default-prefix "userinfo:")
(def ^:private default-expire-sec 300)

(defn- get-key-prefix [opts]
  (clojure.core/get opts :key-prefix default-prefix))

(defn- get-expire-sec [opts]
  (clojure.core/get opts :expire-sec default-expire-sec))

(defn- build-key [opts key]
  (str (get-key-prefix opts) key))

(deftype Redis [server-conn opts]
  core/Cache

  (get [this key]
    (car/wcar server-conn
              (car/get (build-key opts key))))

  (set [this key value]
    (car/wcar server-conn
              (car/set (build-key opts key)
                       value
                       :ex
                       (get-expire-sec opts)))))

(defn new-cache
  ([server-conn] (new-cache server-conn {}))
  ([server-conn opts] (Redis. server-conn opts)))
