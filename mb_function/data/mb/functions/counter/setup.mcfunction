# カウントダウン用のスコアボード（0_setup などから1回だけ実行）
scoreboard objectives add ngtk_counter_tick dummy
scoreboard objectives add ngtk_counter_s dummy
scoreboard objectives add ngtk_counter_m dummy

scoreboard objectives add ngtk_counter_counting dummy
scoreboard objectives add ngtk_counter_end dummy

scoreboard objectives add ngtk_counter_const dummy
scoreboard players set #20 ngtk_counter_const 20
scoreboard players set #60 ngtk_counter_const 60
