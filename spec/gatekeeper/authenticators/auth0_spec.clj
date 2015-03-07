(ns gatekeeper.authenticators.auth0-spec
  (:require [speclj.core :refer :all]
            [jerks-whistling-tunes.core :as jwt]
            [gatekeeper.authenticators.auth0 :refer :all]
            [clj-http.fake :refer [with-fake-routes-in-isolation]]))

(def failed-token-info {"https://subdomain.auth0.com/tokeninfo" {:post (fn [req] {:status 401 :body "{}"})}})
(def successful-token-info {"https://subdomain.auth0.com/tokeninfo" {:post (fn [req] {:status 200 :body "success"})}})

(context "gatekeeper.authenticators.auth0"
  (describe "handle-request?"
    (with truthy-authenticator (new-authenticator {:can-handle-request-fn (fn [req] true)}))
    (with falsy-authenticator (new-authenticator {:can-handle-request-fn (fn [req] false)}))

    (it "returns truthy function result"
      (should (.handle-request? @truthy-authenticator {})))

    (it "returns falsy function result"
      (should-not (.handle-request? @falsy-authenticator {}))))

  (describe "authenticate"
    (with authenticator (new-authenticator {:can-handle-request-fn (fn [req] true)
                                            :client-id "client-id"
                                            :client-secret "client-secret"
                                            :subdomain "subdomain"}))

    (around [it]
      (with-redefs [jwt/valid? (fn [_ token & more] (= token "valid"))]
        (it)))

    (it "is not authenticated without an authorization header"
      (should-not (.authenticate @authenticator {:headers {}})))

    (it "is not authenticated without a malformed authorization header"
      (should-not (.authenticate @authenticator {:headers {"authorization" "poop butt"}})))

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
                       [:headers "x-user"])))))

(run-specs)
