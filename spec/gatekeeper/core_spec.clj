(ns gatekeeper.core-spec
  (:require [speclj.core :refer :all]
            [gatekeeper.core :refer :all]
            [gatekeeper.authenticators.core :as auth]))

(defn proxy-request [request]
  200)

(defrecord DumbAuthenticator [can-handle is-authenticated]
  auth/Authenticator

  (handle-request? [this request]
    (:can-handle this))
  (authenticated? [this request]
    (:is-authenticated this)))

(def-gatekeeper rejector {:authenticators [(DumbAuthenticator. true false)]
                           :proxy-fn proxy-request})
(def-gatekeeper invisible {:authenticators [(DumbAuthenticator. false false)]
                            :proxy-fn proxy-request})
(def-gatekeeper happy-path {:authenticators [(DumbAuthenticator. false false)
                                              (DumbAuthenticator. true true)]
                             :proxy-fn proxy-request})

(describe "gatekeeper.core"
  (describe "def-gatekeeper"
    (it "is forbidden when no authenticators match"
      (should= 401
               (:status (invisible {}))))

    (it "is forbidden when not authenticated"
      (should= 401
               (:status (rejector {}))))

    (it "passes the request to the proxy function"
      (should= 200
               (happy-path {})))))
