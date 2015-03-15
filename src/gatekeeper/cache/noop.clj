(ns gatekeeper.cache.noop
  (:require [gatekeeper.cache.core :as core]))

(deftype Noop []
  core/Cache

  (get [this key]
    nil)

  (set [this key expire value]
    nil))

(defn new-cache []
  (Noop.))
