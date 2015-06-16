(ns ring-gatekeeper.cache.memory
  (:refer-clojure :exclude [get set])
  (:require [clojure.core.cache :as c]
            [ring-gatekeeper.cache.core :as core]))

(deftype Memory [ttl-cache]
  core/Cache

  (get [this key]
      (c/lookup @ttl-cache key))

  (set [this key value]
    (swap! ttl-cache c/miss key value)))

(defn new-cache [ttl-millis]
  (Memory. (atom (c/ttl-cache-factory {} :ttl ttl-millis))))
