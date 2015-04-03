# Seeker

FIXME:
Seeker is a test program written in clojure. It search for a XML wikimedia dump,
tries to download the smaller version, and trasform it locally in a ordered JSON.
Then perform a search into the latter, printin all values from tags, using keywords
from abstract and title tags (I just use title). If a single document matches a
query returns title, url and abstract and save the single match again in a JSON
form.

## Usage

FIXME:

Just run "lein repl" and then "search word".

## Examples

(search "Sardinia")

etc...

Query example:

GET	http://my.techtest.example.com/search?q=Holmsund

It returns a JSON document like that:

```
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

http://dumps.wikimedia.org/enwiki.

http://dumps.wikimedia.org/enwiki/latest/enwiki-­‐latest-­‐abstract23.xml

### Notes

They want a test-driven development (not repl-driven).

OR/AND appropriate use of generative testing. ;CHECK for generative testing.

Solution in github with setup instruction for setup and run it beyond a "standard"
Leiningen build: (lein deps, lein run, etc).

The system has to search for a single keyword.
If a document has not title or abstract can be rejected.
If title, abstract and url is missing return emty string.
Other links and sublinks could be rejected.
