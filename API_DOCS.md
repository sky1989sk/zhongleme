## 接口概览

- **双色球基础接口**
  - `GET /api/ssq`：分页获取双色球开奖记录列表
  - `GET /api/ssq/{period}`：按期号获取单期双色球开奖详情
  - `GET /api/ssq/latest`：获取双色球最新一期
  - `POST /api/ssq/refresh`：增量刷新双色球历史数据
  - `POST /api/ssq/backfill-all`：全量回填双色球历史数据

- **大乐透基础接口**
  - `GET /api/dlt`：分页获取大乐透开奖记录列表
  - `GET /api/dlt/{period}`：按期号获取单期大乐透开奖详情
  - `GET /api/dlt/latest`：获取大乐透最新一期
  - `POST /api/dlt/refresh`：增量刷新大乐透历史数据

- **分析接口**
  - `GET /api/analysis/ssq/hot`：双色球热门红球
  - `GET /api/analysis/ssq/cold`：双色球冷门红球
  - `GET /api/analysis/ssq/blue-hot`：双色球热门蓝球
  - `GET /api/analysis/ssq/statistics`：双色球总体统计信息
  - `GET /api/analysis/dlt/hot`：大乐透热门号码
  - `GET /api/analysis/dlt/cold`：大乐透冷门号码
  - `GET /api/analysis/dlt/statistics`：大乐透总体统计信息

- **新增：中奖查询接口**
  - `POST /api/check-ticket`：根据彩种、期号和多注号码，判断是否中奖及奖级、金额（示意）。

---

## 中奖查询接口

### 请求

- **方法**：`POST`
- **路径**：`/api/check-ticket`
- **请求体（JSON）**：

```json
{
  "lottery_type": "ssq",
  "period": "2026026",
  "ssq_tickets": [
    {
      "red_balls": [2, 9, 16, 22, 25, 29],
      "blue_ball": 3,
      "multiple": 1
    },
    {
      "red_balls": [1, 2, 3, 4, 5, 6],
      "blue_ball": 7,
      "multiple": 2
    }
  ]
}
```

> 对于大乐透，将 `lottery_type` 设置为 `"dlt"`，并使用 `dlt_tickets` 字段：

```json
{
  "lottery_type": "dlt",
  "period": "23150",
  "dlt_tickets": [
    {
      "front_balls": [1, 2, 3, 4, 5],
      "back_balls": [2, 8],
      "multiple": 1
    }
  ]
}
```

### 响应

```json
{
  "lottery_type": "ssq",
  "period": "2026026",
  "results": [
    {
      "index": 1,
      "is_win": true,
      "level": "一等奖",
      "amount": 7000000,
      "hit_red": 6,
      "hit_blue": 1,
      "remark": "金额为示意值，实际以官方公告为准"
    },
    {
      "index": 2,
      "is_win": false,
      "level": null,
      "amount": null,
      "hit_red": 3,
      "hit_blue": 0,
      "remark": null
    }
  ]
}
```

### 错误返回

- 期号不存在：

```json
{
  "detail": "双色球该期号不存在"
}
```

- 未提供对应类型的号码列表：

```json
{
  "detail": "请提供 ssq_tickets 列表"
}
```

---

## 使用最佳实践

- **推荐调用顺序**：
  1. 调用 `GET /api/ssq/latest` 或 `GET /api/dlt/latest` 获取最新期号；
  2. 前端根据期号和开奖时间展示给用户；
  3. 用户录入多注号码（结构化为红/蓝或前区/后区 + 倍数）；
  4. 统一调用 `POST /api/check-ticket`，一次性返回所有注的命中情况。

- **结合分析接口**：
  - 可提前调用 `/api/analysis/*` 系列接口，展示热门/冷门号码，引导用户选号；
  - 中奖查询本身是纯计算逻辑，不依赖外部网站接口，可高频调用。

- **关于金额字段**：
  - 当前实现中的 `amount` 使用规则表中的**示意金额**乘以倍数计算；
  - 实际奖金受销售额、奖池、当期中奖注数等影响，请以官方公告为准；
  - 若你后续在数据库中引入官方开奖的奖级明细，可在 `prize_checker.py` 中替换为真实金额计算逻辑。

---

## 简单测试用例示例

下面是后端单元测试中可使用的基本场景（伪代码，与你的测试框架保持一致即可）：

```python
from app.models import SSQRecord, DLTRecord
from app.services.prize_checker import (
    check_ssq_tickets,
    check_dlt_tickets,
    SSQTicket,
    DLTTicket,
)


def test_check_ssq_tickets_basic():
    draw = SSQRecord(
        period="2026001",
        red_balls=[1, 2, 3, 4, 5, 6],
        blue_ball=7,
        open_date=None,
        sales_amount=None,
        prize_pool=None,
    )

    tickets = [
        SSQTicket(red_balls=[1, 2, 3, 4, 5, 6], blue_ball=7, multiple=1),
        SSQTicket(red_balls=[1, 2, 3, 4, 5, 6], blue_ball=8, multiple=1),
        SSQTicket(red_balls=[1, 2, 3, 10, 11, 12], blue_ball=7, multiple=2),
    ]

    results = check_ssq_tickets(draw, tickets)

    assert results[0].is_win and results[0].level == "一等奖"
    assert results[1].is_win and results[1].level == "二等奖"
    assert results[2].is_win and results[2].multiple == 2


def test_check_dlt_tickets_basic():
    draw = DLTRecord(
        period="23150",
        front_balls=[1, 2, 3, 4, 5],
        back_balls=[6, 7],
        open_date=None,
        sales_amount=None,
        prize_pool=None,
    )

    tickets = [
        DLTTicket(front_balls=[1, 2, 3, 4, 5], back_balls=[6, 7], multiple=1),
        DLTTicket(front_balls=[1, 2, 3, 4, 5], back_balls=[6, 8], multiple=1),
    ]

    results = check_dlt_tickets(draw, tickets)

    assert results[0].is_win and results[0].level == "一等奖"
    assert results[1].is_win and results[1].level in {"二等奖", "三等奖", "四等奖", "五等奖", "六等奖", "七等奖"}
```

