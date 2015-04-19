(ns ring-gatekeeper.cache.redis-spec
  (:require [speclj.core :refer :all]
            [taoensso.carmine :as car]
            [ring-gatekeeper.cache.redis :as redis]
            speclj.run.standard))

(defmacro wcar* [& body] `(car/wcar nil ~@body))

(defn- clear-keys []
  (wcar* (doseq [test-key (wcar* (car/keys "test:*"))]
           (car/del test-key))))

(describe "ring-gatekeeper.cache.redis"
  (before (clear-keys))

  (describe "get"
    (with redis-cache (redis/new-cache nil {:key-prefix "test:"}))

    (context "when key exists"
      (before (wcar* (car/set "test:my-key" "my-value")))

      (it "returns the value"
        (should= "my-value"
                 (.get @redis-cache "my-key"))))

    (context "when key is missing"
      (it "returns nil"
        (should-be-nil (.get @redis-cache "my-key")))))

  (describe "set"
    (with redis-cache (redis/new-cache nil {:key-prefix "test:"}))

    (it "persists the value"
      (should= "test-set-value"
               (do
                 (.set @redis-cache "test-set" "test-set-value")
                 (wcar* (car/get "test:test-set")))))

    (it "expires the key"
      (should (> (do
                   (.set @redis-cache "test-set" "test-set-value")
                   ; Test could fail if process is paused here longer than the TTL
                   (wcar* (car/ttl "test:test-set")))
                 0)))))

(run-specs)
