#!/usr/bin/env bash

function handle(){
    input="$1"
    mv "$APPCENTER_SOURCE_DIRECTORY/$input" "$APPCENTER_SOURCE_DIRECTORY/$input.1"
    sed 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' "$APPCENTER_SOURCE_DIRECTORY/$input.1" > "$APPCENTER_SOURCE_DIRECTORY/$input"
}

handle "build.gradle.kts"
handle "app/build.gradle.kts"
handle "buildSrc/build.gradle.kts"
handle "settings.gradle.kts"