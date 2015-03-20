(ns gatekeeper.cache.noop
  (:require [gatekeeper.cache.core :as core]))

(deftype Noop []
  core/Cache

  (get [this key]
    nil)

  (set [this key expire value]
    nil)

  (get-all [this keys]
    [])

  (inc-or-set [this key amount expire default]
    0))

(defn new-cache []
  (Noop.))
