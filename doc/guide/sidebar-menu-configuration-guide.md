# 側邊欄導航選單 (Sidebar Menu) Function Configurable 維護手冊
(Sidebar Menu Dynamic Navigation Configuration Guide)

---

## 一、架構背景與設計理念

在傳統的前端專案中，側邊欄導航選單（Sidebar Navigation）往往以寫死（Hard-coded）的靜態陣列存於前端代碼中。當系統導入多租戶（Multi-Tenancy）或 SaaS 模式時，此種做法會面臨巨大挑戰：
1. **無法針對租戶開關功能**：無法為基礎版租戶隱藏高階功能，亦無法針對特定客戶開放專屬自訂模組。
2. **改動成本高**：調整選單名稱、排列順序或更換圖示皆需重新發布前端版本。
3. **資料表臃腫**：若為選單另建獨立資料表，會造成後端維運與遷移成本上升。

### 為什麼採用 Function Configurable？
本模組貫徹 **Function Configurable** 核心精神——**「不為單一業務類型另闢專用表，善用通用多維坐標體系 (`SettingScope`) 進行配置」**：
- **維度元資料字典 (`SettingCategory`)**：統一註冊維度 `MENU`（側邊欄選單配置）。
- **通用設定主體 (`Setting`)**：以二級子分類 `subCategory` 作為選單分組，以 `value` 承載擴充屬性（圖示、徽章、視圖路由、外部連結）。

---

## 二、SettingScope 四維坐標與資料規格

$$\text{SettingScope} = (\text{tenant}, \text{category}, \text{subCategory}, \text{code})$$

### 2.1 坐標定義與欄位對應表

| 坐標 / 欄位 | 系統欄位 | 說明與填寫規範 | 範例值 |
| :--- | :--- | :--- | :--- |
| **`tenant`** | `scope.tenant` | 歸屬之企業租戶代碼（完全隔離，各租戶獨立定義）。 | `CW` 或 `ENTERPRISE_A` |
| **`category`** | `scope.category` | 固定填入 **`MENU`**（對應元資料字典之大分類）。 | `MENU` |
| **`subCategory`** | `scope.subCategory` | **選單所屬群組名稱**。前端自動依此欄位二級聚合。 | `核心業務配置` 或 `系統與安全維運` |
| **`code`** | `scope.code` | **選單唯一標識代碼**（同一租戶下不可重複）。 | `MENU_SETTING`, `MENU_API_DOCS` |
| **`name`** | `profile.name` | **選單於側邊欄之顯示名稱**。 | `功能設定管理`, `API 規格文件` |
| **`description`**| `profile.description` | 輔助說明或業務備註。 | `核心業務設定項目增刪改查管理` |
| **`value`** | `setting.value` | **選單擴充屬性 (JSON 字串)**，詳見下方說明。 | `{"icon":"pi pi-sliders-h","badge":"核心","view":"setting"}` |
| **`priorityNo`** | `setting.priorityNo` | **在該分組內的顯示排序**（數字越小排越前）。 | `1`, `2`, `3` |
| **`activeFlag`** | `setting.activeFlag` | **有效性狀態**：`'Y'` 顯示於側邊欄；`'N'` 隱藏。 | `'Y'` 或 `'N'` |

---

### 2.2 `value` 欄位 JSON 屬性定義

`value` 欄位採用標準 JSON 字串儲存前端渲染所需的擴充屬性：

```json
{
  "icon": "pi pi-sliders-h",
  "badge": "核心",
  "view": "setting",
  "url": "/swagger-ui.html"
}
```

- **`icon`** *(string, 推薦)*：PrimeIcons 圖示 Class 代碼（例如 `pi pi-sliders-h`、`pi pi-database`、`pi pi-building`、`pi pi-shield`、`pi pi-file-edit`、`pi pi-book`）。若未填寫預設顯示 `pi pi-folder`。
- **`badge`** *(string, 可選)*：右側小徽章標籤文字（例如 `核心`、`元資料`、`New`、`Beta`）。
- **`view`** *(string, 可選)*：內部路由/視圖標識（例如 `setting` 切換至功能設定管理；`category` 切換至元資料字典）。
- **`url`** *(string, 可選)*：外部跳轉鏈接（例如 `/swagger-ui.html`），點擊後在新分頁開啟。

---

## 三、預設基準選單清單 (Seed Data)

系統預設在 `SettingDataInitializer` 為 `CW` 租戶預載以下 6 個標準選單項目：

```text
📁 核心業務配置 (subCategory = "核心業務配置")
  ├── 1. [MENU_SETTING]  功能設定管理 (icon: pi-sliders-h, badge: 核心, view: setting)
  ├── 2. [MENU_CATEGORY] 元資料字典   (icon: pi-database, badge: 元資料, view: category)
  └── 3. [MENU_TENANT]   租戶隔離中心 (icon: pi-building)

📁 系統與安全維運 (subCategory = "系統與安全維運")
  ├── 1. [MENU_ROLE]     系統權限角色 (icon: pi-shield)
  ├── 2. [MENU_AUDIT]    操作稽核日誌 (icon: pi-file-edit)
  └── 3. [MENU_API_DOCS] API 規格文件 (icon: pi-book, url: /swagger-ui.html)
```

---

## 四、維護作業手冊 (How-To Guide)

### 途徑一：透過側邊欄 UI 直接「切換拖拉排序」（最推薦、直觀）

系統已於側邊欄頂部提供視覺化拖拉排序切換開關，操作步驟如下：

1. **點擊「切換拖拉排序」**：
   - 位於側邊欄頂部 Logo 與選單列表之間。
   - 點擊後進入排序模式，按鈕轉為翠綠色高亮 **`[✓ 完成選單排序]`** 並附帶「拖曳中」動態標籤。
   - 若側邊欄為收合狀態，點擊後系統會自動展開以便於檢視完整文字。
2. **在群組內拖曳項目**：
   - 進入排序模式後，每個選單項目左側浮現 `pi-bars` 拖曳把手，游標呈現抓取狀態 (`grab / grabbing`)。
   - 在所屬分組（如「核心業務配置」）內按住滑鼠左鍵上下拖曳。
   - 拖曳至目標位置時，目標項目會即時浮現藍色插入指示線。
3. **放開滑鼠即時儲存**：
   - 放開滑鼠後，前端自動重算該群組之 `priorityNo`（`1, 2, 3...`）。
   - 前端即時調用 `PUT /api/v1/settings/menus/sort` 完成資料庫持久化，並彈出成功提示。
   - 排序模式下自動**停用點擊跳轉**，避免拖曳時意外觸發頁面切換。
4. **結束排序**：
   - 再次點擊頂部 **`[✓ 完成選單排序]`**，選單即恢復為正常導航模式。

---

### 途徑二：透過「功能設定管理 (UI)」表格維護

1. **進入設定管理頁面**：點擊側邊欄「功能設定管理」。
2. **篩選 MENU 分類**：在上方搜尋列將「維度大分類 (Category)」下拉選單選擇為 **`MENU (側邊欄選單配置)`**，點擊「執行查詢」。
3. **新增自訂選單**：
   - 點擊「+ 新增設定項目」。
   - **Category**：選擇 `MENU`。
   - **SubCategory (群組名稱)**：輸入或選擇既有群組（如 `核心業務配置`），亦可輸入全新群組名稱（系統將自動產生新分組區塊）。
   - **Code**：輸入唯一代碼，例如 `MENU_REPORT`。
   - **Name**：輸入選單標題，例如 `財務報表中心`。
   - **Value**：填入 JSON 擴充資訊，例如 `{"icon":"pi pi-chart-bar","badge":"PRO"}`。
   - **Priority No**：填入排列順序號（如 `4`）。
   - 點擊「儲存」，側邊欄將**即時呼叫 API 刷新並呈現新選單**！
4. **隱藏/停用特定選單**：
   - 點擊該列「編輯」，將「有效性狀態」調整為 `停用 (N)` 並儲存，該選單便會立即自該租戶側邊欄隱藏。
5. **手動調整順序號**：
   - 編輯目標項目，將 `priorityNo` 改為較小數字即可向上排列。

---

### 途徑三：透過 RESTful API 進行自動化維護

#### 1. 批次排序側邊欄選單項目 (專用排序端點)
```http
PUT /api/v1/settings/menus/sort
Content-Type: application/json
X-Tenant-Id: CW

[
  { "id": 10, "priorityNo": 1 },
  { "id": 9,  "priorityNo": 2 },
  { "id": 11, "priorityNo": 3 }
]
```
> **提示**：若未帶 `priorityNo`，後端將自動依照傳入陣列的先後順序賦予 `1, 2, 3...`。回傳資料**保證依 `priorityNo` 升冪排序**。

#### 2. 查詢指定租戶已啟用之側邊欄選單
```http
GET /api/v1/settings/menus?tenant=CW
```

**回應範例：**
```json
[
  {
    "id": 10,
    "tenant": "CW",
    "group": "核心業務配置",
    "code": "MENU_CATEGORY",
    "name": "元資料字典",
    "description": "維度大分類字典維護",
    "value": "{\"icon\":\"pi pi-database\",\"badge\":\"元資料\",\"view\":\"category\"}",
    "priorityNo": 1,
    "activeFlag": "Y"
  },
  {
    "id": 9,
    "tenant": "CW",
    "group": "核心業務配置",
    "code": "MENU_SETTING",
    "name": "功能設定管理",
    "description": "核心業務設定項目增刪改查管理",
    "value": "{\"icon\":\"pi pi-sliders-h\",\"badge\":\"核心\",\"view\":\"setting\"}",
    "priorityNo": 2,
    "activeFlag": "Y"
  }
]
```

#### 3. 為新租戶建立專屬選單
```bash
curl -X POST http://localhost:8080/api/v1/settings \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: ENTERPRISE_B" \
  -d '{
    "tenant": "ENTERPRISE_B",
    "category": "MENU",
    "subCategory": "核心業務配置",
    "code": "MENU_SETTING",
    "name": "功能設定管理",
    "description": "設定維護",
    "value": "{\"icon\":\"pi pi-sliders-h\",\"badge\":\"核心\",\"view\":\"setting\"}",
    "priorityNo": 1
  }'
```

---

## 五、常見問題與注意事項 (FAQ)

### Q1：如果新增了全新的 `subCategory`，側邊欄會如何處理？
**答**：前端 `MenuService` 採自動二級分組演算法（`transformToMenuGroups`），只要出現新的 `subCategory` 字串，側邊欄就會自動長出該群組標題區塊，並將該群組下之項目依 `priorityNo` 自動排列，無需修改前端任何程式碼！

### Q2：租戶切換時選單會即時聯動嗎？
**答**：會！`SidebarComponent` 訂閱了 `TenantService.tenant$`。當使用者切換租戶時，系統會立即向後端呼叫 `GET /api/v1/settings/menus?tenant={newTenant}`，確保不同租戶呈現各自被授權的客製選單。

### Q3：`value` 欄位若填入非 JSON 格式字串會出錯嗎？
**答**：不會。前端 `MenuService` 具備容錯解析（`try...catch`）保護機制。若解析非 JSON，且字串以 `pi ` 開頭（如 `pi pi-user`），系統會自動將其識別為圖示 Class，其餘屬性安全降級為預設值。
