<!--docs:
title: "Building From Source"
layout: landing
section: docs
path: /docs/building-from-source/
-->

# Building From the Latest Source

If you'll be contributing to the library, or need a version newer than what has
been released, Material Elements from ZeoFlow can also be built from source.
To do so:

Clone the repository:

```sh
git clone https://github.com/zeoflow/material-elements.git
```

Then, build the library's AARs using Gradle:

```sh
./gradlew publish -PmavenRepoUrl="file://localhost/<path_to_aars>"
```

This will output AARs and Maven artifacts for each of the library's modules
to the path on your machine, e.g., `$HOME/Desktop/material_aars`.

To use the AARs in your app locally, copy the output from your AAR directory
into your local Maven repository (`~/.m2/repository`). Then add `mavenLocal()`
as a repository in your project's top-level `build.gradle` file. Finally, add
the Design Library dependency as you would normally, using the version
specified as `mdcLibraryVersion` in the library's top-level `build.gradle`
file.

Note: Do not update Gradle beyond 4.10 as the
[android-maven-gradle-plugin](https://github.com/dcendents/android-maven-gradle-plugin)
dependency cannot be used for Gradle 5.x.
