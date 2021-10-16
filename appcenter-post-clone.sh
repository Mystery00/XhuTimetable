#!/usr/bin/env bash
sed -i 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' build.gradle.kts
sed -i 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' app/build.gradle.kts
sed -i 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' buildSrc/build.gradle.kts
sed -i 's|maven("https://nexus3.mystery0.vip/repository/maven-public/")||g' settings.gradle.kts