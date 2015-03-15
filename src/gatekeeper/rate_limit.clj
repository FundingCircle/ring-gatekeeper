(ns gatekeeper.rate-limit
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.periodic :as p]
            [clojure.data.json :as json]))

(def ^:private exceeded-limit-response
  {:status 429
   :body (json/write-str {:message "Exceeded limit"})})

(def ^:private format-time-to-mins (partial f/unparse (f/formatter "yyyy-MM-dd hh:mm")))

(defn- build-bucket-key [request-id time]
  "Builds a key to represent this request and time"
  (str "rate-limit-" request-id "-" (format-time-to-mins time)))

(defn bucket-seq [request-id]
  "Creates an infinite sequence of bucket keys in descending order"
  (let [time-seq (p/periodic-seq (t/now)
                                 (t/minutes -1))]
    (map (partial build-bucket-key request-id) time-seq)))

(defn- inc-request-count! [cache bucket-key]
  "Increments the number of requests in this bucket"
  (.inc-or-set cache bucket-key 1 3600 1))

(defn- count-requests [cache bucket-keys]
  "Counts the number of requests this user has made in the last hour"
  (let [request-keys (take 60 bucket-keys)
        request-counts (map #(java.lang.Integer/parseInt % 10) (.get-all cache request-keys))]
    (reduce + request-counts)))

(defn- exceeds-limit? [cache bucket-keys max-requests-per-hour]
  "Determines if another request would exceed the request limit"
  (let [num-requests (count-requests cache bucket-keys)]
    (>= num-requests max-requests-per-hour)))

(defn- limit-rate* [handler cache max-requests-per-hour request request-id]
  (let [bucket-keys (bucket-seq request-id)]
    (if-not (exceeds-limit? cache bucket-keys max-requests-per-hour)
      (do
        (inc-request-count! cache (first bucket-keys))
        (handler request))
      exceeded-limit-response)))

(defn limit-requests-per-hour [handler cache opts]
  (let [{:keys [limit identifier-fn]} opts]
    (fn [request]
      (let [request-id (identifier-fn request)]
        (limit-rate* handler
                     cache
                     limit
                     request
                     request-id)))))
