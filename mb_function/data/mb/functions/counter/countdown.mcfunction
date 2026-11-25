execute if score ngtk_counter ngtk_counter_counting matches 1 if score ngtk_counter ngtk_counter_tick matches 1 run scoreboard players set ngtk_counter ngtk_counter_counting 0
execute if score ngtk_counter ngtk_counter_counting matches 1 if score ngtk_counter ngtk_counter_tick matches 1 run scoreboard players set ngtk_counter ngtk_counter_end 1
execute if score ngtk_counter ngtk_counter_counting matches 1 if score ngtk_counter ngtk_counter_tick matches 1.. run scoreboard players remove ngtk_counter ngtk_counter_tick 1

# 表示用の分・秒（秒は切り上げ。0分0秒は残り0tickのときだけ）
scoreboard players operation ngtk_counter ngtk_counter_s = ngtk_counter ngtk_counter_tick
scoreboard players add ngtk_counter ngtk_counter_s 19
scoreboard players operation ngtk_counter ngtk_counter_s /= #20 ngtk_counter_const
scoreboard players operation ngtk_counter ngtk_counter_m = ngtk_counter ngtk_counter_s
scoreboard players operation ngtk_counter ngtk_counter_m /= #60 ngtk_counter_const
scoreboard players operation ngtk_counter ngtk_counter_s %= #60 ngtk_counter_const
