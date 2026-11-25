scoreboard players add ngtk_timer ngtk_timer_tick 1
execute if score ngtk_timer ngtk_timer_tick matches 20.. run scoreboard players add ngtk_timer ngtk_timer_s 1
execute if score ngtk_timer ngtk_timer_s matches 60.. run scoreboard players add ngtk_timer ngtk_timer_m 1
execute if score ngtk_timer ngtk_timer_tick matches 20.. run scoreboard players set ngtk_timer ngtk_timer_tick 0
execute if score ngtk_timer ngtk_timer_s matches 60.. run scoreboard players set ngtk_timer ngtk_timer_s 0