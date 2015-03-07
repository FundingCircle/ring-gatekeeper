(defproject gatekeeper "0.1.0"
  :description "Authentication middleware"
  :url "https://github.com/FundingCircle/gatekeeper"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :plugins [[speclj "3.1.0"]]
  :profiles {:dev {:dependencies [[speclj "3.1.0"]
                                  [clj-http-fake "1.0.1"]]}}
  :test-paths ["spec"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "1.0.1"]
                 [com.taoensso/timbre "3.2.1"]
                 [jerks-whistling-tunes "0.1.1"]])
