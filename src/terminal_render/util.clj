(ns terminal-render.util)

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn blank-terminal
  "A convenience function for working with terminal inputs."
  []
  {})

(defn add-char
  "A convenience function for working with terminal inputs.
  Adds a character (char or int) to a terminal at a specific point.
  If fg or bg is nil, the existing fg or bg is used."
  [m c x y fg bg]
  (assoc m [x y] {:c c
                  :fg (or fg (get-in m [[x y] :fg]))
                  :bg (or bg (get-in m [[x y] :bg]))}))

(defn add-string
  "A convenience function for working with terminal inputs.
  Adds a string to a terminal starting at a specific point.
  If fg or bg is nil, the existing fg or bg is used."
  [m s x y fg bg]
  (if (empty? s)
    m
    (add-string (add-char m (first s) x y fg bg) (rest s) (inc x) y fg bg)))
