(ns scrambler.handler.scramble
  (:require [compojure.core :refer :all]
            [rop.core :as rop]
            [ring.util.response :as response]
            [integrant.core :as ig]
            [clojure.java.io :as io]
            [scrambler.handler.index :as index]))

(defn too-long?
  "Checks if a string is too long"
  [s]
  (> (count s) 40))

(defn invalid-char
  "Receives a string to check. Return the first invalid character found
  in the string. Otherwise return nil."
  [s]
  (some #(and (not (<= 97 % 122)) %) (map int s)))

(defn char-freq
  "Receives a collection of characters and returns a map of characters
  found in the collection and its frequency"
  [xs]
  (reduce (fn [acc x] (update acc x (fnil inc 0)))
    {} xs))

(defn scramble?
  "Receives two strings, returns true if a portion of s1 can be
  rearranged to match s2, otherwise returns false."
  [[s1 s2]]
  (let [g1 (char-freq s1)
        g2 (char-freq s2)]
    (every? true?
      (for [[character freq-2] g2]
        (when-let [freq-1 (get g1 character)]
          (<= freq-2 (get g1 character)))))))

(defn error
  [body]
  (rop/fail {:status 400 :body (pr-str {:error body})}))

(defn ok
  [body]
  (rop/succeed {:ok (pr-str {:result body})}))

(defn validate-strings
  "Stops and returns errors if any of given strings is invalid."
  [[s1 s2]]
  (cond
    (empty? s1)
    (error {:first [:empty]})

    (empty? s2)
    (error {:second [:empty]})

    (too-long? s1)
    (error {:first [:too-long]})

    (too-long? s2)
    (error {:second [:too-long]})

    :else
    (if-let [character (invalid-char s1)]
      (error {:first [:invalid-character character]})
      (if-let [character (invalid-char s2)]
        (error {:second [:invalid-character character]})
        (rop/succeed [s1 s2])))))

(defn scramble-handler
  "Error-conscious version of scramble?"
  [xs]
  (rop/>>=* :ok
    xs
    validate-strings
    (rop/switch scramble?)
    ok))

(scramble-handler ["abc" "bc"])
(defn get-strings
  "Extracts strings to pass to scramble-handler in a Ring request map."
  [request]
  ((juxt :first :second) (:params request)))

(defn site-routes
  [options request]
  (response/content-type
    (let [handler (case (:bidi-route request)
                    :scramble/index
                    (fn [options request]
                      {:body (index/loading-page)})
                    :scramble/scramble
                    (fn [options request]
                      (scramble-handler (get-strings request)))
                    (fn [_ _] (response/not-found nil)))]
      (handler options request))
    "text/html"))

(defmethod ig/init-key :scrambler.handler/scramble [_ options]
  (partial site-routes options))
