(ns scrambler.client
  (:require [ajax.core :refer [GET POST]]
            [reagent.core :as reagent]))

(defonce app-state-atom (reagent/atom {:last-message  {}
                                       :first-string  ""
                                       :second-string ""}))

(defn error-message [[type params]]
  (case type
    :invalid-character
    (str "Invalid character: " (char params))

    :too-long
    "String is too long. String must have less than 40 characters"

    :empty
    "String is empty."

    "Unknown error"))

(defn update-value!
  [k e]
  (let [value (.. e -target -value)]
    (swap! app-state-atom assoc k value)))

(defn handler [result]
  (swap! app-state-atom assoc :last-message result))

(defn error-handler [{:keys [status response]}]
  (if (zero? status)
    (swap! app-state-atom assoc :last-message {:offline true})
    (swap! app-state-atom assoc :last-message response)))

(defn do-submit
  []
  (GET "/scramble"
    :params {:first  (:first-string @app-state-atom)
             :second (:second-string @app-state-atom)}
    :response-format :transit
    :handler handler
    :error-handler error-handler))

(defn- layout
  [{:keys [last-message first-string second-string] :as app-state}]
  (let [{:keys [error]} last-message]
    [:div.pa4.black-80
     #_[:div#debug (pr-str app-state)]
     [:div.h2.f6
      (when (contains? last-message :offline)
        [:div.red "Can't connect to server. Maybe server is down or
        maybe you should check your Internet connection."])
      (when (contains? last-message :result)
        (if (:result last-message)
          [:div.green "They match!"]
          [:div.grey "No match, too bad!"]))]
     [:form {:on-submit #(do (.preventDefault %)
                             (do-submit))}
      [:fieldset.ba.b--transparent.ph0.mh0
       [:div.mt3
        [:label.db.fw4.lh-copy.f6 {:for "first"} "First string"]
        [:input.pa2.input-reset.ba.bg-transparent.b--light-silver.w-100.measure
         {:type      "text"
          :name      "first"
          :key       "first"
          :class     (when (:first error) "b--red")
          :value     (or first-string "")
          :on-change (partial update-value! :first-string)}]
        [:div.h1.f6.red
         (when-let [e (:first error)]
           (str "Error in first string: " (error-message e)))]]
       [:div.mt3
        [:label.db.fw4.lh-copy.f6 {:for "second"} "Second string"]
        [:input.pa2.input-reset.ba.bg-transparent.b--light-silver.w-100.measure
         {:type      "text"
          :name      "second"
          :key       "second"
          :class     (when (:second error) "b--red")
          :value     (or second-string "")
          :on-change (partial update-value! :second-string)}]
        [:div.h1.f6.red
         (when-let [e (:second error)]
           (str "Error in second string: " (error-message e)))]]]
      [:button.b.ph3.pv2.button-reset.ba.bg-blue.white.dim.pointer.f6
       {:type "submit"} "Check!"]]]))

(defn- main
  []
  [layout @app-state-atom])

(defn ^:export init
  []
  (when-let [app-element (.getElementById js/document "app")]
    (reagent/render [main] app-element)))

(init)
