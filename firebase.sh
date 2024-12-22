#!/bin/zsh

lein with-profile prd pprint :env > .lein-env
lein shadow release app
firebase deploy