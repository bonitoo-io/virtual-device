#!/usr/bin/env bash

mvn -q exec:java -Dmain.class="io.bonitoo.qa.util.EncryptPass" -Dexec.args="$*"
