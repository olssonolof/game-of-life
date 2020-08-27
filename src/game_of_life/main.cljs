(ns ^:figwheel-hooks game-of-life.main
  (:require
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [game-of-life.coordinates :refer [change-alive-state create-coordinates update-alive-status-on-all]]
    [game-of-life.components.game-board :refer [draw-board]]
    [game-of-life.style :as styles]
    [goog.style]
    [re-com.box :refer [box h-box v-box]]
    [re-com.buttons :refer [button]]
    [re-com.core :refer [label gap slider title]]
    [re-frame.core :as rf]
    [reagent.core :as reagent]))

(defonce timeouts (reagent/atom {}))

(def default-db
  {
   :coordinates (create-coordinates 10)
   :is-running false
   :speed 1000})

(rf/reg-event-db
  :initialize
  (fn [_ _]
    (prn "INIT")
    default-db))

(rf/reg-event-db
  :start-stop-game
  (fn-traced [db event]
             (assoc db :is-running (not (:is-running db)))))

(rf/reg-event-db
  :change-coords-size
  (fn-traced [db [event event-data]]
             (assoc db :coordinates (create-coordinates event-data))))


(rf/reg-event-db
  :change-speed
  (fn-traced [db [event speed]]
             (assoc db :speed speed)))

(rf/reg-event-db
  :change-alive-state
  (fn-traced [db [event coord]]
             (assoc db :coordinates (change-alive-state (:coordinates db) coord))))

(rf/reg-event-db
  :change-state-all
  (fn-traced [db [event new-coords]]
             (let [t (update-alive-status-on-all (:coordinates db))]
              (assoc db :coordinates t))))

(rf/reg-fx
  :timeout
  (fn-traced [{:keys [id event time]}]
             (when-some [existing (get @timeouts id)]
               (js/clearTimeout existing)
               (swap! timeouts dissoc id))
             (when (some? event)
               (swap! timeouts assoc id
                      (js/setTimeout
                        (fn []
                          (rf/dispatch [:change-state-all])
                          (rf/dispatch [:run-game nil time]))
                        time)))))

(rf/reg-event-fx
  :run-game
  (fn-traced [cfx [_ running time]]
             {:db (assoc (:db cfx) :is-running (not running))
              :timeout {:id :message
                        :event (when (not running) [:run-game])
                        :time time}}))


(rf/reg-sub :coordinates (fn [db _] (:coordinates db)))

(rf/reg-sub :is-running (fn [db _] (:is-running db)))

(rf/reg-sub :speed (fn [db _] (:speed db)))


(defn hello
 []
 [:h1 "Game of life"])


(defn content []
  (let [slider-val (reagent/atom 10)
        speed-val (reagent/atom 1000)]
    (fn []
      (let [coords @(rf/subscribe [:coordinates])
            running @(rf/subscribe [:is-running])]

        [v-box
         :margin "10px"
         :children
         [[button
           :label "Start/Stop"
           :class "btn btn-primary"
           :on-click (fn[e]
                       (rf/dispatch [:run-game running @speed-val]))]


          [title
           :label "Size of board"
           :level :level3]
          [slider
           :model slider-val
           :min 10
           :max 150
           :step 1
           :width "300px"
           :disabled? running
           :on-change (fn [e] (reset! slider-val e)
                        (rf/dispatch [:change-coords-size @slider-val]))]

          [label :label (str "Size of grid is " @slider-val " x " @slider-val)]
          [gap :size "20px"]

          [title
           :label "Speed of life"
           :level :level3]
          [slider
           :model speed-val
           :min 100
           :max 2500
           :step 100
           :width "300px"
           :disabled? (not running)
           :on-change (fn [e] (reset! speed-val e)
                        (rf/dispatch [:change-speed @speed-val])
                        (rf/dispatch [:run-game nil @speed-val]))]

          [gap :size "20px"]
          [button
           :label "TEST"
           :class "btn btn-primary"
           :on-click (fn[e] (rf/dispatch [:change-state-all]))]
          [gap :size "20px"]
          [draw-board coords #(rf/dispatch[:change-alive-state %])]

          #_(when running
              (run-game))]]))))



(defn ui
  []
  [:div
   [v-box
    :children
    [[box
      :child [hello]]
     [box
       :child [content]]]]])





(defn render
  []
  (reagent.dom/render [ui] (js/document.getElementById "app")))


(defn generate-and-install-styles
  []
  (goog.style/installStyles (styles/generate-css)))


(defn ^:after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (generate-and-install-styles)
  (render))


(defn run
  []
  (rf/dispatch-sync [:initialize])
  (generate-and-install-styles)
  (render))

(defonce init-block (run))
