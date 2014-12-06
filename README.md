# terminal-render

Render code page 437 terminal stuff simply. To a Swing Graphics, javascript (TODO), or an ANSI terminal (TODO).

## Instalation

In Leiningen:

[terminal-render "0.1.0"]

## Usage

Call new-renderer to get a render function:

````clojure
(def render-terminal (new-renderer 1280 720 cp437-10x10))
````

Then send a map of [x y] to { :c :fg :bg } to render it.

````clojure
(render-terminal g { [1 1] { :c 249 :fg { :r 250 :g 250 :b 250 } :bg { :r 0 :g 0 :b 0 }
                     [7 4] { :c \@  :fg { :r 250 :g 250 :b 250 } :bg { :r 0 :g 0 :b 0 } })
````

add-char and add-string can make things easier.

````clojure
(render-terminal g (-> {}
                       (add-string "Testing" 2 2 { :r 250 :g 250 :b 250 } { :r 0 :g 0 :b 0 })
                       (add-char \@ 9 8 { :r 250 :g 250 :b 250 } { :r 0 :g 0 :b 0 })))
````
## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
