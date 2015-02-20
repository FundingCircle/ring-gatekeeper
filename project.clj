(defproject gate_keeper "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[speclj "3.1.0"]]
  :profiles {:dev {:dependencies [[speclj "3.1.0"]
                                  [clj-http-fake "1.0.1"]]}}
  :test-paths ["spec"]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "1.0.1"]
                 [com.taoensso/timbre "3.2.1"]
                 [jerks-whistling-tunes "0.1.1"]])
