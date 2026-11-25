# スコア
execute at @e[type=minecraft:armor_stand,name=beacon] unless block ~ ~ ~ minecraft:beacon as @a[team=red,distance=..8,scores={beacon_mined=1..}] run scoreboard players operation 青チーム ticket -= @s beacon_mined
execute at @e[type=minecraft:armor_stand,name=beacon] unless block ~ ~ ~ minecraft:beacon as @a[team=blue,distance=..8,scores={beacon_mined=1..}] run scoreboard players operation 赤チーム ticket -= @s beacon_mined

# チャット
execute at @e[type=minecraft:armor_stand,name=beacon] unless block ~ ~ ~ minecraft:beacon as @a[team=red,distance=..8,scores={beacon_mined=1..}] run tellraw @a {"text":"赤チームがビーコンを破壊中！","color":"red"}
execute at @e[type=minecraft:armor_stand,name=beacon] unless block ~ ~ ~ minecraft:beacon as @a[team=blue,distance=..8,scores={beacon_mined=1..}] run tellraw @a {"text":"青チームがビーコンを破壊中！","color":"blue"}

# スコアクリア
scoreboard players set @a[scores={beacon_mined=1..}] beacon_mined 0
