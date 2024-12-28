#!/bin/zsh

lein with-profile prd compile
lein shadow release app
firebase deploy
