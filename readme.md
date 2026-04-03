
> 注：当前项目为 Serverless Devs 应用，由于应用中会存在需要初始化才可运行的变量（例如应用部署地区、函数名等等），所以**不推荐**直接 Clone 本仓库到本地进行部署或直接复制 s.yaml 使用，**强烈推荐**通过 `s init ${模版名称}` 的方法或应用中心进行初始化，详情可参考[部署 & 体验](#部署--体验) 。

# start-springboot-cap 帮助文档

<description>

本案例展示了如何将 Spring Boot，一款高效的 Web 框架，部署至云原生应用开发平台。SpringBoot其“习惯优于配置”的理念、快速开发能力和微服务架构支持，广泛应用于 Web 应用程序开发、微服务架构、批处理和数据处理等场景中。

</description>


## 资源准备

使用该项目，您需要有开通以下服务并拥有对应权限：

<service>



| 服务/业务 |  权限  | 相关文档 |
| --- |  --- | --- |
| 函数计算 |  AliyunFCFullAccess | [帮助文档](https://help.aliyun.com/product/2508973.html) [计费文档](https://help.aliyun.com/document_detail/2512928.html) |
| 日志服务 |  AliyunFCServerlessDevsRolePolicy | [帮助文档](https://help.aliyun.com/zh/sls) [计费文档](https://help.aliyun.com/zh/sls/product-overview/billing) |
| 对象存储 |  AliyunOSSFullAccess | [帮助文档](https://help.aliyun.com/zh/oss) [计费文档](https://help.aliyun.com/zh/oss/product-overview/billing) |

</service>

<remark>



</remark>

<disclaimers>



</disclaimers>

## 部署 & 体验

<appcenter>
   
- :fire: 通过 [云原生应用开发平台 CAP](https://cap.console.aliyun.com/template-detail?template=start-springboot-cap) ，[![Deploy with Severless Devs](https://img.alicdn.com/imgextra/i1/O1CN01w5RFbX1v45s8TIXPz_!!6000000006118-55-tps-95-28.svg)](https://cap.console.aliyun.com/template-detail?template=start-springboot-cap) 该应用。
   
</appcenter>
<deploy>
    
   
</deploy>

## 案例介绍

<appdetail id="flushContent">

本案例是基于 Pivotal 团队提供的全新的 Spring Boot 框架，简化 Spring 应用的初始化搭建过程，并且快速部署到云原生应用开发平台 CAP。

Spring Boot 是一个快速开发 Spring 框架应用的脚手架，它使用“习惯优于配置”（约定优于配置）的理念让你的项目快速运行起来。Spring Boot 并不是对 Spring 功能上的增强，而是提供了一种快速使用 Spring 的方式。

Spring Boot的流行程度非常高，主要得益于其快速开发、微服务架构支持、易于管理和部署、兼容性强以及社区支持等特点。Spring Boot天然支持微服务架构的特点使其成为了构建微服务的理想选择。通过Spring Boot，开发者可以轻松地将应用程序拆分成多个可独立部署和升级的小型服务，充分利用计算资源，提高系统的可扩展性和可维护性。

Spring Boot因其快速开发、微服务架构支持、易于管理和部署、兼容性强以及社区支持等特点而备受欢迎，成为了当前最流行的Java Web开发框架之一。

Spring Boot适用的场景也非常广泛，如：Web应用程序开发、微服务架构、批处理和数据处理等。无论是简单的Web应用还是复杂的分布式系统，Spring Boot都能提供高效、稳定的开发支持，是Java开发者在构建企业级应用时的首选框架之一。

通过云原生应用开发平台 CAP，您只需要几步，就可以体验 Spring Boot 框架，并享受 Serverless 架构带来的降本提效的技术红利。

</appdetail>







## 使用流程

<usedetail id="flushContent">

部署完成之后，您可以看到系统返回给您的案例地址

此时，打开案例地址，就可以进入，如下图：

![图片alt](https://img.alicdn.com/imgextra/i4/O1CN01jjYG5l1vm4BYqIt2K_!!6000000006214-0-tps-1308-708.jpg)

</usedetail>

## 二次开发指南

<development id="flushContent">

本项目可以用于二次开发。

初始化项目时，需要绑定代码仓库，CAP平台会自动配置代码仓库的Webhook。当仓库对应的分支有任何提交时，CAP平台会收到Webhook推送，并自动完成构建与部署。

代码中演示了进行前、后端开发以及访问云产品，用户可以仿造演示代码开发任何自己想要的功能。

</development>

## Supabase 集成

项目已内置 Supabase Java SDK 集成（`com.harium.supabase:core`），可通过配置快速接入。

### 1. 配置

在 `src/main/resources/application.properties` 中设置：

```properties
supabase.url=https://your-project-ref.supabase.co
supabase.anon-key=your-anon-key
supabase.service-role-key=your-service-role-key
```

说明：

- `supabase.service-role-key` 与 `supabase.anon-key` 二选一即可；若都设置，优先使用 `service-role-key`。
- 生产环境建议通过环境变量或密钥管理系统注入，避免明文写入代码仓库。

### 2. 接口

- `GET /supabase/health`：检查 SDK 初始化与连接配置状态。
- `GET /supabase/table/{table}?select=*&limit=10`：通过 SDK 查询表数据。

### 3. Book 表 CRUD 示例

假设 Supabase 中存在 `book` 表（例如字段：`id`、`title`、`author`、`price`）。

- 查询列表：`GET /supabase/book?limit=10`
- 查询单条：`GET /supabase/book/{id}`
- 创建：`POST /supabase/book`
- 更新：`PUT /supabase/book/{id}`
- 删除：`DELETE /supabase/book/{id}`

示例请求：

```bash
curl -X POST http://localhost:9000/supabase/book \
	-H "Content-Type: application/json" \
	-d '{"title":"Spring In Action","author":"Craig Walls","price":88.0}'

curl http://localhost:9000/supabase/book?limit=10

curl http://localhost:9000/supabase/book/1

curl -X PUT http://localhost:9000/supabase/book/1 \
	-H "Content-Type: application/json" \
	-d '{"title":"Spring Boot Upgraded","price":99.0}'

curl -X DELETE http://localhost:9000/supabase/book/1
```

## Supabase Postgres 直连 CRUD（MyBatis）

项目已新增基于 MyBatis 的 `book` 表 CRUD，接口前缀：`/db/book`。

### 1. 环境变量

启动前建议设置：

```bash
export SUPABASE_DB_URL='jdbc:postgresql://db.qkfndglocfuzsjxlqkpl.supabase.co:5432/postgres?sslmode=require'
export SUPABASE_DB_USER='postgres'
export SUPABASE_DB_PASSWORD='YOUR-PASSWORD'
```

### 2. 接口

- `GET /db/book`：查询全部
- `GET /db/book/{id}`：按 id 查询
- `POST /db/book`：新增
- `PUT /db/book/{id}`：更新
- `DELETE /db/book/{id}`：删除

示例：

```bash
curl -X POST http://localhost:9000/db/book \
	-H "Content-Type: application/json" \
	-d '{"title":"Designing Data-Intensive Applications","author":"Martin Kleppmann","price":120.0}'

curl http://localhost:9000/db/book

curl http://localhost:9000/db/book/1

curl -X PUT http://localhost:9000/db/book/1 \
	-H "Content-Type: application/json" \
	-d '{"price":99.0}'

curl -X DELETE http://localhost:9000/db/book/1
```







