(ns gatekeeper.user-info-spec
  (:require [speclj.core :refer :all]
            [gatekeeper.user-info :refer :all]
            speclj.run.standard))

(describe "gatekeeper.user-info"
  (describe "assoc-user-info"
    (it "parses json x-user header"
      (should= {:cool true}
               (:user-info ((assoc-user-info identity) {:headers {"x-user" "{\"cool\": true}"}}))))
    (it "throws exception for invalid json x-user header"
      (should-throw
        (:user-info ((assoc-user-info identity) {:headers {"x-user" "{\"cool\": true"}}))))))

(run-specs)
