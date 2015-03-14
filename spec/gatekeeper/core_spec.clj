(ns gatekeeper.core-spec
  (:require [speclj.core :refer :all]
            [gatekeeper.core :refer :all]
            [gatekeeper.authenticators.core :as auth]
            speclj.run.standard))

(defn proxy-request [request]
  (if (:authenticated request)
    200
    (throw (Exception. "Request was not authenticated"))))

(defrecord DumbAuthenticator [can-handle is-authenticated]
  auth/Authenticator

  (handle-request? [this request]
    can-handle)
  (authenticate [this request]
    (if is-authenticated
      (assoc request :authenticated true)
      false)))

(def-gatekeeper rejector {:authenticators [(DumbAuthenticator. true false)]
                           :proxy-fn proxy-request})
(def-gatekeeper custom-rejector {:authenticators [(DumbAuthenticator. true false)]
                                 :proxy-fn proxy-request
                                 :handle-not-authenticated (constantly {:status 500})})
(def-gatekeeper invisible {:authenticators [(DumbAuthenticator. false false)]
                            :proxy-fn proxy-request})
(def-gatekeeper happy-path {:authenticators [(DumbAuthenticator. false false)
                                             (DumbAuthenticator. true true)]
                            :proxy-fn proxy-request})
(def-gatekeeper unroutable {:authenticators [(DumbAuthenticator. true true)]
                            :proxy-fn (constantly nil)})
(def-gatekeeper custom-unroutable {:authenticators [(DumbAuthenticator. true true)]
                                   :proxy-fn (constantly nil)
                                   :handle-not-found (constantly {:status 501})})

(describe "gatekeeper.core"
  (describe "def-gatekeeper"
    (it "is forbidden when no authenticators match"
      (should= 401
               (:status (invisible {}))))

    (it "is forbidden when not authenticated"
      (should= 401
               (:status (rejector {}))))

    (it "is not found when request is not forwardable"
      (should= 404
               (:status (unroutable {}))))

    (it "passes the request to the proxy function"
      (should= 200
               (happy-path {})))

    (it "allows custom not found handler"
      (should= 501
               (:status (custom-unroutable {}))))

    (it "allows custom not authenticated handler"
      (should= 500
               (:status (custom-rejector {}))))))
