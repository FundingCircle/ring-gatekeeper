(defproject ring-gatekeeper "0.1.3"
  :description "Authentication middleware"
  :url "https://github.com/FundingCircle/ring-gatekeeper"
  :license {:name "BSD 3-clause"
            :url "http://opensource.org/licenses/BSD-3-Clause"}
  :plugins [[speclj "3.2.0"]]
  :profiles {:dev {:dependencies [[speclj "3.2.0"]
                                  [clj-http-fake "1.0.1"]]}}
  :test-paths ["spec"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "1.1.1"]
                 [clojurewerkz/spyglass "1.1.0"]
                 [com.taoensso/carmine "2.9.2"]
                 [jerks-whistling-tunes "0.1.2"]])
