## 秒杀agent测试集：

1、线上有正在进行的秒杀活动吗？

期望：（1）agent必须先调用/ops/metrics，防止不调工具，编纂答案

​	    （2）判断答案与activities里面的status=RUNNING是否存在一致，防止读错数据

来源：基础能力

2、现在MQ有没有积压？

期望：调用/ops/metrics，看queueDepth和deadLetterDepth，报出有没有积压和数量

来源：基础能力

3、活动到现在卖出了多少单？

期望：调用/ops/metrics，报dbOrderCount的值

来源：基础能力

4、为什么这个活动的redisStock会返回null

期望：（1）先看Redis.up：false→Redis连通性问题

​	    （2）是否所有活动都是null，是→该活动没有进行库存的预热或者key拼写错误

​	    （3）单个null→活动未预热或者key被删

来源：真实事故：在某次测试时发现Redis里面的活动值都是null