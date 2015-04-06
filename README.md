# Seeker

Seeker is a test program written in clojure. It provides a search into a wikimedia dump.

The target file is in XML format. It is intermittently hosted in dumps.wikimedia.org website.
Seeker, after a check for his existence, prudently tries to store it locally.
After this step, it trasforms the whole file in a ordered JSON.
Finally, gives to the user the opportunity to perform an interactive search for a specific voice.
The correct match is printed on screen and stored locally in another JSON file.

## Usage

Clone the project folder locally, cd into it and just launch "lein run"
from a terminal. After that, simply follow the instruction on screen.

### Example of result json format

{
  "q":"Holmsund",
  "results":[
    {
      "title":"Wikipedia: IFK Holmsund",
      "url":"http:\/\/en.wikipedia.org\/wiki\/IFK_Holmsund",
      "abstract":"Idrottsf\u00f6reningen Kamraterna Holmsund
was a Swedish football team from Holmsund, V\u00e4sterbotten
County founded in 1923http:\/\/www.bolletinen."
    }
  ]
}
```

### Bugs

...

### Wikimedia dumps locations

The dump is located in a subfolder of http://dumps.wikimedia.org/enwiki:

http://dumps.wikimedia.org/enwiki/latest/enwiki-­‐latest-­‐abstract23.xml

### Notes

The code was written in Emacs, with the inf-clojure package on. This just
to avoid unnecessary error messages from the middleware (CIDER).

It was builded up from both test-driven approach, with the very
human readable framework expectations (http://jayfields.com/expectations/),
and with the acceleration of leiningen repl, with use of (require 'seeker.core :reload-all).

The system is very simple and fits just a simple one-word search.
