# Docker 部署指南

## 專案概述
這個 workspace 包含兩個 Spring Boot 微服務應用程式：
- **demo-api**: 主要 API 服務，負責向 demo-excel 請求 Excel 檔案並**使用 POI 讀取和分析 Excel**
- **demo-excel**: Excel 專用服務，專注於**使用 POI 生成 Excel 檔案**並以 byte[] 形式提供給 demo-api

## 核心工作流程 🔄
1. **demo-api** 向 **demo-excel** 的 `/excel/generate-bytes` 端點發送請求
2. **demo-excel** 使用 **Apache POI** 生成 Excel 檔案
3. **demo-excel** 將 Excel 檔案以 byte[] 形式回傳給 **demo-api**
4. **demo-api** 接收到 Excel 檔案後，使用 **Apache POI** 讀取和分析內容
5. **demo-api** 回傳完整的分析結果

## 🎯 簡化架構特色
- **單一端點設計**: 每個服務只保留最核心的功能
- **demo-api**: 僅保留 `/excel/request-and-read` 主要工作流程
- **demo-excel**: 僅保留 `/excel/generate-bytes` Excel 生成功能
- **移除冗餘**: 刪除了所有非核心的 API 端點和服務類別
- **專注本質**: 突出 "demo-api 向 demo-excel 請求 Excel 並用 POI 讀取" 的核心需求

## 核心功能
- 🔄 **簡潔微服務架構**: 專注於核心 Excel 處理工作流程
- 📊 **Excel 處理**: 使用 Apache POI 進行 Excel 檔案生成與讀取
- 🔥 **熱重載開發**: 程式碼即時修改無需重啟容器
- 🐳 **Docker 容器化**: 完整的開發和生產環境支援
- � **專注功能**: 移除冗余端點，專注核心工作流程

## 前置需求
- Docker Desktop
- Docker Compose
- 至少 4GB 可用記憶體（Maven 依賴下載需要）
- VS Code（推薦，支援熱重載開發）

## 技術棧
- **Java 17** (Eclipse Temurin)
- **Spring Boot 3.5.6**
- **Apache POI 5.2.5** (Excel 處理)
- **Spring DevTools** (熱重載)
- **Docker & Docker Compose**
- **Alpine Linux** (容器基礎)

## 建置與運行

### 方法 1: 開發模式 - 即時程式碼修改 (推薦開發時使用)

使用開發模式的 Docker Compose，支援程式碼即時修改：
```bash
docker compose -f docker-compose.dev.yml up --build
```

背景執行開發模式：
```bash
docker compose -f docker-compose.dev.yml up -d --build
```

停止開發模式：
```bash
docker compose -f docker-compose.dev.yml down
```

### 方法 2: 生產模式 - 使用 Docker Compose

同時建置並啟動兩個應用程式：
```bash
docker compose up --build
```

背景執行：
```bash
docker compose up -d --build
```

停止所有服務：
```bash
docker compose down
```

查看日誌：
```bash
# 所有服務
docker compose logs -f

# 特定服務
docker compose logs -f demo-api
docker compose logs -f demo-excel
```

### 方法 3: 個別建置 Docker Image

建置 demo-api：
```bash
cd demo-api
docker build -t demo-api:latest .
```

建置 demo-excel：
```bash
cd demo-excel
docker build -t demo-excel:latest .
```

執行容器：
```bash
# demo-api (port 18080)
docker run -d -p 18080:8080 --name demo-api demo-api:latest

# demo-excel (port 18081)
docker run -d -p 18081:8080 --name demo-excel demo-excel:latest
```

## 訪問應用程式

### 服務端點

#### Demo API 服務 (http://localhost:18080)
- `GET /` - 服務首頁
- `GET /health` - 健康檢查  
- `GET /test` - 測試端點

**核心 Excel 工作流程 API** ⭐:
- `GET /excel/request-and-read?type=sample|report` - 向 demo-excel 請求 Excel 檔案，並使用 POI 讀取分析

#### Demo Excel 服務 (http://localhost:18081)

**Excel 生成 API**:
- `GET /excel/generate-bytes?type=sample|report` - 使用 POI 產生 Excel 檔案並回傳 byte[]

### Excel 檔案類型
- **sample**: 員工資料表 (10 筆員工記錄，包含姓名、部門、薪資等)
- **report**: 財務報表 (收入、支出、利潤等統計資料)

## 開發模式特色

### 🔥 熱重載功能
開發模式支援程式碼即時修改：
1. 修改任何 `.java` 檔案
2. Spring Boot DevTools 自動偵測變化
3. 應用程式自動重啟（2-4 秒）
4. 瀏覽器重新整理即可看到變更

### 📁 Volume 掛載
```yaml
volumes:
  - ./demo-api:/app          # 本機程式碼同步到容器
  - maven-cache-api:/root/.m2 # Maven 依賴快取
```

### 🛠️ 開發工具整合
- **VS Code**: 安裝 Extension Pack for Java
- **自動編譯**: 檔案儲存後自動編譯
- **即時預覽**: 修改後立即看到效果

## Excel 功能展示

### 🎯 核心工作流程測試
```bash
# 主要工作流程：demo-api 向 demo-excel 請求 Excel，接收後用 POI 讀取分析
curl "http://localhost:18080/excel/request-and-read?type=sample"
curl "http://localhost:18080/excel/request-and-read?type=report"

# 直接從 demo-excel 取得 Excel byte[]（用於服務間通信）
curl "http://localhost:18081/excel/generate-bytes?type=sample"
curl "http://localhost:18081/excel/generate-bytes?type=report"
```

### � 測試回應範例
```bash
# 測試員工資料表工作流程
curl "http://localhost:18080/excel/request-and-read?type=sample"

# 預期回應包含：
# - 檔案大小和類型資訊
# - POI 讀取的完整 Excel 內容
# - 工作表結構分析
# - 資料行列統計
```

## 容器管理

查看運行中的容器：
```bash
docker ps
```

停止容器：
```bash
docker stop demo-api demo-excel
```

刪除容器：
```bash
docker rm demo-api demo-excel
```

查看容器日誌：
```bash
docker logs demo-api
docker logs demo-excel
```

重啟特定服務：
```bash
# 僅重啟 demo-api
docker compose -f docker-compose.dev.yml restart demo-api

# 僅重啟 demo-excel  
docker compose -f docker-compose.dev.yml restart demo-excel
```

## 架構說明

### 簡化微服務架構
```
┌─────────────────────────────┐    GET /excel/generate-bytes    ┌─────────────────────────────┐
│        demo-api             │ ──────────────────────────────► │       demo-excel            │
│     (Port 18080)            │                                │     (Port 18081)            │
│                             │ ◄────────────────────────────── │                             │
│ HomeController:             │       Excel byte[]              │ HomeController:             │
│ /excel/request-and-read     │                                │ /excel/generate-bytes       │
│                             │                                │                             │
│ 📋 RestTemplate            │                                │ 📊 ExcelService            │
│ 📖 ExcelReaderService      │                                │ 📄 POI XSSFWorkbook        │
│ 📄 POI 讀取分析            │                                │ 📝 生成 Excel 檔案         │
└─────────────────────────────┘                                └─────────────────────────────┘
```

### Excel 處理流程
1. **demo-excel** 使用 **Apache POI** 建立 Workbook
2. **demo-excel** 生成 Excel 檔案並轉為 byte[]
3. **demo-api** 接收 byte[] 資料
4. **demo-api** 使用 **Apache POI** 讀取 Excel：
   - 解析工作表 (Sheet)
   - 讀取行列資料 (Row, Cell)
   - 分析資料類型和結構
   - 提取完整內容

### 技術架構特點
- **spring-network**: Docker 橋接網路，容器間使用服務名稱通信
- **Volume 掛載**: 支援開發模式熱重載和 Maven 依賴快取
- **Port 映射**: demo-api(18080→8080), demo-excel(18081→8080)
- **簡化設計**: 移除多餘端點，專注核心 Excel 工作流程

## Docker Image 說明

### 開發環境 (Dockerfile.dev)
- **基礎映像**: `eclipse-temurin:17-jdk-alpine`
- **包含工具**: Maven (用於即時編譯)
- **執行方式**: `mvn spring-boot:run` (支援熱重載)
- **Volume 掛載**: 程式碼目錄和 Maven 快取

### 生產環境 (Dockerfile)
- **Multi-stage build**: 
  - Build stage: 使用 JDK 17 編譯應用程式
  - Runtime stage: 使用 JRE 17 Alpine (更小的 image)
- **已包含**: `.dockerignore` 排除不必要檔案
- **最佳化**: 較小的映像檔大小

### 簡化的程式碼結構

#### Demo-API 服務
```
demo-api/src/main/java/com/example/demo_api/
├── DemoApiApplication.java          # Spring Boot 主程式
├── controller/
│   └── HomeController.java         # 唯一控制器，包含核心端點
├── service/
│   └── ExcelReaderService.java     # POI Excel 讀取服務
└── config/
    └── RestTemplateConfig.java     # HTTP 客戶端設定
```

#### Demo-Excel 服務
```
demo-excel/src/main/java/com/example/demo_excel/
├── DemoExcelApplication.java       # Spring Boot 主程式
├── controller/
│   └── HomeController.java        # 唯一控制器，Excel 生成端點
└── service/
    └── ExcelService.java          # POI Excel 生成服務
```

### 依賴管理
```xml
<!-- Apache POI for Excel processing -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.2.5</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.5</version>
</dependency>
```

### 簡化的 POI 工作流程
- **demo-excel 服務**: 
  - 使用 `ExcelService` 和 `XSSFWorkbook` 創建 Excel 檔案
  - 支援兩種類型：sample (員工資料) 和 report (財務報表)
  - 將 Excel 轉為 byte[] 格式回傳

- **demo-api 服務**:
  - `HomeController` 的 `/excel/request-and-read` 端點
  - 使用 `RestTemplate` 向 demo-excel 請求 Excel 檔案
  - `ExcelReaderService` 接收 byte[] 並用 POI 讀取分析
  - 提供完整的 JSON 格式分析結果

## 疑難排解

### Port 衝突
```bash
# 檢查 port 使用情況 (Linux/macOS)
netstat -tulpn | grep :18080
netstat -tulpn | grep :18081

# Windows
netstat -ano | findstr :18080
netstat -ano | findstr :18081
```

### 容器問題
```bash
# 清理所有容器和 image
docker compose down --rmi all

# 重新建置（不使用快取）
docker compose build --no-cache

# 查看詳細日誌
docker compose -f docker-compose.dev.yml logs -f --tail=100
```

### Maven 依賴問題
```bash
# 清理 Maven 快取 Volume
docker volume rm demo-api_maven-cache-api demo-api_maven-cache-excel

# 強制重新下載依賴
docker compose -f docker-compose.dev.yml up --build --force-recreate
```

### 熱重載不工作
1. 確認 VS Code 安裝了 `Extension Pack for Java`
2. 檢查自動儲存設定: `"files.autoSave": "afterDelay"`
3. 手動觸發編譯: `Ctrl+Shift+P` → `Java: Rebuild Projects`
4. 查看容器日誌確認 DevTools 是否偵測到變化

### 服務間通信問題
```bash
# 測試容器間網路連接
docker exec demo-api-dev ping demo-excel-dev

# 檢查網路設定
docker network ls
docker network inspect demo-api_spring-network
```

### 記憶體不足
```bash
# 增加 Docker Desktop 記憶體限制 (建議 4GB+)
# 或清理無用的容器和映像
docker system prune -a
```

## 常見使用情境

### 開發流程
1. 啟動開發環境: `docker compose -f docker-compose.dev.yml up -d --build`
2. 開啟 VS Code 並安裝 Java 擴展
3. 修改程式碼，自動重啟生效
4. 測試核心端點: `curl http://localhost:18080/excel/request-and-read?type=sample`
5. 停止環境: `docker compose -f docker-compose.dev.yml down`

### 生產部署
1. 建置生產映像: `docker compose build`
2. 啟動生產環境: `docker compose up -d`
3. 監控服務狀態: `docker compose logs -f`
4. 更新服務: `docker compose up -d --no-deps <service-name>`

## 效能建議

- **開發模式**: 首次啟動較慢（需下載依賴），後續啟動快速
- **生產模式**: 啟動快速，映像檔較小
- **Maven 快取**: 使用 Volume 快取可大幅減少重複下載時間
- **記憶體建議**: 至少分配 4GB 給 Docker Desktop
