(ns scrambler.handler.scramble-test
  (:require [clojure.test :refer :all]
            [integrant.core :as ig]
            [rop.core :as rop]
            [ring.mock.request :as mock]
            [duct.router.bidi-testing :as bidi]
            [scrambler.handler.scramble :as scramble]))

(deftest invalid-char-test
  (testing "empty string should yield nil"
    (is (= (scramble/invalid-char "") nil)))
  (testing "a-z string should yield nil"
    (is (= (scramble/invalid-char "abcdefghijklmnopqrstuvwxyz") nil)))
  (testing "capitalized A should result in error"
    (is (= (scramble/invalid-char "aAbcdefghijklmnopqrstuvwxyz")
          65)))
  (testing "{ should result in error"
    (is (= (scramble/invalid-char "abcdefghijklmnopqrstuvwxyz{")
          123)))

  (testing "long strings"
    (is (= (scramble/char-freq "abc")
          {\a 1, \b 1, \c 1}))
    (is (= (scramble/char-freq "abbc")
          {\a 1, \b 2, \c 1}))
    (is (= (scramble/char-freq "abbcccc")
          {\a 1, \b 2, \c 4}))))

(deftest char-freq-test
  (testing "empty string"
    (is (= (scramble/char-freq "") {})))
  (testing "long strings"
    (is (= (scramble/char-freq "abc")
          {\a 1, \b 1, \c 1}))
    (is (= (scramble/char-freq "abbc")
          {\a 1, \b 2, \c 1}))
    (is (= (scramble/char-freq "abbcccc")
          {\a 1, \b 2, \c 4}))))

(deftest scramble?-test
  (testing "empty strings"
    (is (= (scramble/scramble? ["" ""]) true)))
  (testing "equal strings should return true"
    (is (= (scramble/scramble? ["abc" "abc"])
          true))
    (is (= (scramble/scramble? ["defgh" "defgh"])
          true)))
  (testing "reversed strings should return true"
    (is (= (scramble/scramble? ["abc" "cba"])
          true))
    (is (= (scramble/scramble? ["defgh" "hgfed"])
          true))))

(deftest validate-strings-test
  (testing "empty strings"
    (is (= (scramble/validate-strings [])
          (scramble/error {:first [:empty]})))
    (is (= (scramble/validate-strings [nil "abc"])
          (scramble/error {:first [:empty]}))))
  (testing "invalid characters"
    (is (= (scramble/validate-strings ["abcA" "abc"])
          (scramble/error {:first [:invalid-character 65]})))
    (is (= (scramble/validate-strings ["abc" "abc{"])
          (scramble/error {:second [:invalid-character 123]}))))
  (testing "strings too long"
    (let [long-string (apply str (repeat 41 \a))]
      (is (= (scramble/validate-strings [long-string "abc"])
            (scramble/error {:first [:too-long]})))
      (is (= (scramble/validate-strings ["abc" long-string])
            (scramble/error {:second [:too-long]}))))))

(deftest smoke-test
  (testing "scramble page exists"
    (let [wrap-bidi #(bidi/route % ["" {"/scramble" :scramble/scramble}])
          handler  (comp (ig/init-key :scrambler.handler/scramble {}) wrap-bidi)
          response (handler (-> (mock/request :get "/scramble")
                              (assoc :params {:first "abc" :second "bc"})))]
      (and (is (= "{:result true}" (:body response)) "response ok")
        (is (= 200 (:status response)) "response ok")))))
