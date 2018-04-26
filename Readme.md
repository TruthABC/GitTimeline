# 毕业设计项目
* 主题：软件项目工作区与演化可视化
* Topic: A Visualization for Software Project Workspace and Evolution

### 开发者日志
* 20180320 - 进度：建立项目GitTimeline
* 20180420 - 进度：改变开发方向
* 20180420 - 进度：添加若干Serializable对象，用于将仓库及api调用频率内存信息序列化
* 20180423 - 注：“程序异常终止原则”，新约定：“文件非只读操作”例如delete与mkdir默认100%成功，不允许失败，如失败则终止程序 - System.exit(1)。
* 20180423 - 注：“程序异常终止原则”，新约定：与网络有关的操作例如clone，可以失败，失败后不终止程序。
* 20180423 - 注：局限性：不允许项目同重名。（原因：项目缓存目录与文件名直接取了项目名）
* 20180423 - 注：局限性懒人方案：引入新的项目，与已有项目重名，会被当做项目已经被缓存。则每次先清除缓存可以避免。
* 20180423 - 进度：ProjectManager开发完成，场景说明：载入已有项目（无网络），新建项目（有网络），载入但更新已有项目（有网络）。
* 20180423 - 进度：APICountingManager开发开始
* 20180424 - 进度：更改了Project(Serializable)，增加pureName属性。
* 20180424 - 进度：初步完成并调试了CountingManager
* 20180424 - 进度：安卓官方API数据半脱离数据库
* 20180424 - 注：APICountingManager更新缓存规则尝试(TODO)：新工作的commit序号为二的幂时缓存。
* 20180424 - 进度：尝试CaseSample测试
* 20180425 - 进度：调试、重构；确保了算法正确性；提供了验证算法正确的相关类（ProjectAPICounterNaive VS. CaseSampleAPICountingManagerUsage）
* 20180426 - 注：以上确保正确性的算法，对同样的MAP排序可能排出不一样的序
* 20180426 - 进度：引入SpringBoot、WebSocket依赖，开发后端backend包
* 20180426 - 注：WebSocket采用了广播模式（作为不支持多人同时使用工具标志之一）
* 20180426 - 进度：开始开发后台service + controller，操作与前端对接


### ProjectManager说明
* 用于项目的克隆；project与commit数据的提取、缓存；
* 构造方法：通过提供项目在线git仓库的url，形如"https://github.com/MrDoomy/Torch"，如有缓存则载入
* 公共方法(1/2) - initByUrl(projectUrl)：希望作用于其他项目时，重新构造并初始化
* 公共方法(2/2) - downloadProject()：将项目下载到本地（如有项目或者解析API的缓存则全部自动删除）
* Getter - getProjectCache()：得到项目的详细缓存，内容详见serializable.Project & Commit
* Getter - isProjectCached()：当前项目是否有缓存，是否被缓存过

### APICountingManager说明
* 用于项目每次Commit的API计数的解析，解析粒度为单次Commit
* 构造方法：通过传入serializable.Project对象构造，如有缓存则载入（故需要使用到到ProjectManager）
* 公共方法(1/5) - initByProject(project)：希望作用于其他项目时，重新构造并初始化
* 公共方法(2/5) - deleteCountingCache()：如有API解析缓存则删除
* 公共方法(3/5) - saveCountingCache()：如解析有新进度，将API解析写入缓存，覆盖旧缓存
* 公共方法(4/5) - hasNextCommit()：是否还有下一个Commit没有进行解析
* 公共方法(5/5) - analyseNextCommit()：如有下一个Commit没有进行解析，则解析下一个Commit（并返回单次Commit解析结果）
* Setter - setShowDetail(int)：0不打印细节（默认0） or 1显示细节
* Getter - getProject()：构造APICountingManager所用的Project对象
* Getter - getCountingCache()：得到项目的详细缓存，内容详见serializable.APICounters & APICounter
* Getter - getCountingNow()：Commit解析进度，0则进度为零，且说明缓存状态为无
* Getter - getNewCountingCount()：内存即时信息，新进行的解析次数（用于判断是否有新的解析进展）
* Getter - getCheckoutFault()：内存即时信息，解析过程中checkout的失败次数
* Getter - getCountAPIFault()：内存即时信息，解析过程中API技术过程的出错次数（例如总的编译不通过java文件数）

### 研究进展
* 20180320 - 
``` text
Github扩展了Git版本管理的功能，体现为event，即事件的扩展。
Git中仅有branch分支，commit提交，merge合并等与项目文件本身有关的版本信息。
GitHub中含有额外的event事件信息，包括issue问题，pull_request拉取请求。

当前计划：首先针对Git中的信息进行可视化，不依赖GitHub的API，不考虑event事件相关信息。
分主题：Git项目活跃度可视化。
分主题：Git项目用户参与可视化。（除去pull request merge贡献方式）
分主题：Git项目不同版本文件可视化索引。

实现：web应用，输入可视化Git仓库地址，自动进行解析后展示结果。
解析过程由web应用调用后台服务，基于后台mysql数据库服务与文件存储服务完成。

进阶：对GitHub信息（主要是event，基于Git，扩展后的信息）同时进行可视化考虑。
```
* 20180420 -
```text
优化曦源项目API可视化代码，实现效率与渲染方式的双重优化

问题：

1、定位问题的关键：数据库操作太慢，没有数据库操作可以加速90%。
解决方案:脱离数据库，使用序列化与文件系统完成数据存储。
2、坚持web应用：仅web前端无法完成分析，但是桌面前端不方便进行可视化。
3、接下来可能的问题：实时分析返回前端数据时，担心数据体量过大。
4、提取API数据的正确性验证（文件系统下）。
5、最后再考虑commit词云和commit信息的对接。

总结：
	废弃数据库，改用文件系统序列化数据
	实时分析并返回数据给前端，前端管理数据
	前端最好做到解析过程实时渲染

如何使用：

1、前端输入项目地址，后端根据状态缓存仓库并分析API信息，生成序列化后的信息缓存文件（如有API信息文件则可以直接使用，如没有则首先查看是否可以使用仓库缓存）
（
	(1)未缓存仓库，且未分析（没有仓库版本信息文件，没有API信息文件）（初始态）；
	(2)已缓存仓库完成，但分析未完成（有仓库版本信息文件，没有API信息文件）；
	*(3)分析已完成，但仓库缓存已删除（没有仓库版本信息文件，有API信息文件）；
	(4)已缓存仓库完成，且分析已完成（有仓库版本信息文件，有API信息文件）；
）；
2、前端输入项目地址，要求依据已有仓库缓存，重新分析（清空API数据缓存，并首先查看是否可以使用仓库缓存）；
3、前端输入项目地址，要求更新已有仓库缓存，重新分析（一律重置所有缓存）；

后端相关：
后端分析API，文件系统缓存到本地，传回数据，前端实时排序渲染；（TODO：暂时不支持多人同时调用服务）
后端内存中保留数据，使用状态机（初始：输入地址前；点击按钮解析中；解析已完成（前端展示完成）；前端变更API筛选返回数据）
```