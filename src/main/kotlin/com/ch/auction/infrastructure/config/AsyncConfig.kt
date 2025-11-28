package com.ch.auction.infrastructure.config

import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.core.task.TaskDecorator
import org.springframework.core.task.support.TaskExecutorAdapter
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executors

@Configuration
@EnableAsync
class AsyncConfig {

    @Bean(name = ["taskExecutor"])
    fun taskExecutor(): AsyncTaskExecutor {
        val executor = TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor())
        executor.setTaskDecorator(MdcTaskDecorator())
        return executor
    }

    class MdcTaskDecorator : TaskDecorator {
        override fun decorate(runnable: Runnable): Runnable {
            val contextMap = MDC.getCopyOfContextMap()
            return Runnable {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap)
                }
                try {
                    runnable.run()
                } finally {
                    MDC.clear()
                }
            }
        }
    }
}
