(ns ring-gatekeeper.authenticators.core)

(defprotocol Authenticator
  "Authenticates requests"
  (handle-request? [this request]
                   "Determines if the authenticator can authenticate a request")
  (authenticate [this request]
                "Returns the authenticated user, or nil if no user exists"))
