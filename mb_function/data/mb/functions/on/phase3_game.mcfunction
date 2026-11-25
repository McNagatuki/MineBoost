# タイマー初期化
function mb:timer/initialize
function mb:counter/initialize

# ビーコン初期化
function mb:beacon/initialize
function mb:beacon/place

# 死亡カウント初期化
scoreboard players set @a deaths 0

# サウンド・タイトル
execute as @a at @s run playsound minecraft:item.goat_horn.sound.0 master @s ~ ~ ~
title @a title "開始！"

# カウント開始
function mb:counter/start

# フェーズ進行
scoreboard players set mineboost phase 3