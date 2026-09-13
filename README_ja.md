# AI Document Assistant

[English](README.md) | [日本語](README_ja.md)

AI Document Assistantは、Spring BootとSpring AIを使用して開発した、ローカル環境で動作するRAG（Retrieval-Augmented Generation / 検索拡張生成）バックエンドアプリケーションです。

ユーザーがPDFファイルをアップロードし、そのPDFの内容について自然言語で質問できます。

アップロードされたPDFからテキストを抽出・分割し、Embeddingを生成してPostgreSQL + pgvectorに保存します。質問時には関連度の高い文章をベクトル検索で取得し、Ollama上で動作するローカルLLMにコンテキストとして渡して、回答と参照情報を生成します。

本プロジェクトでは、AI機能だけではなく、文書のプライバシー、ユーザーごとのデータ分離、認証・認可などのバックエンドセキュリティも重視しています。

---

## 主な機能

- ユーザー登録・ログイン
- JWT認証
- BCryptによるパスワードハッシュ化
- PDFアップロード
- PDFファイル検証
- Apache PDFBoxによるテキスト抽出
- テキストのチャンク分割
- OllamaによるローカルEmbedding生成
- PostgreSQL + pgvectorへのベクトル保存
- ベクトル類似検索
- RAGによるPDFへの質問回答
- 回答と参照元情報の返却
- ユーザーごとの文書アクセス制御
- リクエストバリデーション
- 共通例外処理
- FlywayによるDBスキーマ管理
- Controller/APIの自動テスト

---

## システム構成

```text
Client / Postman
       |
       v
Spring Security
       |
       v
JWT Authentication Filter
       |
       v
REST Controller
       |
       v
Service
       |
       +----------------------+
       |                      |
       v                      v
PostgreSQL / JPA        PDF Processing
       |                      |
       |                      v
       |                 Text Extraction
       |                      |
       |                      v
       |                  Chunking
       |                      |
       |                      v
       |                 Embedding Model
       |                      |
       +-----------> PostgreSQL + pgvector
                              |
                              v
                       Similarity Search
                              |
                              v
                         RAG Context
                              |
                              v
                         Local LLM
                           (Ollama)
                              |
                              v
                         回答 + 出典
```

---

## RAG処理フロー

### PDFアップロード

```text
PDFアップロード
      |
      v
リクエストチェック
      |
      v
PDFファイル検証
      |
      v
Apache PDFBox
      |
      v
ページ単位でテキスト抽出
      |
      v
チャンク分割
      |
      v
EmbeddingGemma
      |
      v
PostgreSQL + pgvector
```

ベクトルデータには、文書ID、所有者ID、ページ番号、チャンク番号などのメタデータを保持します。

### 質問回答

```text
ユーザーの質問
      |
      v
JWT認証
      |
      v
文書所有者チェック
      |
      v
質問をEmbedding化
      |
      v
ベクトル類似検索
      |
      v
関連チャンク取得
      |
      v
RAG Prompt
      |
      v
Qwen3 / Ollama
      |
      v
回答 + 参照情報
```

LLMには、取得した文書コンテキストのみを利用して回答するよう指示しています。

---

## セキュリティ設計

本システムでは、LLMに文書データを渡す前にアプリケーション側で認証・認可を行います。

### 認証

JWTを利用したステートレス認証を実装しています。

```text
ログイン
   |
   v
メールアドレス・パスワード確認
   |
   v
JWT発行
   |
   v
Authorization: Bearer <token>
   |
   v
JwtAuthenticationFilter
   |
   v
Spring Security Context
```

ユーザーのパスワードはBCryptでハッシュ化して保存します。

### 文書アクセス制御

文書へのアクセスは所有者情報を利用して制御します。

```text
認証済みユーザー
       |
       v
Document ID + Owner ID
       |
       v
所有権確認
       |
       v
Vector Search
```

ベクトル検索でも、

```text
documentId
ownerId
```

の両方を利用して検索対象を制限します。

クライアントから送信された`userId`を認可判断に使用するのではなく、Spring Securityの認証情報から現在のユーザーをサーバー側で取得します。

### PDFファイル検証

アップロードされたファイルに対して、複数のチェックを行います。

- 空ファイルチェック
- MIME Typeチェック
- ファイル名チェック
- PDFシグネチャチェック
- Apache PDFBoxによるPDF解析
- ページチェック
- 最大アップロードサイズ制限

### エラー処理

予期しないサーバーエラーの詳細はサーバー側にログとして記録し、クライアントには一般化したエラーメッセージを返します。

内部スタックトレースやデータベースの詳細情報をAPIレスポンスとして意図的に公開しない設計としています。

### シークレット管理

DBパスワードやJWT署名用シークレットは、ソースコードへ直接記載しません。

```properties
spring.datasource.password=${DB_PASSWORD}
app.jwt.secret=${JWT_SECRET}
```

環境変数から取得します。

---

## 使用技術

| 分類 | 技術 |
|---|---|
| 言語 | Java 21 |
| Backend | Spring Boot |
| AI Framework | Spring AI |
| Security | Spring Security |
| 認証 | JWT |
| ORM | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Vector Store | PostgreSQL + pgvector |
| PDF処理 | Apache PDFBox |
| Embedding Model | EmbeddingGemma |
| LLM | Qwen3 1.7B |
| AI実行環境 | Ollama |
| DB Migration | Flyway |
| Build Tool | Maven |
| Test | JUnit / Spring Boot Test / MockMvc |

---

## プロジェクト構成

```text
src/main/java/dev/docmind
|
+-- chunk
|   +-- TextChunker
|
+-- config
|   +-- SecurityConfig
|
+-- controller
|   +-- AskController
|   +-- AuthController
|   +-- DocumentController
|   +-- HealthController
|   +-- SearchController
|
+-- dto
|
+-- entity
|
+-- exception
|
+-- pdf
|
+-- repository
|
+-- security
|
+-- service
|
+-- AiDocumentAssistantApplication
```

Flywayのマイグレーションファイルは以下に配置します。

```text
src/main/resources/db/migration
```

---

## データベース

PostgreSQLを通常データとベクトルデータの両方に使用しています。

主なテーブル：

```text
users
documents
vector_store
flyway_schema_history
```

`users`、`documents`などのアプリケーションスキーマはFlywayで管理します。

`vector_store`については、現在Spring AIのpgvector初期化機能を利用しています。

HibernateはDB構造を自動変更せず、Entityとの整合性を確認する設定です。

```properties
spring.jpa.hibernate.ddl-auto=validate
```

---

## 必要環境

実行前に以下を準備してください。

- Java 21
- Maven
- PostgreSQL
- PostgreSQL pgvector extension
- Ollama

使用するOllamaモデル：

```text
embeddinggemma
qwen3:1.7b
```

モデル取得：

```bash
ollama pull embeddinggemma
ollama pull qwen3:1.7b
```

---

## 環境変数

以下の環境変数が必要です。

```text
DB_PASSWORD
JWT_SECRET
```

実際の認証情報やシークレットはGitリポジトリへコミットしないでください。

---

## 設定例

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_document_db
spring.datasource.username=ai_document_user
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.embedding.model=embeddinggemma
spring.ai.ollama.chat.model=qwen3:1.7b

spring.ai.vectorstore.pgvector.dimensions=768

app.jwt.secret=${JWT_SECRET}
```

---

## アプリケーション起動

PostgreSQLとOllamaを起動した状態で実行します。

Windowsの場合：

```bash
mvnw.cmd spring-boot:run
```

または：

```bash
mvn spring-boot:run
```

デフォルトでは以下で起動します。

```text
http://localhost:8080
```

---

## API一覧

| Method | Endpoint | 内容 |
|---|---|---|
| GET | `/api/health` | ヘルスチェック |
| POST | `/api/auth/register` | ユーザー登録 |
| POST | `/api/auth/login` | ログイン・JWT取得 |
| POST | `/api/documents/upload` | PDFアップロード・処理 |
| GET | `/api/documents` | ログインユーザーの文書一覧取得 |
| POST | `/api/search` | 関連する文書チャンクの検索 |
| POST | `/api/ask` | 文書に対する質問 |

認証が必要なAPIでは以下のHeaderを使用します。

```http
Authorization: Bearer <JWT_TOKEN>
```

---

## テスト

主要なAPIフローに対する自動テストを実装しています。

対象には以下が含まれます。

- 認証
- 文書操作
- ベクトル検索
- 文書への質問回答

Mavenから実行：

```bash
mvn test
```

IntelliJ IDEAからまとめて実行することもできます。

---

## プライバシー

現在のAI処理はOllamaを利用してローカル環境で実行します。

```text
PDF
 |
 v
Local Spring Boot Application
 |
 v
Local Embedding Model
 |
 v
Local PostgreSQL / pgvector
 |
 v
Local LLM
```

ローカル運用時には、文書内容を外部のクラウドLLM APIへ送信せずにRAG処理を実行できます。

企業環境で利用する場合は、社内ネットワークやプライベートクラウド上にAIモデルを配置する構成へ発展させることも想定できます。

---

## 現在の制限事項

- PDFのみ対応
- スキャン画像のみのPDFには未対応（OCR未実装）
- チャンク分割ロジックは比較的シンプル
- 回答品質は使用するローカルLLMおよび実行環境に依存
- フロントエンドUIは未実装
- 現在はローカルPostgreSQL・Ollamaを使用
- 本番環境向けのデプロイ設定は未実装

---

## 今後の改善

今後の改善候補：

- OCR対応
- チャンク分割方式の改善
- 検索結果のReranking
- PDF以外の文書形式への対応
- フロントエンドダッシュボード
- 引用・参照元表示の改善
- テスト専用DB・プロファイル
- 本番環境向けデプロイ設定
- Monitoring / Observability
- セキュリティテストの強化

---

## 開発目的

本プロジェクトは、Spring Bootを利用したAIバックエンドアプリケーションを実際に設計・実装し、エンドツーエンドの開発を理解することを目的として作成しました。

主な学習・実装対象：

- Spring Bootアプリケーション設計
- REST API設計
- 認証・認可
- セキュアな文書処理
- PostgreSQL
- FlywayによるDB管理
- Vector Search
- RAG
- ローカルLLM連携
- 自動テスト
- アプリケーション全体の設計・開発

---

## License

現在、本プロジェクトにはライセンスを設定していません。