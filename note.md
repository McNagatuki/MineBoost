# Note

## 準備作業

- リス空間を作成
- リス地にブロックを設置（羊毛と岩盤）
- リス地とビーコンにアーマースタンド設置

## ルール

- 液体禁止（水・溶岩）
- XRay
- ベッド使用

## スコア

- 赤チームスコア
  - mineboost ticket
- 青チームスコア
  - mineboost ticket
- フェーズ
  - mineboost phase
- 死亡カウント
  - deaths
- 勝利チーム
  - mineboost winner
    - 1：赤
    - 2：青
    - 0：no contest

## Phase

- Phase 0: 試合開始前
- Phase 1: 試合準備
- Phase 2: 試合中（準備フェーズ）
- Phase 3: 試合中（戦闘フェーズ）
- Phase 4: 試合終了

## 設定

- チケット：1000
- 時間
  - 準備：30分
  - 試合：90分

## 場所

- 赤チームリス
  - red_spawn
- 青チームリス
  - blue_spawn
- ビーコン
  - beacon

## コマンド

### Overview

- 初期化
  - タイマー
  - ビーコン
  - ~~ワールドボーダー~~
  - チーム作成
  - スコアボード
  - コマンド出力オフ
  - デフォルトゲームモード アドベンチャー
  - 右側にスコア表示
- 試合準備
  - （手動）青チームスコア指定
    - `scoreboard players set 青チーム ticket 1000`
  - （手動）赤チームスコア指定
    - `scoreboard players set 赤チーム ticket 1000`
  - タイマー初期化
  - ビーコン
    - 初期化
    - 設置
  - アイテムクリア・肉配布
  - ゲームモード アドベンチャー
  - 死亡カウント0
  - 勝利チーム初期化
  - Phaseを1に
- 準備フェーズ開始
  - （手動）時間設定
    - `scoreboard players set ngtk_counter ngtk_counter_m 30`
    - `scoreboard players set ngtk_counter ngtk_counter_s 0`
  - タイマー初期化
  - ビーコン
    - 初期化
    - 設置
  - 死亡カウント0
  - tp
  - リス固定
  - アイテム配布
    - 肉 64個
    - 鉄ツルハシ（不可壊）
  - ゲームモード サバイバル
  - サウンド
  - タイトル
  - カウント開始
  - Phaseを2に
- 準備フェーズ
  - （手動）敵拠点近くに行ったら暗視と弱体化
    - `execute at @e[limit=1,name=red_spawn,type=armor_stand] run effect give @a[team=blue, distance=..100] blindness 1 1`
    - `execute at @e[limit=1,name=blue_spawn,type=armor_stand] run effect give @a[team=red, distance=..100] blindness 1 1`
    - `execute at @e[limit=1,name=red_spawn,type=armor_stand] run effect give @a[team=blue, distance=..100] weakness 1 1`
    - `execute at @e[limit=1,name=blue_spawn,type=armor_stand] run effect give @a[team=red, distance=..100] weakness 1 1`
  - （手動）時間カウント
    - `function mb:counter/countdown`
  - （手動）時間経過で戦闘フェーズ開始
    - `execute if score ngtk_counter ngtk_counter_end matches 1 run function mb:on/phase3_game`
- 戦闘フェーズ開始
  - （手動）時間設定
    - `scoreboard players set ngtk_counter ngtk_counter_m 40`
    - `scoreboard players set ngtk_counter ngtk_counter_s 0`
  - タイマー初期化
  - ビーコン
    - 初期化
    - 設置
  - 死亡カウント0
  - サウンド
  - タイトル
  - カウント開始
  - Phaseを3に
- 戦闘フェーズ
  - （手動）敵拠点近くに行ったら暗視と弱体化
    - `execute at @e[limit=1,name=red_spawn,type=armor_stand] run effect give @a[team=blue, distance=..100] blindness 1 1`
    - `execute at @e[limit=1,name=blue_spawn,type=armor_stand] run effect give @a[team=red, distance=..100] blindness 1 1`
    - `execute at @e[limit=1,name=red_spawn,type=armor_stand] run effect give @a[team=blue, distance=..100] weakness 1 1`
    - `execute at @e[limit=1,name=blue_spawn,type=armor_stand] run effect give @a[team=red, distance=..100] weakness 1 1`
  - （手動）死んだら自チームスコア-1
    - `function mb:check_death`
  - （手動）ビーコン掘ったら
    - `function mb:beacon/check`
      - 相手のスコア-1
      - サウンド
      - サブタイトル
      - ビーコン再設置
  - （手動）時間カウント
    - `function mb:counter/countdown`
  - （手動）チームスコアが0になったらPhase4に（試合終了）
    - `execute if score 赤チーム ticket matches ..0 run function mb:on/phase4_end_game`
    - `execute if score 青チーム ticket matches ..0 run function mb:on/phase4_end_game`
  - （手動）時間経過でPhaseを4に（試合終了）
    - `execute if score ngtk_counter ngtk_counter_end matches 1 run function mb:on/phase4_end_game`
- 試合終了処理
  - 勝利判定
  - サウンド
  - タイトル
  - 時間経過でPhaseを4に

- 常時起動
  - （手動）ビーコンから5マス離れた人にタグ付与
    - `execute at @e[name=beacon,type=armor_stand] run tag @a[distance=8..,team=red] add MB_speed_up`
    - `execute at @e[name=beacon,type=armor_stand] run tag @a[distance=8..,team=blue] add MB_drop_up`
