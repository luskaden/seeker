# Seeker

*Seeker* is a test program written in Clojure. It provides a search into a wikimedia dump.

The target file is in XML format. It is intermittently hosted in `dumps.wikimedia.org` website.
After a check of its existence, *seeker* tries prudently to store it locally.
After this step, the whole file is converted in a ordered JSON.
Finally, *seeker* gives to the user the opportunity to perform an interactive search for a specific voice. Correct matches are printed on screen and stored locally in another tiny JSON file.

## Usage

Clone the project locally, `cd` into it and just launch `lein run`
from a terminal. After that, simply follow the instructions on screen.

### Example of a result in a JSON format

```
{
  "q" : "Scrum ban",
  "results" : [
  {
    "title" : "Scrum ban",
    "url" : "http://en.wikipedia.org/wiki/Scrum_ban",
    "abstract" : "Scrum ban is an agile project management methodology. Also referred to as scrumban or scrum-ban it is a mix of Scrum and Kanban project management with aspects of both methodologies put together."
  }
 ]
}
```

### Locations

The dump is originally located in: [http://dumps.wikimedia.org/enwiki/latest/enwiki‐latest‐abstract23.xml]
(http://dumps.wikimedia.org/enwiki/latest/enwiki‐latest‐abstract23.xml)

The JSON results are located in the subfolder `../seeker/json/`.

Whole reordered dump: `../seeker/json/collection.json`.

Correct match: `../seeker/json/results.json`.


### Notes

*Seeker* is written in Emacs, with the `inf-clojure` package on, just
to avoid unnecessary error messages from the middleware (cider).

It was builded up with the use of very human readable framework *expectations*
[http://jayfields.com/expectations/](http://jayfields.com/expectations/), with the acceleration of *Leiningen repl*, using `(require 'seeker.core :reload-all)`, and the use of `printf` *breakpoints*.
