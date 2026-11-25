scoreboard players operation ngtk_counter ngtk_counter_tick = ngtk_counter ngtk_counter_m
scoreboard players operation ngtk_counter ngtk_counter_tick *= #60 ngtk_counter_const
scoreboard players operation ngtk_counter ngtk_counter_tick += ngtk_counter ngtk_counter_s
scoreboard players operation ngtk_counter ngtk_counter_tick *= #20 ngtk_counter_const

scoreboard players set ngtk_counter ngtk_counter_counting 1
scoreboard players set ngtk_counter ngtk_counter_end 0