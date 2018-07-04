(ns scrambler.handler.index
  (:require [hiccup.page :refer [include-js include-css html5]]))

(def mount-target
  [:div#app
   [:h3 "Loading..."]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css "//unpkg.com/tachyons@4.10.0/css/tachyons.min.css")])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/main.js")]))
