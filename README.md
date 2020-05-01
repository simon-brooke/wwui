# wwui

A conversational user interface to Wildwood; but more broadly, a playground for experimenting with natural language user interaction

## Installation

Download from http://example.com/FIXME.

## Usage

**WARNING**: does not even nearly work yet, and may never do so. The value of this archive is probably mainly as something to mine for algorithms and ideas, especially for those doing natural language interpreteation and generation.

**WARNING**: nothing in this is stable. Nothing should be treated as an API. Feel free to steal ideas and code, but don't depend on `wwui` as a library.

### Concept

If `wwui` is ever finished and ever works, you will invoke it with:

    $ java -jar wwui-0.1.0-standalone.jar [args]

Where `args` specifies one or more knowledge sources, and, for each source, the names and possibly strategies of agents accessing that source. This might be in the form of arguments directly on the arg list but is more likely to be in the form of a path/url to a configuration file in EDN notation. After instantiating a [Wildwood](https://github.com/simon-brooke/wildwood) engine and linking the required knowledge sources, it will open what is effectively a read-eval-print loop (REPL) in which you can type natural language questions querying the knowledge source(s).

For more information read the docs.

## License

Copyright © 2020 Simon Brooke simon@journeyman.cc

Licensed under the GNU General Public License, version 2.0 or (at your option) any later version.

### Other licenses

This code is licensed under GPL specifically because I believe that if ever completed it will be of commercial significance. If you wish to use either this project or derivative works in a commercial or closed-source project, I'm very much open to that and am very willing to assist - but I will expect a license fee, and will license it to you under a commercial license. Note that if your project is also licensed under the GPL, even if you are profiting from it, you may use this code with its existing license for free.
