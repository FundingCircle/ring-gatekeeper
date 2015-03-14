(ns gatekeeper.authenticators.auth0-spec
  (:require [clj-http.client] ; Must be loaded before clj-http.fake
            [clj-http.fake :refer [with-fake-routes-in-isolation]]
            [gatekeeper.authenticators.auth0 :refer :all]
            [gatekeeper.cache.core :as cache]
            [jerks-whistling-tunes.core :as jwt]
            [speclj.core :refer :all]))

(def failed-token-info {"https://subdomain.auth0.com/tokeninfo" {:post (fn [req] {:status 401 :body "{}"})}})
(def successful-token-info {"https://subdomain.auth0.com/tokeninfo" {:post (fn [req] {:status 200 :body "success"})}})

(deftype ConstantCache [result]
  cache/Cache

  (get [this key]
    result)

  (set [this key expire value]
    nil))

(context "gatekeeper.authenticators.auth0"
  (describe "handle-request?"
    (with truthy-authenticator (new-authenticator {:can-handle-request-fn (constantly true)}))
    (with falsy-authenticator (new-authenticator {:can-handle-request-fn (constantly false)}))

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
                                                   :cache (ConstantCache. "success")
                                                   :subdomain "subdomain"}))

    (around [it]
      (with-redefs [jwt/valid? (fn [_ token & more] (= token "valid"))]
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
               (get-in (with-fake-routes-in-isolation successful-token-info
                         (.authenticate @authenticator {:headers {"authorization" "Bearer valid"}}))
                       [:headers "x-user"])))

    (it "does not hit auth0 when result is cached"
      (should= "success"
               (get-in (.authenticate @cached-authenticator {:headers {"authorization" "Bearer valid"}})
                       [:headers "x-user"])))))

(run-specs)
