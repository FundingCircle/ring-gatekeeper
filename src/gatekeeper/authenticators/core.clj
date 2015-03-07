(ns gatekeeper.authenticators.core)

(defprotocol Authenticator
  "Authenticates requests"
  (handle-request? [this request]
                   "Determines if the authenticator can authenticate a request")
  (authenticate [this request]
                "Authenticates the request and returns the modified request if successful, or false if not authenticated"))

