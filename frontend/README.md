# Setting Module Frontend (Angular 18 + PrimeNG 18)

本專案為 **Setting Module** 的前端管理介面，基於 **Angular 18** 與 **PrimeNG 18** (TypeScript, SCSS)，提供直觀便捷的視覺化操作平台。

---

## 🚀 核心功能特色

1. **多租戶動態切換 (X-Tenant-Id)**：
   - 頂部導航列支援即時切換當前租戶代碼（例如：`ALL` 全域共用、`default`、`tenant-01` 等）。
   - 切換租戶時，自動更新 HTTP 請求標頭 `X-Tenant-Id`，並即時重整選單與資料。
2. **元資料驅動階層選項 (Metadata Cascade)**：
   - 第一層：業務大維度 (Category) 下拉選單（呼叫 `/api/v1/settings/categories`）。
   - 第二層：業務子分類 (SubCategory) 二級聯動選單（呼叫 `/api/v1/settings/categories/{cat}/sub-categories`）。
3. **動態多條件 Specification 分頁查詢**：
   - 支援 Category, SubCategory, Code, Name 模糊搜尋與 ActiveFlag 狀態組合過濾。
   - 伺服器端分頁 (Lazy Loading Paginator)，每頁筆數支援 10 / 20 / 50 筆切換。
4. **設定項目 CRUD 對話框**：
   - 彈出式對話框 (`p-dialog`) 進行設定新增與編輯。
   - 刪除採用二階段確認機制 (`p-confirmDialog`)，支援軟刪除（狀態標記為停用）。
   - 操作成功或失敗均提供即時 Toast 浮水印通知 (`p-toast`)。

---

## 🛠️ 開發與啟動步驟

### 1. 安裝依賴套件
```bash
npm install
```

### 2. 啟動本機開發伺服器
```bash
npm start
```
- 前端網址：[http://localhost:4200](http://localhost:4200)
- 本專案已配置 `proxy.conf.json`，所有 `/api/*` 請求會自動代理轉發至後端服務 [http://localhost:8080](http://localhost:8080)。

---

## 📦 技術棧
- **Angular**: 18.2.x (Standalone Components)
- **PrimeNG**: 18.x (Aura Theme Preset)
- **PrimeIcons**: 7.x
- **TypeScript**: 5.5.x

---

## 🏛️ 前端架構規範 (遵照 LIFT 準則)

```text
src/app/
├── core/                         # 核心層 (全系統唯一單例、App Shell、無關業務)
│   ├── interceptors/
│   │   └── tenant.interceptor.ts # 自動注入 X-Tenant-Id 標頭
│   ├── services/
│   │   └── tenant.service.ts     # 全域單例租戶狀態管理 (BehaviorSubject)
│   ├── models/
│   │   ├── page.model.ts         # 全域基礎設施泛型模型 PagedResult<T>
│   │   └── tenant.model.ts       # 租戶切換選項模型
│   └── components/               # App Shell 應用外殼元件 (Layout, Header, Footer)
│       ├── layout/
│       ├── header/
│       └── footer/
├── features/                     # 功能層 (依業務領域垂直切分)
│   └── setting/                  # Setting 業務領域切片
│       ├── models/
│       │   └── setting.model.ts  # Setting 領域專屬 Model
│       ├── services/
│       │   └── setting.service.ts# Setting 領域專屬 API 服務
│       └── components/
│           └── setting-management/# Smart Component (完整拆分 .ts, .html, .scss)
├── app.component.ts              # 根元件 (僅作為外殼容器引入 Layout 與 Feature)
├── app.component.html
├── app.component.scss
└── app.config.ts                 # 根配置 (PrimeNG Aura, HttpClient withInterceptors)
```
