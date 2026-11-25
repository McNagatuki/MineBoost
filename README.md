# MineBoost — Minecraft Forge 1.20.1

タグで採掘速度とブロックのドロップ数を変更するサーバー向けMODです。
MOD ID: `mineboost` / バージョン: `1.0.0` / ビルド対象: Forge `47.4.0` / Java 17。
クライアント側に本MODを導入しなくてもよい設計です（実接続は未検証）。
新規ポーション、アイテム、通信チャンネルは追加しません。

## ビルド

JDK 17（64bit）をインストールし、`JAVA_HOME` および PATH を設定してください。
`java -version` が 17 を示すことを確認してください。インターネット接続が必要です。

- Windows: `build.bat` をダブルクリック、または PowerShell で `powershell -ExecutionPolicy Bypass -File .\build.ps1`。
- Linux/macOS: `bash build.sh`（curl と unzip が必要）。

初回は公式 Forge MDK から Gradle Wrapper のみを取得し、依存関係をダウンロードします。
既に対応する Gradle 8.x を使用できる場合は `gradle build` でもビルド可能です。
成功時の生成物: `build/libs/mineboost-forge-1.20.1-1.0.0.jar`。
この JAR を Forge 1.20.1 サーバーの `mods` に入れ、起動してください。
ZIP ファイル自体は mods に入れないでください。

## 使い方

初期設定は採掘速度 2.0 倍、ブロックドロップ 2.0 倍です。タグを持っていないプレイヤーには適用されません。
以下のコマンドには OP レベル 2 以上が必要です。サーバーコンソールでは先頭の `/` を省きます。

```mcfunction
/tag @p add XXX_speed_up
/tag @p add XXX_drop_up
/mineboost speed multiplier 5
/mineboost drop multiplier 3
/mineboost status
```

解除:

```mcfunction
/tag @p remove XXX_speed_up
/tag @p remove XXX_drop_up
```

タグ名の変更（完全一致、大小文字区別。元のタグを自動で付け替えることはありません）:

```mcfunction
/mineboost speed tag mining_boost
/mineboost drop tag loot_boost
/tag @p add mining_boost
/tag @p add loot_boost
```

`:` を含むタグなどは引用符で囲んでください。倍率は 1〜100、小数可、1 で無効化。
設定は `config/mineboost.toml` に保存し、再起動しても維持されます。
タグを持つプレイヤー全員に同じ倍率が適用されます。タグの追加・削除・変更は即時判定します。

## 仕様と注意点

- 採掘速度は通常の計算値（道具、効率強化、採掘速度上昇、疲労などを含む）に倍率をかけます。サーバー側の採掘進捗が完了した時点で、通常の破壊ルートを呼び出します。
- クライアントの破壊予測、ヒビのアニメーション、次のブロックへ移るタイミングはバニラのままなので、実測時間は tick、通信遅延、操作に左右されます。極端な倍率で特に違いが出ます。
- ドロップはブロックの loot table で計算された結果を増やします（幸運、シルクタッチ適用後）。元が0個なら0個。小数は端数確率で丸めます（1個×2.5なら2個または3個）。スタック上限に合わせて分割します。
- MOB、チェストの戦利品、釣り、経験値、手動で捨てるアイテム、コンテナから別処理で飛び出す中身は対象外。ブロック loot context を生成するコマンドや他MODの機能は対象になる可能性があります。
- アイテムに BlockEntityTag がある場合は複製しません（中身入りシュルカーボックス等）。一般的な建材も対象なので、設置→破壊で増殖が可能です。経済サーバーではタグの権限管理をしてください。
- 採掘速度とドロップの倍率は独立しています。クリエイティブ/スペクテイターには適用しません。
- 独自のステータス効果アイコンや `/effect` 対応はありません。サーバーのみで管理するパッシブ効果です。
- Forge の BreakEvent、採掘制限、到達距離等を尊重する設計です。他MODとの順序・互換性は未検証です。

## 動作確認チェックリスト

この配布物はソースコードです。作成環境では Forge Maven に接続できず、Gradle/Java 17 もなかったため、コンパイルおよび実サーバーテストは未実施です。
以下をテスト用ワールドで確認してから本番導入してください。

1. Forge 47.4.0 で起動し、MODなしのクライアント（サーバーの他MOD要件を満たすもの）から接続する。
2. サバイバルでタグなし、速度のみ、ドロップのみ、両方の4条件を比較する。
3. 通常のツルハシで石、鉱石、黒曜石、素手で破壊不可のものを試す。
4. 幸運・シルクタッチ、倍率2.5、64個超、BlockEntityTag 付きアイテムを試す。
5. 採掘中に操作中断・ターゲット変更・距離超過・タグ削除を試す。保護範囲では破壊できないことを確認する。
6. 死亡・ログアウト・ワールド移動後、再起動後に設定とタグ判定を確認する。
7. 他MODがある場合は採掘・保護・loot modifier の互換性を確認する。

## 参考

- Forge docs: https://docs.minecraftforge.net/en/1.20.1/concepts/sides/
- Forge docs: https://docs.minecraftforge.net/en/1.20.x/advanced/accesstransformers/
- Forge downloads: https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
- 1.20.1 mappings: https://mappings.dev/1.20.1/net/minecraft/server/level/ServerPlayerGameMode.html
