package com.srs.rental.kafka.producer;

import com.srs.common.kafka.BaseKafkaProducer;
import com.srs.common.kafka.KafkaTopic;
import com.srs.common.kafka.message.rental.LeaseApprovedKafkaMessage;
import com.srs.rental.entity.ApplicationEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


@Component
@Log4j2
public class LeaseKafkaProducer extends BaseKafkaProducer {

    public LeaseKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void notifyLeaseBeingApproved(ApplicationEntity lease) {
        var message = new LeaseApprovedKafkaMessage();
        message.setMarketCode(lease.getMarketCode());
        message.setFloorCode(lease.getFloorCode());
        message.setStallCode(lease.getStallCode());
        message.setApplicationId(lease.getApplicationId());
        this.sendThenLogResult(KafkaTopic.LEASE_APPROVAL, message, log);
    }

}
