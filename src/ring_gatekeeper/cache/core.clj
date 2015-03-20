(ns ring-gatekeeper.cache.core)

(defprotocol Cache
  "Caches values"
  (get [this key]
       "Retrieves the cached value for the given key")
  (set [this key expire value]
       "Sets the cached value for the given key"))
