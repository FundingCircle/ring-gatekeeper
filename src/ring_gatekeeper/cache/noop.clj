(ns ring-gatekeeper.cache.noop
  (:require [ring-gatekeeper.cache.core :as core]))

(deftype Noop []
  core/Cache

  (get [this key]
    nil)

  (set [this key expire value]
    nil))

(defn new-cache []
  (Noop.))
