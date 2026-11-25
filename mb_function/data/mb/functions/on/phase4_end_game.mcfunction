# 勝利判定
scoreboard players set mineboost winner 0
execute if score 赤チーム ticket > 青チーム ticket run scoreboard players set mineboost winner 2
execute if score 赤チーム ticket < 青チーム ticket run scoreboard players set mineboost winner 2
execute if score 赤チーム ticket = 青チーム ticket run scoreboard players set mineboost winner 0

# アナウンス
execute if score mineboost winner matches 1 run title @a title {"text":"赤チームの勝利","color":"red"}
execute if score mineboost winner matches 2 run title @a title {"text":"青チームの勝利","color":"blue"}
execute if score mineboost winner matches 0 run title @a title "引き分け"

title @a title "ゲーム終了！"
execute as @a at @s run playsound minecraft:block.anvil.use player @a ~ ~ ~ 1 1

# フェーズ
scoreboard players set ngtk_phase phase 4
