(ns terminal-render.awt
  (:import [javax.imageio ImageIO]
           [java.awt.image BufferedImage LookupOp ShortLookupTable]
           [java.awt Graphics]))

;(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn load-glyphs
  "Load raster glyphs from a file.
  You probably won't need to use this."
  [name]
  (let [image (ImageIO/read (clojure.java.io/resource name))
        per-row 16
        w (/ (.getWidth image) per-row)
        h (/ (.getHeight image) per-row)
        images (for [index (range 256)]
                 (let [x (* (int (mod index per-row)) w)
                       y (* (int (/ index per-row)) h)
                       i (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
                       g (.getGraphics i)]
                   (.drawImage g image 0 0 w h x y (+ x w) (+ y h) nil)
                   i))]
    {:glyphs (vec images)
     :char-width w
     :char-height h}))

(defn set-colors
  "Given a glyph, create a new one where the black has been replaced by
  the bg color and everything else has been replaced by the fg color.
  You probably won't need to use this."
  [g { fgr :r fgg :g fgb :b :as foreground } { bgr :r bgg :g bgb :b :as background }]
  (let [table (make-array Short/TYPE 4 256)]
    (doseq [i (range 256)]
      (aset-short table 0 i (short (if (= 0 i) bgr fgr)))
      (aset-short table 1 i (short (if (= 0 i) bgg fgg)))
      (aset-short table 2 i (short (if (= 0 i) bgb fgb)))
      (aset-short table 3 i (short 255)))
    (.filter (LookupOp. (ShortLookupTable. 0 table) nil) g nil)))

(defn new-awt-renderer
  "Returns a new render function that takes a Graphics and tiles and renders the tiles to the Graphics object.
  width - width in characters
  height -height in characters
  font - the path and name of the font file"
  [{:keys [width height font]}]
  (let [{:keys [glyphs char-width char-height]} (load-glyphs font)
        full-width (* width char-width)
        full-height (* height char-height)
        glyph-cache-atom (atom {})
        get-glyph (fn [c fg bg]
                    (let [k (str c "-" fg "-" bg)]
                      (when-not (get @glyph-cache-atom k)
                        (swap! glyph-cache-atom assoc k (set-colors (get glyphs (int c)) fg bg)))
                      (get @glyph-cache-atom k)))
        render-cache-atom (atom {})
        #^BufferedImage offscreen-buffer (BufferedImage. full-width full-height BufferedImage/TYPE_INT_ARGB)
        #^Graphics offscreen-graphics (.getGraphics offscreen-buffer)
        default-fg {:r 192 :g 192 :b 192}
        default-bg {:r 0 :g 0 :b 0}
        empty-tile {:c 0 :fg default-fg :bg default-bg}
        empty-tiles (into {} (for [x (range width)
                                   y (range height)]
                               [[x y] empty-tile]))]
    (fn [#^Graphics graphics tiles]
      (doseq [[[x y] {:keys [c fg bg] :as tile}] (merge empty-tiles tiles)]
        (when (not= tile (get @render-cache-atom [x y]))
          (swap! render-cache-atom assoc [x y] tile)
          (.drawImage offscreen-graphics (get-glyph (int c) (or fg default-fg) (or bg default-bg)) (* x char-width) (* y char-height) nil)))
      (.drawImage graphics offscreen-buffer 0 0 full-width full-height nil))))
