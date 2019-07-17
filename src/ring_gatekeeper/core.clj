(ns ring-gatekeeper.core)

(def find-first (comp first filter))

(defn- dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn- strip-user [request]
  (dissoc-in request [:headers "x-user"]))

(defn authenticate [handler authenticators & [opts]]
  (fn [request]
    (if-let [authenticator (find-first #(.handle-request? % request) authenticators)]
      (if-let [user-info (.authenticate authenticator request)]
        (do
          (prn "user-info: " user-info)
          (handler (assoc-in request
                             [:headers "x-user"]
                             user-info)))
        (do
          (prn "#### failed 1")
          (handler (strip-user request))))
      
      (do 
        (prn "### failed 2")
        (handler (strip-user request))))))
