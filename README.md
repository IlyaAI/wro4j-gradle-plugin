# Wro4J Gradle Plugin

Provides build-time solution for wro4j with Gradle.

Latest release: 1.8.0.Beta4

## Getting Started

You could find complete example [here](https://github.com/IlyaAI/wro4j-gradle-plugin-sample)

#### Step 1. Layout your web sources

Put your web sources (html, js, css, less, etc) in the following folders:
```
src/
  main/
    webResources/
      js/
        *.js
      themes/
        default/
          images/
            *.png; *.gif; ...
          *.css; *.less; ...
      static/
        index.html; ...
```
Plugin treats all paths against `$projectDir/src/main/webResources`.
Files from `static` folder will be copied to output as is by default but for js and css it is necessary to provide bundle configuration (see step 4).

#### Step 2. Apply plugin
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'ro.isdc.wro4j.gradle:wro4j-gradle-plugin:1.8.0.Beta4'
    }
}
apply plugin: 'java'
apply plugin: 'wro4j'
```
Wro4J plugin requires Java plugin to be applied first.

#### Step 3. Add webjar dependencies
```groovy
dependencies {
    webjars 'org.webjars:jquery:2.1.4'
    webjars 'org.webjars:bootstrap:3.3.4'
}
```

#### Step 4. Define bundles
```groovy
webResources {
    bundle ('core') {
        js 'js/**/*.js'
        preProcessor 'jsMin'
    }

    bundle ('libs') {
        js 'webjars/jquery/2.1.4/jquery.min.js'
    }

    bundle ('theme-default') {
        css 'webjars/bootstrap/3.3.4/less/bootstrap.less'
        css 'themes/default/main.css'

        cssOverrideImport 'variables.less', '../../../../themes/default/variables.less'
        preProcessor 'less4j', 'cssUrlRewriting'
    }

    assets {
        include 'themes/default/images/**'
    }
}
```
You could reference webjar's content just using `webjars/` prefix, e.g. `webjars/bootstrap/3.3.4/less/bootstrap.less`.

#### Step 5. Build
```
gradlew build
```
When build finished you will get a jar with the following resources:
```
static/
  themes/
    default/
      images/
        *.png; *.gif; ...
    core.js
    libs.js
    theme-default.css
    index.html; ...
```