package dev.sbs.updater;

import dev.sbs.api.SimplifiedApi;
import dev.sbs.api.client.hypixel.request.HypixelResourceRequest;
import dev.sbs.updater.processor.resource.ResourceCollectionsProcessor;
import dev.sbs.updater.processor.resource.ResourceItemsProcessor;
import dev.sbs.updater.processor.resource.ResourceSkillsProcessor;
import dev.sbs.updater.util.UpdaterConfig;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class DatabaseUpdater {

    private static final HypixelResourceRequest HYPIXEL_RESOURCE_REQUEST = SimplifiedApi.getWebApi(HypixelResourceRequest.class);
    @Getter private final Logger log;

    public DatabaseUpdater() {
        UpdaterConfig config;
        try {
            File currentDir = new File(SimplifiedApi.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            config = new UpdaterConfig(currentDir.getParentFile(), "updater");
        } catch (Exception exception) {
            throw new IllegalArgumentException("Unable to retrieve current directory", exception); // Should never get here
        }

        this.log = LogManager.getLogger(this);
        this.getLog().info("Connecting to Database");
        SimplifiedApi.getSessionManager().connectSql(config);
        this.getLog().info("Database Initialized in {}ms", SimplifiedApi.getSessionManager().getSession().getInitializationTime());
        this.getLog().info("Database Cached in {}ms", SimplifiedApi.getSessionManager().getSession().getStartupTime());

        this.getLog().info("Loading Processors");
        ResourceItemsProcessor itemsProcessor = new ResourceItemsProcessor(HYPIXEL_RESOURCE_REQUEST.getItems());
        ResourceSkillsProcessor skillsProcessor = new ResourceSkillsProcessor(HYPIXEL_RESOURCE_REQUEST.getSkills());
        ResourceCollectionsProcessor collectionsProcessor = new ResourceCollectionsProcessor(HYPIXEL_RESOURCE_REQUEST.getCollections());

        try {
            this.getLog().info("Processing Items");
            itemsProcessor.process();
            this.getLog().info("Processing Skills");
            skillsProcessor.process();
            this.getLog().info("Processing Collections");
            collectionsProcessor.process();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        new DatabaseUpdater();
    }

}
