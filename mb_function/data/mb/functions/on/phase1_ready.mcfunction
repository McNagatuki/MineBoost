# タイマー初期化
function mb:timer/initialize
function mb:counter/initialize

# ビーコン初期化
function mb:beacon/initialize
function mb:beacon/place

# アイテム配布処理
clear @a[team=!ignore]
give @a[team=!ignore] minecraft:cooked_beef 64

# ゲームモード変更
gamemode adventure @a[team=!ignore]

# 死亡カウント初期化
scoreboard players set @a deaths 0

# 勝利チーム初期化
scoreboard players set mineboost winner 0

# フェーズ
scoreboard players set ngtk_phase phase 1
