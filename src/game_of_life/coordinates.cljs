(ns game-of-life.coordinates)

(defn create-coordinates [size]
  (vec (for [y (range size)]
         (vec
             (for [x (range size)]
              {:pos-y y :pos-x x :is-alive false :last-state false})))))


(defn change-alive-state [coordinates {:keys [pos-y pos-x is-alive]}]
  (update-in coordinates [pos-y pos-x] assoc :is-alive (not is-alive)))


(defn set-last-state [coordinates {:keys [pos-y pos-x is-alive last-state]}]
  (update-in coordinates [pos-y pos-x] assoc :last-state is-alive))

(defn count-alive-neighbours [coordinates pos-y pos-x]
  (let [range-y (range (dec pos-y) (+ 2 pos-y))
        range-x (range (dec pos-x) (+ 2 pos-x))
        neighbours (:is-alive (get-in coordinates [(dec pos-y) pos-x]))]
    (->>
      (for [y range-y x range-x]
        (when-not (and (= pos-y y)
                       (= pos-x x))
          (->>
            (get-in coordinates [y x])
            (:is-alive))))
      (filter true?)
      (count))))



(defn set-alive-status [coordinates {:keys [pos-y pos-x is-alive]}]
  (let [alive-neighbours (count-alive-neighbours coordinates pos-y pos-x)]
    (if (and is-alive (< 1 alive-neighbours) (> 4 alive-neighbours))
     nil
      (if (and (= 3 alive-neighbours) (not is-alive))
        {:pos-y pos-y :pos-x pos-x :is-alive true}
        (when is-alive
          {:pos-y pos-y :pos-x pos-x :is-alive false})))))


(defn state-changes [coordinates]
  (->>
   (for [row coordinates]
     (for [coord row]
       (set-alive-status coordinates coord)))
   (flatten)
   (remove nil?)))

(defn update-state-on-all [coordinates changes]
  (loop [coords coordinates change changes]
       (if (empty? change)
         coords
         (recur (update-in coords [(:pos-y (first change)) (:pos-x (first change))]
                          assoc :is-alive (:is-alive (first change)))
                (rest change)))))


(defn update-alive-status-on-all [coordinates]
  (let [changes (state-changes coordinates)]
    (update-state-on-all coordinates changes)))

(comment
  (update-alive-status-on-all coords))


