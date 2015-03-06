(ns gatekeeper.authenticators.core)

(defprotocol Authenticator
  "Authenticates requests"
  (handle-request? [this request] "Determines if the authenticator can authenticate a request")
  (authenticated? [this request] "Determines if a request has been authenticated"))

