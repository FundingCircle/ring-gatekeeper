(ns ring-gatekeeper.core)

(def find-first (comp first filter))

(defn authenticate [handler authenticators & [opts]]
  (fn [request]
    (if-let [authenticator (find-first #(.handle-request? % request) authenticators)]
      (if-let [user-info (.authenticate authenticator request)]
        (handler (assoc-in request
                           [:headers "x-user"]
                           user-info))
        (handler request))
      (handler request))))
