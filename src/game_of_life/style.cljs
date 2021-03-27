(ns game-of-life.style
  (:require
    [garden.core :refer [css]]))

(def styles
  [
    [:h1 {:background :white}]
    [:button {:margin :10px}]

    [:.top-label {:margin-left :10px}]

    [:.rectangle-alive
     {:width :20px
      :height :20px
      :background :green
      :margin :1px
      :border-radius :3px}]


    [:.rectangle-not-alive
     {:width :20px
      :height :20px
      :margin :1px
      :border-radius :3px
      :background :grey}]])

(defn generate-css []
  (css styles))
