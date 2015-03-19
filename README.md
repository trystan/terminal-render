# terminal-render

Render code page 437 terminal stuff to a Swing Graphics, an ANSI terminal, javascript (TODO).

## Instalation

In Leiningen:

````clojure
    [terminal-render "0.2.0"]
````

## Usage

Call new-awt-renderer or new-ansi-renderer to get a render function:

````clojure
(def render-terminal (new-ansi-renderer {:width 80 :height 20}))
````

Then send a map of [x y] to { :c :fg :bg } to render it.

````clojure
(render-terminal { [1 1] { :c 249 :fg { :r 250 :g 250 :b 250 } :bg { :r 0 :g 0 :b 0 }
                   [7 4] { :c \@  :fg { :r 250 :g 250 :b 250 } :bg { :r 0 :g 0 :b 0 } })
````

blank-terminal, add-char, and add-string can make things easier.

````clojure
(render-terminal (-> (blank-terminal)
                     (add-string "Testing" 2 2 { :r 250 :g 250 :b 250 } nil)
                     (add-char \@ 9 8 { :r 250 :g 250 :b 250 } nil)))
````
## License

Copyright © 2015 Trystan

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
