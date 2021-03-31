package com.example.akka.actor;

import akka.cluster.sharding.ShardRegion;
import akka.japi.Creator;
import com.example.akka.message.Messages.EmployeeCommand;
import org.springframework.stereotype.Component;


@Component
public class EmployeeActor implements Creator<EmployeePersistenceActor> {

    public static final int NUMBER_OF_SHARDS = 100;
    private static final long serialVersionUID = 1234567L;

    static ShardRegion.MessageExtractor messageExtractor = new ShardRegion.MessageExtractor() {
        @Override
        public String entityId(Object message) {
            if (message instanceof EmployeeCommand) {
                return ((EmployeeCommand) message).getId();
            } else
                return null;
        }

        @Override
        public Object entityMessage(Object message) {
            return message;
        }

        @Override
        public String shardId(Object message) {
            if (message instanceof EmployeeCommand) {
                return getShardIdFromEntityId(((EmployeeCommand) message).getId());
            } else if (message instanceof ShardRegion.StartEntity) {
                return getShardIdFromEntityId(((ShardRegion.StartEntity) message).entityId());
            } else {
                return null;
            }
        }

        private String getShardIdFromEntityId(String entityId) {
            int shardId = entityId.hashCode() % NUMBER_OF_SHARDS;
            return String.valueOf(shardId);
        }
    };


    @Override
    public EmployeePersistenceActor create() {
        return new EmployeePersistenceActor();
    }
}
