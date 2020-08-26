# SiteGen Change Log

## Version 0.4.0 - August 2020

- Implemented basic support to Freemaker templates
- Implemented basic command line interface
- Setup automatic source code formatting using the Google Java Style
- Replaced `nanohttpd-webserver` by Undertow server
- Created CLI commands `watch`, `build`, and `help`


## Version 0.3.0 - October 15, 2018

- Upgrade `nanohttpd-webserver` to version 2.3.1
- Print context variables in the terminal to help debugging
- Allow setting of any variable in the YML header of the pages to be later
  used in templates via the context variable `page.extra.<variable-name>`.
- Read configurations from a `sitegen.yml` file in the root of project


## Version 0.2.0 - November 21, 2016

- Added list of pages to the rendering context
- Added "hot reload" for templates and static files
- Added `published_time and `modifified_time` page properties
- Added basic markdown support
- Support permalinks without explicit filename
- Support blog post pages


## Version 0.1.0 - June 9, 2016

- Basic static pages generation
- Embeded web server for static files
- Added filesystem watcher for detecting page modification

