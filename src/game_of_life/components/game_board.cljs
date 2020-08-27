(ns game-of-life.components.game-board
  (:require
      [re-com.box :refer [box h-box v-box]]
      [reagent.core :as reagent]))


(defn draw-board [coordinates update-alive-status-fn]
  [:div
    (for [row coordinates]
      ^{:key (:pos-y (first row))}
      [h-box
       :children [(for [coord row]
                    ^{:key (str (:pos-y coord) (:pos-x coord))}
                    [:div {:class (if (:is-alive coord)
                                    :rectangle-alive
                                    :rectangle-not-alive)
                           :on-click (fn [e]
                                       (update-alive-status-fn coord))}])]])])

