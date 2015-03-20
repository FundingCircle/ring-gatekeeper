(ns gatekeeper.cache.core)

(defprotocol Cache
  "Caches values"
  (get [this key]
       "Retrieves the cached value for the given key")
  (set [this key expire value]
       "Sets the cached value for the given key")
  (get-all [this keys]
           "Retrieves all of the values for the given keys")
  (inc-or-set [this key inc-amount expire value]
              "Increment the counter if it exists, otherwise create it"))
