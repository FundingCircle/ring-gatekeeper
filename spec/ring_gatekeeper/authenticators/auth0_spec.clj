(ns ring-gatekeeper.authenticators.auth0-spec
  (:require [clj-http.client] ; Must be loaded before clj-http.fake
            [clj-http.fake :refer [with-fake-routes-in-isolation]]
            [ring-gatekeeper.authenticators.auth0 :refer :all :as auth0]
            [ring-gatekeeper.cache.core :as cache]
            [jerks-whistling-tunes.core :as jwt]
            [speclj.core :refer :all]))

(def failed-token-info {"https://subdomain.auth0.com/tokeninfo" {:post (fn [req] {:status 401 :body "{}"})}})
(def successful-token-info {"https://subdomain.auth0.com/tokeninfo" {:post (fn [req] {:status 200 :body "success"})}})

(deftype ConstantCache [expected-key result]
  cache/Cache

  (get [this key]
    (if (= expected-key key)
      result
      (throw (Exception. (str "expected " expected-key " but received " key)))))

  (set [this key value]
    nil))

(context "ring-gatekeeper.authenticators.auth0"
  (describe "handle-request?"
    (with truthy-authenticator (new-authenticator {:can-handle-request-fn (constantly true) :client-secret "client-secret"}))
    (with falsy-authenticator (new-authenticator {:can-handle-request-fn (constantly false) :client-secret "client-secret"}))

    (it "returns truthy function result"
      (should (.handle-request? @truthy-authenticator {})))

    (it "returns falsy function result"
      (should-not (.handle-request? @falsy-authenticator {}))))

  (describe "authenticate"
    (with authenticator (new-authenticator {:can-handle-request-fn (constantly true)
                                            :client-id "client-id"
                                            :client-secret "client-secret"
                                            :subdomain "subdomain"}))

    (with cached-authenticator (new-authenticator {:can-handle-request-fn (constantly true)
                                                   :client-id "client-id"
                                                   :client-secret "client-secret"
                                                   :cache (ConstantCache. "aud-sub" "success")
                                                   :subdomain "subdomain"}))

    (around [it]
      (with-redefs [jwt/validate (fn [token & more]
                                   (if (= token "valid")
                                     {:sub "sub"
                                      :aud "aud"}
                                     false))]
        (it)))

    (it "is not authenticated without an authorization header"
      (should-not (.authenticate @authenticator {:headers {}})))

    (it "is not authenticated with a malformed authorization header"
      (should-not (.authenticate @authenticator {:headers {"authorization" "not valid"}})))

    (it "handles invalid tokens"
      (should-not (.authenticate @authenticator {:headers {"authorization" "Bearer invalid"}})))

    (it "handles auth0 error"
      (should-not
        (with-fake-routes-in-isolation failed-token-info
          (.authenticate @authenticator {:headers {"authorization" "Bearer valid"}}))))

    (it "adds the x-user header when successful"
      (should= "success"
               (with-fake-routes-in-isolation successful-token-info
                 (.authenticate @authenticator {:headers {"authorization" "Bearer valid"}}))))

    (it "does not hit auth0 when result is cached"
      (should= "success"
               (.authenticate @cached-authenticator {:headers {"authorization" "Bearer valid"}})))))

(context "#'auth0/extract-token-from-auth-header"
  (describe "extract token from auth header"
     (it "should not be valid if not ^Bearer"
         (should-not (#'auth0/extract-token-from-auth-header "not valid")))
     (it "should be valid if ^Bearer"
         (should= (#'auth0/extract-token-from-auth-header
                   "Bearer somethingvalid")
                  "somethingvalid"))))
(context "#'auth0/extract-token"
  (describe "extract token from query string"
    (it "should get token from header"
        (should= (#'auth0/extract-token {:headers {"authorization" "Bearer x"}})
                 "x"))
    (it "should default to getting token from header"
        (should= (#'auth0/extract-token {:headers {"authorization" "Bearer x"}
                                         :params {"id-token" "y"}})
                 "x"))
    (it "should get token from query string"
        (should= (#'auth0/extract-token {:params {"id-token" "y"}})
                 "y")))
  (describe "no token to extract"
    (it "should be nil" (should-not (#'auth0/extract-token {})))))

(run-specs)
