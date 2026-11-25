# タイマー
function mb:timer/setup
function mb:counter/setup

# ビーコン
function mb:beacon/setup

# ワールドボーダ
# function sgg:area/setup

# チーム作成
team add ignore
team add red
team add blue

team modify red color red
team modify blue color blue 

# ゲーム用スコアボード
scoreboard objectives add phase dummy
scoreboard objectives add ticket dummy
scoreboard objectives add deaths deathCount
scoreboard objectives add winner dummy
scoreboard players set mineboost phase 0
scoreboard players set 赤チーム ticket 0
scoreboard players set 青チーム ticket 0
scoreboard players set @a deaths 0
scoreboard players set mineboost winner 0

scoreboard objectives modify ticket displayname "チケット"
scoreboard objectives setdisplay sidebar ticket

# 高さ制限
# function sgg:height/setup

# ゲームルール
gamerule commandBlockOutput false
defaultgamemode adventure
# gamerule keepInventory true
