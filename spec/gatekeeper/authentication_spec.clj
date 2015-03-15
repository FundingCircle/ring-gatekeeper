(ns gatekeeper.authentication-spec
  (:require [speclj.core :refer :all]
            [gatekeeper.authentication :refer :all]
            [gatekeeper.authenticators.core :as auth]
            speclj.run.standard))

(defrecord DumbAuthenticator [can-handle is-authenticated]
  auth/Authenticator

  (handle-request? [this request]
    can-handle)
  (authenticate [this request]
    (if is-authenticated
      (assoc request :authenticated true)
      false)))

(defn- handle-authenticated-request [request]
  (if (:authenticated request)
    {:status 200}
    (throw (Exception. "Request was not authenticated"))))

(defn handle-throw[request]
  (throw (Exception. "Should not have made it here")))

(describe "gatekeeper.authentication"
  (describe "authenticate"
    (context "when no authenticators can handle request"
      (with auth-fn (authenticate handle-throw
                                  [(DumbAuthenticator. false false)]))

      (it "is unauthorized"
        (should= 401
                 (:status (@auth-fn {})))))

    (context "when request is handled, but unauthorized"
      (with auth-fn (authenticate handle-throw
                                  [(DumbAuthenticator. false false) (DumbAuthenticator. true false)]))
      (it "is unauthorized"
        (should= 401
                 (:status (@auth-fn {})))))

    (context "with an authenticated request"
      (with auth-fn (authenticate handle-authenticated-request
                                  [(DumbAuthenticator. false false) (DumbAuthenticator. true true)]))

      (it "is successful"
        (should= 200
                 (:status (@auth-fn {})))))

    (context "with a custom unauthorized response"
      (with auth-fn (authenticate handle-throw
                                  [(DumbAuthenticator. true false)]
                                  {:unauthorized-response {:status 500}}))
      (it "is server error"
        (should= 500
                 (:status (@auth-fn {})))))))

(run-specs)
