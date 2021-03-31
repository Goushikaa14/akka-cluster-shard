package com.example.akka.actor;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.scaladsl.AkkaManagement;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component("akkaManager")
public class ActorManager {
    public static final String SHARDING_STATE_STORE_MODE = "persistence";
    public static final String SHARDING_SNAPSHOT_PLUGIN = "pg-snapshot";
    public static final String SHARDING_JOURNAL_PLUGIN = "pg-journal";
    public static final boolean enableRememberEntities = true;
    public static final String EMPLOYEE_ACTOR = "employeeActor";


    @Autowired
    private EmployeeActor employeeActor;

    @Getter
    private ActorSystem actorSystem;

    @PostConstruct
    public void postConstruct() {
        log.error("Initializing actor manager");
        actorSystem = ActorSystem.create("employee-actor");

        Cluster cluster = Cluster.get(actorSystem);

        log.info("Started [" + actorSystem + "], cluster.selfAddress = " + cluster.selfAddress() + ")");

        AkkaManagement.get(actorSystem).start();
        ClusterBootstrap.get(actorSystem).start();


        cluster.subscribe(actorSystem.actorOf(Props.create(ClusterWatcher.class)), ClusterEvent.initialStateAsEvents(),
                ClusterEvent.ClusterDomainEvent.class);

        cluster.registerOnMemberUp(() -> log.info("Cluster member is up!"));

        ClusterShardingSettings settings = clusterShardSettings();
        ClusterSharding.get(actorSystem)
                .start(EMPLOYEE_ACTOR,
                        Props.create(EmployeePersistenceActor.class, employeeActor),
                        settings,
                        EmployeeActor.messageExtractor);

    }

    private ClusterShardingSettings clusterShardSettings() {
        ClusterShardingSettings settings = ClusterShardingSettings.create(actorSystem)
                .withRememberEntities(enableRememberEntities);
        settings = validateAndSetStateStoreSetting(settings);
        return settings;
    }

    private ClusterShardingSettings validateAndSetStateStoreSetting(ClusterShardingSettings settings) {
        if (enableRememberEntities) {
            settings = settings.withJournalPluginId(SHARDING_JOURNAL_PLUGIN)
                    .withSnapshotPluginId(SHARDING_SNAPSHOT_PLUGIN)
                    .withStateStoreMode(SHARDING_STATE_STORE_MODE);
        }
        return settings;
    }

    public ActorRef shardingRegion() {

        return ClusterSharding.get(actorSystem).shardRegion(EMPLOYEE_ACTOR);
    }
}
