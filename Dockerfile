# ベースイメージ（軽量なOpenJDK）
FROM eclipse-temurin:17-jdk-alpine

# 作業ディレクトリを設定
RUN mkdir -p /app
WORKDIR /app

# 必要なパッケージをインストール（maven）
RUN apk add --no-cache maven

# 依存関係を先にダウンロードしてキャッシュを利用
COPY pom.xml /app
RUN mvn dependency:go-offline

# プロジェクトのソースコードをコピー
COPY src /app/src

# Maven のビルド（キャッシュを活用）
RUN mvn clean install

# ホットリロードを有効にする環境変数を設定
ENV JAVA_OPTS="-Dspring.devtools.restart.enabled=true -Dspring.devtools.livereload.enabled=true"

# アプリケーションを実行
CMD ["sh", "-c", "mvn spring-boot:run -Dspring-boot.run.profiles=dev $JAVA_OPTS"]
