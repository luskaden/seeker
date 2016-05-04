# Seeker

*Seeker* is a Clojure exercise. It converts a XML file retrieved from `dumps.wikimedia.org` to JSON,
performs a keyword search on it and finally prints correct matches on screen, after storing them locally
in a new JSON file.

## Usage

Clone the project locally, `cd` into it and use `lein run` to launch it.
Then simply follow the instructions on screen.

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

The XML dump was originally located in: [http://dumps.wikimedia.org/enwiki/latest/enwiki‐latest‐abstract23.xml]
(http://dumps.wikimedia.org/enwiki/latest/enwiki‐latest‐abstract23.xml)
and stored locally in `../seeker/resources/` subfolder.

The JSON results would be located in `../seeker/json/`,
namely `../seeker/json/collection.json` for the whole reordered dump,
and `../seeker/json/results.json` for the correct matches.
The second one will be overwritten after new successfully searches.

### Tech notes

- Written in Emacs, with the `inf-clojure` package, to avoid unwanted error messages from the middleware (Cider);
- framework *expectations* was used [http://jayfields.com/expectations/](http://jayfields.com/expectations/);
- to speed up all the process *Leiningen repl* , using `(require 'seeker.core :reload-all)`;
- old school `printf` *breakpoints*.
