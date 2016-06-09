# SiteGen

Yet another simple static site generator... that works.

## Install

To build ache from source, you can run the following commands in your terminal:

```bash
git clone https://github.com/aecio/sitegen.git
cd ache
./gradlew clean installApp
```

which will generate an installation package under sitegen/build/install/. You can then make SiteGen command line available in the terminal by adding it to the PATH:

```bash
export SITEGEN_HOME="{path-to-cloned-sitegen-repository}/build/install/sitegen"
export PATH="$SITEGEN_HOME/bin:$PATH"
```

## Using

Create files using the following convention:

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
