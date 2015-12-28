# Wro4J Gradle Plugin

Provides build-time solution for wro4j with Gradle.

Latest release: 1.7.9-Beta1

## Getting Started

You could find complete example [here](https://github.com/IlyaAI/wro4j-gradle-plugin-sample)

#### Step 1. Apply plugin
```groovy
buildscript {
    repositories {
        maven { url 'https://dl.bintray.com/ilyaai/maven' }
    }
    dependencies {
        classpath 'ro.isdc.wro4j.gradle:wro4j-gradle-plugin:1.7.9-Beta1'
    }
}
apply plugin: 'wro4j'
```

#### Step 2. Add webjar dependencies
```groovy
dependencies {
    webjars 'org.webjars:jquery:2.1.4'
    webjars 'org.webjars:bootstrap:3.3.4'
}
```

#### Step 3. Define web resources
```groovy
webResources {
    bundle ('core') {
        js 'js/**.js'
        preProcessor 'jsMin'
    }

    bundle ('libs') {
        js "webjars/jquery/2.1.4/jquery.min.js"
    }

    bundle ('theme-default') {
        css 'webjars/bootstrap/3.3.4/less/bootstrap.less'
        css 'themes/default/main.css'

        cssOverrideImport "variables.less", "../../../../themes/default/variables.less"
        preProcessor 'less4j', 'cssUrlRewriting'
    }

    assets {
        include 'themes/default/images/**'
    }
}
```
You could reference webjar's content just using `webjars/` prefix, e.g. `webjars/bootstrap/3.3.4/less/bootstrap.less`

#### Step 4. Build
```
gradlew build
```
When build finished you will get a jar with the following resources:
```
static/
    themes/
        default/
            images/
                *.png;*.gif;...
    core.js
    libs.js
    theme-default.css
```