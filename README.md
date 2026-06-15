# ComboMaker

Street Fighter 6のコンボや連携を記録・管理するためのJavaデスクトップアプリです。

## 現在の機能

- コンボ/連携の登録、更新、削除
- キャラクターとキーワードでの絞り込み
- ダメージ、ドライブ消費、SAゲージ、位置、難易度、タグ、メモの管理
- ローカルファイルへの自動保存

## 起動方法

PowerShellでプロジェクトルートから実行します。

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1
```

または `scripts\run.bat` を実行してください。

データはユーザーホーム配下の `.combocounter/combos.tsv` に保存されます。

## チェック

GUIを開かずにコンパイルと保存層の簡易確認を行う場合:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1
```

## 構成

- `model`: コンボデータの定義
- `repository`: 保存先を隠蔽するデータアクセス層
- `ui`: Swingベースの画面

将来的にSQLiteやクラウド同期へ移行する場合は、`ComboRepository` の実装を追加する想定です。
