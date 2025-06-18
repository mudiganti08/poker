package com.poker.scheduler;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.poker.service.BackupService;

@Component
public class BackupScheduler {

    @Autowired
    private BackupService backupService;

    // Run every 5 minutes
    @Scheduled(fixedRate = 1 * 60 * 1000)
    public void runBackupTask() {
        System.out.println("🕒 Starting scheduled CSV backup...");
        backupService.exportSummaryCSV();
    }
}

