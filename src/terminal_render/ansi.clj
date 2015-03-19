(ns terminal-render.ansi)

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn to-16-color
  [{:keys [r g b] :as color} is-fg]
  (if (and r g b)
    (+ (if is-fg 30 40)
       (cond
        (< (max r g b) 33) 0
        (> r (max g b)) 1
        (> g (max r b)) 2
        (< b (min r g)) 3
        (> b (max r g)) 4
        (< g (min r b)) 5
        (< r (min g b)) 6
        :else 7))
    color))

(defn to-256-color
  [{:keys [r g b] :as color}]
  (if (and r g b)
    (+ 16 (* 36 (int (/ r 42))) (* 6 (int (/ g 42))) (int (/ b 42)))
    color))

(def csi (str (char 27) \[))

(defn move-to-code [x y]
  (str csi y ";" x "H"))

(defn restore-defaults-code []
  (str csi "0m"))

(defn to-16-color-code [fg bg]
  (str csi "1;" (to-16-color fg true) ";" (to-16-color bg false) "m"))

(defn to-256-color-code [fg bg]
  (str csi "38;5;" (to-256-color fg) ";48;5;" (to-256-color bg) "m"))

(defn to-16777216-color-code [fg bg]
  (str csi "38;2;" (:r fg) ";" (:g fg) ";" (:b fg) ";48;2;" (:r bg) ";" (:g bg) ";" (:b bg) "m"))

(defn new-ansi-renderer
  "Returns a new render function that takes tiles and renders them to stdout.
  width - width in characters
  height - height in characters
  color-format - number of colors: 16 or :system, 256 or :indexed, 16777216 or :truecolor"
  [{:keys [width height color-format] :as op}]
  (let [default-fg {:r 211 :g 211 :b 211}
        default-bg {:r 0 :g 0 :b 0}
        set-colors-code (case (or color-format 256)
                          16 to-16-color-code
                          :system to-16-color-code
                          256 to-256-color-code
                          :indexed to-256-color-code
                          16777216 to-16777216-color-code
                          :truecolor to-16777216-color-code)
        empty-tile {:c \space :fg default-fg :bg default-bg}]
    (fn [tiles]
      (let [updates (loop [x 0
                           y 0
                           last-fg-bg nil
                           parts []]
                      (let [{:keys [c fg bg] :as tile} (get tiles [x y] empty-tile)
                            fg (or fg default-fg)
                            bg (or bg default-bg)]
                        (cond
                         (= x width)
                         (recur 0 (inc y) last-fg-bg (conj parts (move-to-code 1 (+ 2 y))))
                         (= y height)
                         (clojure.string/join "" parts)
                         (not= [fg bg] last-fg-bg)
                         (recur (inc x) y [fg bg] (conj parts (set-colors-code fg bg) c))
                         :else
                         (recur (inc x) y last-fg-bg (conj parts c)))))
            full-parts [(move-to-code 1 1)
                        updates
                        (restore-defaults-code)
                        (move-to-code 1 (inc height))]]
        (print (clojure.string/join "" full-parts))
        (flush)))))
