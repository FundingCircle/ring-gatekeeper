(ns ring-gatekeeper.core-spec
  (:require [speclj.core :refer :all]
            [ring-gatekeeper.core :refer :all]
            [ring-gatekeeper.authenticators.core :as auth]
            speclj.run.standard))

(defrecord DumbAuthenticator [can-handle is-authenticated]
  auth/Authenticator

  (handle-request? [this request]
    can-handle)
  (authenticate [this request]
    (if is-authenticated
      "user"
      false)))

(defn mark-request-handled [request]
  (assoc request :handled true))

(describe "ring-gatekeeper.core"
  (describe "authenticate"
    (context "when no authenticators can handle request"
      (with auth-fn (authenticate mark-request-handled
                                  [(DumbAuthenticator. false false)]))

      (it "is passed through"
        (should (:handled (@auth-fn {}))))

      (it "has no user"
        (should-not (get-in (@auth-fn {:headers {"x-user" "unauth-user"}}) [:headers "x-user"]))))

    (context "when request is handled, but unauthorized"
      (with auth-fn (authenticate mark-request-handled
                                  [(DumbAuthenticator. false false) (DumbAuthenticator. true false)]))
      (it "is passed through"
        (should (:handled (@auth-fn {}))))

      (it "is unauthorized"
        (should-not (get-in (@auth-fn {:headers {"x-user" "unauth-user"}}) [:headers "x-user"]))))

    (context "with an authenticated request"
      (with auth-fn (authenticate mark-request-handled
                                  [(DumbAuthenticator. false false) (DumbAuthenticator. true true)]))

      (it "is successful"
        (should= "user"
                 (get-in (@auth-fn {}) [:headers "x-user"])))

      (it "is passed through"
        (should (:handled (@auth-fn {})))))))

(run-specs)
