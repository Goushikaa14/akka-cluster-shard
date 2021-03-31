package com.example.akka.controller;

import com.example.akka.actor.ActorManager;
import com.example.akka.message.Messages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final ActorManager actorManager;

    @PostMapping(consumes = "application/json")
    public void createEmployee(@RequestBody EmployeeData employeeData) {
        log.info("Create Employee");
        Messages.EmployeeCommand employeeCommand = Messages.EmployeeCommand.newBuilder().setId(employeeData.getId())
                .setName(employeeData.getName()).setDesignation(employeeData.getDesignation()).build();
        actorManager.shardingRegion().tell(employeeCommand, null);
    }


}
