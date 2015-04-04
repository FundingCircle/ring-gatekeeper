(ns ring-gatekeeper.cache.core
  (:refer-clojure :exclude [get set]))

(defprotocol Cache
  "Caches values"
  (get [this key]
       "Retrieves the cached value for the given key")
  (set [this key value]
       "Sets the cached value for the given key"))
