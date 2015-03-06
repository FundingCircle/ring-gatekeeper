(ns gatekeeper.request-proxy-spec
  (:require [speclj.core :refer :all]
            [gatekeeper.request-proxy :refer [proxy-request-by-server]]
            [clj-http.fake :refer [with-fake-routes-in-isolation]]))

(def hello-request
  {"hello.com/gatekeeper" {:get (fn [req]
                                  {:status 200 :body "{}"})}})

(context "auth-proxy.request-proxy"
  (describe "proxy-request-by-server"
    (context "when forwarding server is not found"
      (it "has a 417 when forwarding server not found"
        (should= 417
                 (:status (proxy-request-by-server {} {:server-name "here" :request-method :get})))))

    (context "when forwarding server is found"
      (with response
        (with-fake-routes-in-isolation hello-request
          (proxy-request-by-server {"hello" "http://hello.com"} {:server-name "hello"
                                                                 :request-method :get
                                                                 :uri "/gatekeeper"})))
      (it "forwards the response status"
        (should= 200
                 (:status @response)))

      (it "forwards the response body as an input stream"
        (should= "{}"
                 (slurp (:body @response)))))))

(run-specs)
