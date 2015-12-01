(ns ring-gatekeeper.authenticators.auth0
  (:require [ring-gatekeeper.authenticators.core :as auth]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [ring-gatekeeper.cache.noop :as noop-cache]
            [jerks-whistling-tunes.core :as jwt]
            [jerks-whistling-tunes.sign :refer [hs256]]
            [jerks-whistling-tunes.utils :refer [decode-base-64]]))

(defn- auth0-token-info-url [subdomain]
  (str "https://" subdomain ".auth0.com/tokeninfo"))

; Key must be unique per user per application
; aud: Auth0 client id
; sub: User id
(defn- build-cache-key [{:keys [sub aud]}]
  (str aud "-" sub))

(defn- get-auth0-user-info-response [cache id-token subdomain claims]
  (let [response (client/post (auth0-token-info-url subdomain)
                              {:throw-exceptions false
                               :content-type :json
                               :body (json/write-str {:id_token id-token})})
        {:keys [body status]} response]
    (if (= 200 status)
      (do
        (.set cache
              (build-cache-key claims)
              body)
        body)
      nil)))

(defn- get-cached-user-info-response [cache claims]
  (.get cache (build-cache-key claims)))

(defn- extract-token-from-auth-header [auth-header]
  (let [[_ id-token] (re-matches #"^Bearer (.*)$" auth-header)]
    id-token))

(defn- extract-token [request]
  (let [auth-header (get-in request [:headers "authorization"] "")]
    (or (extract-token-from-auth-header auth-header)
        (get-in request [:params "id-token"]))))

(def default-cache (noop-cache/new-cache))

(defrecord Auth0Authenticator [client-secret opts]
  auth/Authenticator

  (handle-request? [this request]
    ((:can-handle-request-fn opts) request))

  (authenticate [this request]
    (let [{:keys [client-id subdomain]} opts
          cache (get opts :cache default-cache)
          token (extract-token request)]
      (if-let [claims (jwt/validate token
                                    (jwt/signature (hs256 client-secret))
                                    (jwt/aud client-id)
                                    jwt/exp)]
        (or (get-cached-user-info-response cache claims)
            (get-auth0-user-info-response cache token subdomain claims))
        nil))))

(defn new-authenticator [opts]
  (Auth0Authenticator. (decode-base-64 (:client-secret opts)) opts))
