(ns gatekeeper.rate-limit-spec
  (:require [gatekeeper.cache.core :as cache]
            [gatekeeper.rate-limit :refer :all]
            [speclj.core :refer :all]
            speclj.run.standard))

(def counter (atom 0))

(deftype ConstantCache [result expected-num-keys]
  cache/Cache

  (get-all [this keys]
    (if (= expected-num-keys (count keys))
      result
      (throw (Exception. (str "Expected " expected-num-keys "keys, received " (count keys))))))

  (inc-or-set [this key inc-amount expire value]
    (if (re-matches #"rate-limit-1-\d{4}-\d{2}-\d{2} \d{2}:\d{2}" key)
      (swap! counter inc)
      (throw (Exception. (str "inc key " key "did not match expected format"))))))

(describe "gatekeeper.rate-limit-spec"
  (before
    (reset! counter 0))

  (describe "limit-requests-per-hour"
    (describe "when under limit"
      (with limit-fn (limit-requests-per-hour (constantly {:status 200})
                                 (ConstantCache. ["3" "2" "4"] 60)
                                 {:limit 10
                                  :identifier-fn :id}))

      (it "forwards request when under limit"
        (should= 200
                 (:status (@limit-fn {:id 1}))))

      (it "increments the request count"
        (should= 1
                 (do
                   (@limit-fn {:id 1})
                   @counter))))

    (describe "when over limit"
      (with limit-fn (limit-requests-per-hour (constantly {:status 200})
                                 (ConstantCache. ["3" "2" "4" "5"] 60)
                                 {:limit 10
                                  :identifier-fn :id}))

      (it "has too many requests"
        (should= 429
                 (:status (@limit-fn {:id 1}))))

      (it "does not increment the request count"
        (should= 0
                 (do
                   (@limit-fn {:id 1})
                   @counter))))))

(run-specs)
