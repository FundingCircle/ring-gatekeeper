(ns gatekeeper.authenticators.auth0
  (:require [gatekeeper.authenticators.core :as auth]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [jerks-whistling-tunes.core :as jwt]
            [taoensso.timbre :as timbre :refer [log debug info error]]))

(defn- auth0-token-info-url [subdomain]
  (str "https://" subdomain ".auth0.com/tokeninfo"))

(defn- get-auth0-user-info-response [id-token subdomain]
  (client/post (auth0-token-info-url subdomain)
              {:throw-exceptions false
               :content-type :json
               :body (json/write-str {:id_token id-token})}))

(defn- extract-token [auth-header]
  (let [[_ id-token] (re-matches #"^Bearer (.*)$" auth-header)]
    id-token))

(defrecord Auth0Authenticator [opts]
  auth/Authenticator

  (handle-request? [this request]
    ((get-in this [:opts :can-handle-request-fn]) request))

  (authenticated? [this request]
    (let [{:keys [client-id client-secret subdomain]} (:opts this)
          token (-> request
                  :headers
                  (get "authorization" "")
                  extract-token)]
      (if (jwt/valid? client-secret token :aud client-id :secret-fn jwt/decode-base-64)
        (let [response (get-auth0-user-info-response token subdomain)]
          (if (= 200 (:status response))
            (assoc-in request
                      [:headers "x-user"]
                      (:body response))
            false))
        false))))

(defn new-authenticator [opts] (Auth0Authenticator. opts))
