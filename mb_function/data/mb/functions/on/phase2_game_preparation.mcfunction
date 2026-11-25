# タイマー初期化
function mb:timer/initialize
function mb:counter/initialize

# ビーコン初期化
function mb:beacon/initialize
function mb:beacon/place

# 死亡カウント初期化
scoreboard players set @a deaths 0

# tp・リス固定
tp @a[team=red] @e[limit=1,name=red_spawn,type=armor_stand]
tp @a[team=blue] @e[limit=1,name=blue_spawn,type=armor_stand]
spawnpoint @a[team=red] ~ ~ ~
spawnpoint @a[team=blue] ~ ~ ~

# アイテム配布
clear @a[team=!ignore]
give @a[team=!ignore] minecraft:cooked_beef 64
give @a[team=!ignore] minecraft:iron_pickaxe{Unbreakable:true}

# ゲームモード
gamemode survival @a[team=!ignore]

# サウンド・タイトル
execute as @a at @s run playsound minecraft:block.anvil.place master @s ~ ~ ~
title @a title "準備フェーズ 開始！"

# カウント開始
function mb:counter/start

# フェーズ進行
scoreboard players set mineboost phase 2