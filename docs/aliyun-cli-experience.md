# 阿里云 CLI 访问经验沉淀（2026-09-14 会话）

> 基于真实操作会话整理：ECS 运维 / NAS 工具链复用 / 镜像构建 / 日志链路。
> AK/SK 一律不写入本文件，均存于本机 `~/.aliyun/`（CLI 凭据）与 `~/.aliyun/ecs.env`（chmod 600）。

---

## 一、访问环境与账号

| 项 | 值 | 说明 |
|---|---|---|
| CLI 版本 | 3.4.11 | `aliyun version` |
| 认证 profile | `default` | AK 前缀 `pRn`，勿回显/打印 AK/SK |
| 默认地域 | `cn-hangzhou` | 杭州 |
| ECS 命令参数 | `--RegionId cn-hangzhou` | 驼峰大写 |
| NAS/DataHub 命令参数 | `--region cn-hangzhou` | 小写（各产品不一，先 `aliyun help <product>` 确认） |
| DataWorks 命令参数 | `--biz-region-id cn-hangzhou` | 另有 `defaultProjectId=667265` |
| SLS 命令 | 必须带 `--user-agent "AlibabaCloud-Agent-Skills/alibabacloud-sls-query/<session-id>"` | 本地工具类命令除外 |
| SSH 登录 | `source ~/.aliyun/ecs.env; export SSHPASS="$ALIYUN_ECS_PASSWORD"; sshpass -e ssh root@<ip>` | 密码只存环境变量，不回显 |

**规则**：确认云资源状态一律用 CLI/skill，不用浏览器打开控制台；`aliyun help <product>` 是查参数的第一入口（不要猜参数名）。

---

## 二、账号内关键资源清单（2026-09-14 实测）

| 资源 | ID / 地址 | 备注 |
|---|---|---|
| 常用开发机 | i-bp10nfju0tqgxk6qffps（47.114.51.101，2C2G 包年） | swap 2GB + cursor-auto-stop 定时清理 |
| dev-env-test | ~~i-bp1ehtvf6to2lolekcsn~~（47.114.46.198，2C4G） | **已删除** |
| NAS 文件系统 | **0023doj3vvqliq954z0**（通用型·容量型·NFS） | 挂载点 `0023doj3vvqliq954z0-kxs4.cn-hangzhou.nas.aliyuncs.com:/`，VPC `vpc-bp1ws9puxo7j9bxhwacyg` |
| NAS 工具链 | `/mnt/nas/opt`（git2.49 + jdk17 + maven3.9.16 + node22，~679M） | 由 `docs/nas-toolchain-init.sh` 管理 |
| 自定义镜像 | **m-bp11q3hf6e67oqs7ingv**（dev-env-base-image，20GB，V7 成功） | 唯一镜像，持续计费约 ¥2.4/月 |
| 镜像构建模板 | **ip-bp1je7gi32umxdcv4ba2**（dev-env-pipeline-v7） | 唯一模板，v3~v6 已删 |
| SLS | 项目 `webframework-log-d654d2` / logstore `app-log` | detail 子字段 device/action 建 text 索引；traceid long+doc_value |
| DataHub | project `webframework_datahub` / topic `app_log`（BLOB） | endpoint dh-cn-hangzhou |
| DataWorks | defaultProjectId=667265 | 实时任务 LogHub→ODPS DF_ch_667265.app_log（ds 分区） |
| 安全组 | sg-bp1270jgw0a3s09ovc2z（nas-reuse-test，22 放行） | 复用可继续用 |
| VPC/交换机 | vpc-bp1ws9puxo7j9bxhwacyg；vsw-bp1ml95tnjvt21y5rdl51（k 区）等 3 个 | 建机同 VPC 才能内网挂 NAS |

---

## 三、常用命令速查（已验证可用写法）

### ECS
```bash
# 查实例
aliyun ecs DescribeInstances --RegionId cn-hangzhou --output cols=InstanceId,InstanceName,Status rows=Instances.Instance[]

# 创建按量实例（关键参数名！）
aliyun ecs RunInstances --RegionId cn-hangzhou \
  --ImageId aliyun_3_x64_20G_alibase_20260828.vhd \
  --InstanceType ecs.e-c1m2.large \
  --SecurityGroupId sg-bp1270jgw0a3s09ovc2z \
  --VSwitchId vsw-bp1ml95tnjvt21y5rdl51 \
  --SystemDisk.Category cloud_essd_entry --SystemDisk.Size 40 \
  --InternetMaxBandwidthOut 1 \
  --Password "$ALIYUN_ECS_PASSWORD" \
  --InstanceChargeType PostPaid \
  --Amount 1 --ClientToken "x-$(date +%s)"

# 删除实例：Running 状态必须 --Force true！
aliyun ecs DeleteInstance --RegionId cn-hangzhou --InstanceId <id> --Force true

# 查公共镜像（用 jq 过滤名称，--ImageOwnerAlias system/self）
aliyun ecs DescribeImages --RegionId cn-hangzhou --ImageOwnerAlias system --OSType linux --Architecture x86_64 --Status Available | jq -r '.Images.Image[] | select(.OSName|contains("Alibaba Cloud Linux 3")) | [.ImageId,.OSName] | @tsv'

# 镜像构建模板
aliyun ecs DescribeImagePipelines --RegionId cn-hangzhou --output cols=ImagePipelineId,Name rows=ImagePipeline.ImagePipelineSet[]
aliyun ecs DescribeImagePipelineExecutions --RegionId cn-hangzhou --output cols=ExecutionId,ImagePipelineId,Status rows=ImagePipelineExecution.ImagePipelineExecutionSet[]
aliyun ecs DeleteImagePipeline --RegionId cn-hangzhou --ImagePipelineId <id>
```

### NAS（注意：`--FileSystemId` 是大写驼峰！）
```bash
# 文件系统与计费容量（MeteredSize 异步更新，按小时）
aliyun nas DescribeFileSystems --region cn-hangzhou | jq '.FileSystems.FileSystem[0] | {FileSystemId, MeteredSize, StorageType}'

# 挂载点
aliyun nas DescribeMountTargets --region cn-hangzhou --FileSystemId <fsid> --output cols=MountTargetDomain,Status rows=MountTargets.MountTarget[]

# 回收站（默认开启，保留 3 天；清空 = 禁用并清空 → 重新开启）
aliyun nas GetRecycleBinAttribute --FileSystemId <fsid> --region cn-hangzhou
aliyun nas DisableAndCleanRecycleBin --FileSystemId <fsid> --region cn-hangzhou
aliyun nas EnableRecycleBin --FileSystemId <fsid> --ReservedDays 3 --region cn-hangzhou
```

### ECS 内挂载 NAS（fstab 持久化）
```bash
mkdir -p /mnt/nas
echo "<fsid>-kxs4.cn-hangzhou.nas.aliyuncs.com:/ /mnt/nas nfs vers=4.0,noresvport,_netdev 0 0" >> /etc/fstab
mount -a
# 验证：df -h | grep nas；mount | grep nfs（NAS 不是块设备，lsblk 看不到）
```

---

## 四、踩坑记录（血泪教训）

| # | 坑 | 正确姿势 |
|---|---|---|
| 1 | `RunInstances --SystemDiskCategory` 报参数错误 | 参数是 `--SystemDisk.Category` / `--SystemDisk.Size`（带点号） |
| 2 | `--InstanceChargeType PayAsYouGo` 报 NotFound | 按量枚举值是 **`PostPaid`**（包年包月 `PrePaid`） |
| 3 | Running 状态 `DeleteInstance` 报 IncorrectInstanceStatus | 加 **`--Force true`** |
| 4 | `DescribeMountTargets --file-system-id` 报参数错误 | NAS 参数是 **`--FileSystemId`**（大写驼峰） |
| 5 | `DescribeImages` 加 jq 过滤名称为空 | 先用 `head -c` 看原始 JSON 结构再写 jq 路径 |
| 6 | 镜像构建失败全在软件源网络（install-nodejs 超时 298s/130s、install-maven 404） | 下载一律走镜像：git→gh-proxy.com（shturl.cc/I5 不可达勿用）、JDK→清华 Adoptium、Maven→阿里云 apache（404 自动回退清华）、Node/npm→npmmirror |
| 7 | NAS MeteredSize 比 du 大 ~1.5x | 计费容量含回收站 + 元数据开销；小文件（git/node）放大明显；回收站默认开启 3 天，重装/覆盖文件产生残留 |
| 8 | 建机后 SSH 立即连不上 | Running ≠ sshd 就绪，cloud-init 首次初始化约需 30~35s，重试即可 |
| 9 | 以为"系统盘用 NAS" | NAS 是网络文件系统，**不能做系统盘**；系统盘必须云盘，NAS 只挂目录 |
| 10 | 找"实例挂了哪些 NAS" | 控制台无此关系图（NFS 无状态）；唯一权威 = 登录实例 `df -h \| grep nas` |

---

## 五、NAS 工具链复用方案（本次核心沉淀）

```
首次初始化（一次性）：bash docs/nas-toolchain-init.sh install   → ~3.5min（含下载装软件）
新机复用：             bash docs/nas-toolchain-init.sh reuse     → ~38s（零安装）
实测：创建 10s + SSH 35s + 挂载配置 3s；工具链 679M，NAS 容量型 ≈ ¥0.23/月
```

- 工具链目录：`/mnt/nas/opt/{git,jdk17,maven3,node}`；PATH 配置 `/etc/profile.d/dev-env.sh`
- 升级工具链 = 改脚本版本常量重跑 install，全部实例下次登录即用新版本
- NAS 内网写速实测 430MB/s，跑工具链足够；node_modules 海量小文件场景谨慎（NFS 小文件 IO 差）

---

## 六、待办/可选优化

- [ ] V7 镜像 m-bp11q3hf6e67oqs7ingv 若不再用于建机，可删除省 ¥2.4/月（连带释放源快照）
- [ ] `/mnt/nas/.write-test`（21B 测试残留）可删，无实际影响
- [ ] 安全组 sg-bp1270jgw0a3s09ovc2z 保留复用

## EIP 绑定/释放（2026-09-14 新增）

**坑：EIP_CAN_NOT_ASSOCIATE_WITH_PUBLIC_IP** —— 实例已有普通公网 IP（NatPublicIp）时不能直接绑 EIP，必须先释放普通公网 IP。

正确顺序（避免失联）：
```bash
# 1. 释放普通公网 IP（带宽峰值置 0 = 释放公网 IP，仅限按流量计费实例）
aliyun ecs ModifyInstanceNetworkSpec --RegionId cn-hangzhou \
  --InstanceId i-xxx --InternetMaxBandwidthOut 0
# 2. 立即绑定 EIP（中间仅几秒空窗）
aliyun vpc AssociateEipAddress --RegionId cn-hangzhou \
  --AllocationId eip-xxx --InstanceId i-xxx --InstanceType EcsInstance
# 3. 验证
aliyun vpc DescribeEipAddresses --RegionId cn-hangzhou --AllocationId eip-xxx
ssh root@<EIP>   # SSH 走新 EIP 验证
```

**注意**：ModifyInstanceNetworkSpec 无 --InternetChargeType 参数（CLI 3.4.11）；按流量实例带宽置 0 即释放公网 IP。

**当前 EIP 资源**：eip-bp1c8hnf12twltdjta1mr = 120.26.142.177（华东1，5M，PayByTraffic）→ 绑定抢占式 **i-bp18r2q6jfdvg0r6ev47**（e-c1m1.large）。

**计费要点**（2024-12-17 新规）：EIP 直绑 VPC ECS（配额≤2000）免保有费；流量费走 CDT（中国内地 20GB/月免费，超出 ¥0.80/GB 阶梯）。

---

## SLS 日志查询（2026-09-15 新增，已在 spring-demo 实测）

**验证 logstore 落库 / 查最近日志**：
```bash
# --line 是正确参数（不是 --size！），--query 写查询语句，--from/--to 为 Unix 秒
aliyun sls get-logs --project=webframework-log-d654d2 --logstore=app-log \
  --from=$(date -v-10M +%s) --to=$(date +%s) --query="*" --line=10
```

- CLI 3.4.11 的 sls 命令来自插件 `aliyun-cli-sls`（`aliyun sls --help` 可见）；
- 查询结果含 `__tag__:app/env/source`（组级 tag）、`__source__`、`__topic__`、字段内容；
- 本机直调可不带 `--user-agent`；skill 会话内调用才需要第 18 行那个 user-agent。

**SLS 写入集成（spring-demo）要点**：依赖 `com.aliyun.openservices:aliyun-log:0.6.163`；
凭证走环境变量 `ALIBABA_CLOUD_ACCESS_KEY_ID/SECRET`（兼容 STS `ALIBABA_CLOUD_SECURITY_TOKEN`）；
字段约定：顶层 `level/message/traceId(long)/device`，`detail` 为不建索引的 JSON。
详见 spring-demo 仓库 `docs/integration-experience.md` 第 4 节。

---

## ECI 镜像缓存（2026-09-15 新增，已在杭州 region 实测）

**查询镜像缓存**：
```bash
aliyun eci DescribeImageCaches --RegionId cn-hangzhou
# 输出 ImageCaches[]：ImageCacheId / ImageCacheName / Status / ImageCacheSize / CreationTime / Images
```

**删除镜像缓存**（按 ID，逐个删，返回 RequestId 即成功）：
```bash
aliyun eci DeleteImageCache --RegionId cn-hangzhou --ImageCacheId imc-xxxxxxxxx
```

**自动缓存堆积的原因**：workflow 每次 push 用新 commit SHA 当镜像 tag → ECI 自动匹配
（AutoMatchImageCache）匹配不到 → 每次自动新建 `auto-create-for-*` 缓存。5 次部署 = 5 个缓存。
清理后下次部署仍会新建，属正常行为；介意配额可定期批量清理（`DescribeImageCaches` 先列出 → 循环 `DeleteImageCache`）。

**计费要点（重要，2026-08 官方文档）**：
- 手动创建：创建时收「临时资源费（2vCPU/4GiB 实例+ESSD，按秒）+ 快照存储费（保留多久收多久）」；使用时按容量挂按量云盘收费。
- 自动创建（auto-create-for-*）：**创建免费**；**单个缓存 ≤30 GiB 使用免费**（>30 GiB 才对超出部分收临时存储费）；
  保留由阿里云托管（未用 7 天删 / 闲置超 30 天删），无长期快照费。
- 30 GiB 按**单个缓存**判断，不是所有缓存加总。
- 计费归属：实例费+临时存储费 → 弹性容器实例账单；云盘费+快照费 → 块存储账单。

**本机 AK 注入环境变量（本地验证 SLS 等，不打印密钥）**：
```bash
export ALIBABA_CLOUD_ACCESS_KEY_ID=$(python3 -c "import json;d=json.load(open('$HOME/.aliyun/config.json'));p=[x for x in d['profiles'] if x['name']=='default'][0];print(p['access_key_id'])")
export ALIBABA_CLOUD_ACCESS_KEY_SECRET=$(python3 -c "import json;d=json.load(open('$HOME/.aliyun/config.json'));p=[x for x in d['profiles'] if x['name']=='default'][0];print(p['access_key_secret'])")
# config.json 结构：{"current":"default","profiles":[{"name":"default","mode":"AK","access_key_id":...,"access_key_secret":...,"region_id":...}]}
```
> 规则：AK/SK 只进环境变量，不打印、不入文档/仓库。

---

## ECI 固定 EIP 绑定 + ACR workflow（2026-09-15 新增，spring-demo 实测）

**核心结论：ECI 创建时指定已有 EIP，ECI 释放后 EIP 不释放、回到 Available**（只有 `--auto-create-eip true` 自动创建的 EIP 才随实例释放）。

```bash
# 1. 查询 EIP（注意 vpc 插件参数是 --biz-region-id，不是 --RegionId）
aliyun vpc describe-eip-addresses --biz-region-id cn-hangzhou
# → TotalCount / EipAddresses.EipAddress[]: IpAddress, Status(Available/InUse), AllocationId, InstanceId

# 2. 创建 ECI 时绑定已有 EIP（替代 --auto-create-eip true + --eip-bandwidth/--eip-isp）
aliyun eci create-container-group \
  --biz-region-id cn-hangzhou \
  --container-group-name spring-demo-eci-acr \
  --compute-category economy \
  --container '[...]' \
  --vswitch-id vsw-xxx --security-group-id sg-xxx \
  --eip-instance-id eip-bp1c8hnf12twltdjta1mr \
  --restart-policy OnFailure

# 3. 删 ECI 后 EIP 解绑有延迟，连续部署需先等它回 Available（最长 ~90s）
#    aliyun vpc describe-eip-addresses --allocation-id <id> | jq -r '.EipAddresses.EipAddress[0].Status' == "Available"

# 4. 安全组规则查询（ecs 插件参数 --biz-region-id；8080 入方向 0.0.0.0/0 Accept 才能公网访问）
aliyun ecs describe-security-group-attribute --biz-region-id cn-hangzhou --security-group-id sg-xxx \
  | jq -r '.Permissions.Permission[] | "\(.Direction) \(.IpProtocol) \(.PortRange) \(.SourceCidrIp) \(.Policy)"'
```

**ECI economy（经济型）规格红线**：1 vCPU 只支持 2/4/8 GiB 内存——请求 1C0.5G 直接报
`FeatureBasedConstraintConflict: [vCPU ComputeCategory]`（400）。且 `--container` JSON 里
`Memory` 单位是 **GiB**（不是 MB），写 512.0 = 512 GiB，会向上规整成超大规格（¥110 账单元凶）。

**ACR 个人版（crpi-* 端点）要点**：
- buildx 推送必须 `provenance: false` + `sbom: false`，否则报
  `unknown manifest class for application/vnd.oci.empty.v1+json`；
- 镜像仓库不支持推送时自动创建，需先在控制台建好（命名空间/仓库，私有）；
- 仓库是否存在的校验（走 docker registry token 流，凭据=ACR 账号密码）：
  ```bash
  TOKEN=$(curl -s -u "<user>:<pass>" \
    "https://dockerauth.cn-hangzhou.aliyuncs.com/auth?service=registry.aliyuncs.com:cn-hangzhou:<id>&scope=repository:<ns>/<repo>:pull" \
    | jq -r .token)
  curl -s -H "Authorization: Bearer $TOKEN" \
    "https://crpi-xxx.cn-hangzhou.personal.cr.aliyuncs.com/v2/<ns>/<repo>/tags/list"
  # 200=存在；{"errors":[{"code":"NAME_UNKNOWN"}]}=仓库不存在
  ```
- WWW-Authenticate 头里的 realm/service 才是真 token 端点（`curl -sI https://<registry>/v2/` 查看）。

**GitHub Actions 相关（gh CLI 已用 PAT 重新认证）**：
```bash
gh secret set ACR_USERNAME --repo <owner>/<repo> --body "..."          # 设置 secret
printf '%s' "$PASS" | gh secret set ACR_PASSWORD --repo <owner>/<repo> # 密码走 stdin，不进进程参数
gh workflow run deploy-eci-acr.yml --repo <owner>/<repo> --ref dev     # 指定分支手动触发
gh run view <id> --repo <owner>/<repo> --log-failed | tail -40         # 看失败步骤日志
gh run list --repo <owner>/<repo> --workflow deploy-eci.yml -L 3 --json databaseId,status,conclusion
```

**冒烟测试教训**：curl 一次性探测 + `|| true` 会把"没连上"掩盖成成功（历史所有 run 都是 HTTP=000 假绿）。
正确写法：重试直到两个端点都返回 HTTP 200（最多 ~3 分钟），超时 `exit 1` 让步骤真实失败。

**当前 EIP 用途更新**：`eip-bp1c8hnf12twltdjta1mr`（120.26.142.177 / eip-dev-5m，5M 按量）
已改为 spring-demo ECI workflow 的固定公网 IP（创建 ECI 时绑定，跑完自动解绑回 Available）；
此前绑定的抢占式 ECS i-bp18r2q6jfdvg0r6ev47 已不占用该 EIP。

## 2026-09-15 MongoDB Atlas 连接失败根因：Network Access 白名单（重要）

- 现象：MongoDB Atlas 集群（cluster0.o1bvw8t，香港 AWS 159.143.x.x）从本机/杭州 ECS/杭州 ECI/新加坡 ECS 全部 TCP 通、TLS 握手失败 `TLSV1_ALERT_INTERNAL_ERROR`（alert 80），一度误判为"大陆→香港 AWS 被干扰"。
- 真相：**Atlas 免费集群默认 Network Access 白名单不含测试出口 IP，Atlas 在 TLS 握手层直接丢 alert 拒绝**。三渠道一致失败 = 出口 IP 都不在白名单。
- 解法：Atlas 控制台 → Network Access → Add IP Address 加 `0.0.0.0/0`，保存后**无需等**，本机/杭州 ECS/新加坡 ECS 三渠道同时恢复（TLSv1.3 + ping + insert/readback/cleanup 全过）。
- 经验：
  1. Atlas TLS alert 80（internal error）优先查 Network Access 白名单，别先怀疑网络/干扰。
  2. 跨云验证用 mongosh（官方 shell，tar 解压即用）：`downloads.mongodb.com/compass/mongosh-2.4.2-linux-x64.tgz`，比装 pymongo/venv 快。
  3. 新加坡临时验证机（t6-c1m1.large 抢占式 0.03）：创建 → 经杭州中转 SSH → 验证完 Stop→Stopped→DeleteInstance（不加 --Force），全程 <10 分钟，花费 <0.1 元。
  4. 账号余额不足（InvalidAccountStatus.NotEnoughBalance）时按量/抢占式都下单失败，充值后立即恢复，无需改参数。
