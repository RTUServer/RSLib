package com.github.ipecter.rtuserver.lib.util.schedule;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class QuartzUtil {

    public static void addCronTask(String name, String cron, Class<? extends Job> job) {
        JobDetail detail = JobBuilder.newJob(job).usingJobData(new JobDataMap()).build();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("rslib", name)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .startNow()
                .build();
        try {
            StdSchedulerFactory.getDefaultScheduler().scheduleJob(detail, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static void removeCronTask(String name) {
        try {
            StdSchedulerFactory.getDefaultScheduler().unscheduleJob(TriggerKey.triggerKey("rslib", name));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public static String getNextFireTime(String name) {
        try {
            Trigger trigger = StdSchedulerFactory.getDefaultScheduler().getTrigger(new TriggerKey(name));
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextFireTime = trigger.getNextFireTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            Duration duration = Duration.between(now, nextFireTime);

            long days = duration.toDays();
            long hours = duration.toHours() - days * 24;
            long minutes = duration.toMinutes() - days * 24 * 60 - hours * 60;
            return String.format("%d시간 %d분", hours, minutes);
        } catch (SchedulerException e) {
            return "0시간 0분";
        }
    }
}
