
package mano.util;

/**
 * 定义一个由{@link ThreadPool}自动调度的短时任务。
 * @author jun
 */

@FunctionalInterface
public interface ScheduleTask {
    /**
     * 执行该任务。
     * 注意：该方法内禁止长时间的阻塞。
     * @param millis 调度时的时间，毫秒。
     * @return {@code true} 则从调度器中移除该任务，否则将在下次调度时再次执行。
     * 
     */
    boolean execute(long millis);
    
    /**
     * 注册一个任务到调度上下文中。
     * @param task 任务。
     * @throws NullPointerException 参数 task 不能为空。
     * @throws IllegalArgumentException 该任务已经在调度上下文中。
     */
    public static void register(ScheduleTask task) throws NullPointerException,IllegalArgumentException{
        if(task==null){
            throw new NullPointerException("task");
        }
        else if(ThreadPool.scheduledTasks.contains(task)){
            throw new IllegalArgumentException("This task already exists in scheduled's context.");
        }
        ThreadPool.scheduledTasks.add(task);
    }
}
