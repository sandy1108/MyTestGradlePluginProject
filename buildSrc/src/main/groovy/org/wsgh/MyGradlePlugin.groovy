package org.wsgh

import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * Created by zhangyipeng on 2018/1/24.
 */

public class MyGradlePlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        createTestTask(project)
    }
    private void createTestTask2(Project project){
        def task2 = project.tasks.create("task2")
        task2.doLast {
            println("===============>task2")
        }
        def task1 = project.tasks.create("task1")
        task1.doLast {
            println("===============>task1")
        }
        def task3 = project.tasks.create("task3")
        task3.doLast {
            println("===============>task3")
        }
        def task = project.tasks.create("testMain")
        task.doLast {
            println("===============>taskMainLastDo")
        }
        task.doLast {
            println '====>!!!!!!taskMain'
        }
        task.doFirst {
            println("===============>taskMainFirstDo")
        }
        task.doLast(task1)
        task.dependsOn(task2,task3)
        task2.mustRunAfter(task3)

    }
    private void createTestTask(Project project){
        def task2 = project.tasks.create("task2")
        task2.doLast {
            println("===============>task2")
        }
        def task1 = project.tasks.create("task1")
        task1.doLast {
            println("===============>task1")
        }
        def task3 = project.tasks.create("task3")
        task3.doLast {
            println("===============>task3")
        }
        def task = project.tasks.create("testMain")
        task.doLast {
            println("===============>taskMainLastDo")
        }
        task.doFirst {
            println("===============>taskMainFirstDo")
        }
        task.dependsOn(task1,task2,task3)
        task2.mustRunAfter(task3)
    }
}
