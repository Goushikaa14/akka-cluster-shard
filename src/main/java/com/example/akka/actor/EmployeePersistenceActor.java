package com.example.akka.actor;

import akka.persistence.AbstractPersistentActorWithTimers;
import akka.persistence.SaveSnapshotFailure;
import akka.persistence.SaveSnapshotSuccess;
import akka.persistence.SnapshotOffer;
import com.example.akka.message.Messages;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class EmployeePersistenceActor extends AbstractPersistentActorWithTimers {

    private final AtomicReference<Messages.Employee> checkPoint = new AtomicReference<>();

    private Messages.Employee employee;

    @Override
    public Receive createReceiveRecover() {
        return receiveBuilder()
                .match(SnapshotOffer.class, ss -> {
                    log.info("Recovering the actor {} from last captured snapshot", self().path().name());
                    this.employee = (Messages.Employee) ss.snapshot();
                })
                .build();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SaveSnapshotFailure.class, sf -> {
                    log.error("Save snapshot failed");
                })
                .match(SaveSnapshotSuccess.class, saveSnapshotSuccess -> {
                    log.info("Save snapshot success");
                })
                .match(Messages.EmployeeCommand.class, employeeCommand -> {
                    employee = Messages.Employee.newBuilder()
                            .setId(employeeCommand.getId())
                            .setName(employeeCommand.getName())
                            .setDesignation(employeeCommand.getDesignation())
                            .build();
                    saveSnapshot(employee);
                })
                .build();
    }

    @Override
    public String persistenceId() {
        return self().path().name();
    }
}
