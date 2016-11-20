# SiteGen

Yet another simple static site generator... that works.

## Install

To build sitegen from source, you can run the following commands in your terminal:

```bash
git clone https://github.com/aecio/sitegen.git
cd sitegen
./gradlew clean installApp
```

which will generate an installation package under sitegen/build/install/. You can then make SiteGen command line available in the terminal by adding it to the PATH:

```bash
export SITEGEN_HOME="{path-to-cloned-sitegen-repository}/build/install/sitegen"
export PATH="$SITEGEN_HOME/bin:$PATH"
```

## Usage

Files should be created using the following convention:

```c
.
└── src
│   ├── pages  // create your pages here
│   │   ├── index.html
│   │   ├── page1.html
│   ├── static // any static assets goes here
│   │   ├.. any-file.pdf
│   │   ├── css
│   │   │   └── style.css
│   │   ├── img
│   │   │   └── logo.jpg
│   └── templates  // templates for your pages goes here
│       └── mytemplate.mustache
└── output // Website will be generated here.
    │.. index.html
    │.. page1.html
    └── static // Static folder will be copied here
```

Then, run `sitegen` on the root directory of your project. `sitegen` is going to compile the web site and run a web server. Navigate to  [http://localhost:8080](http://localhost:8080) to view the result. If you change any file while the web server
is running, then the changes will be detected automatically and the web site
will recompiled.

## Templates

Currently, the following template engines are supported:
- Mustache
- Markdown
