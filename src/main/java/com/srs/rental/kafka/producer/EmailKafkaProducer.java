package com.srs.rental.kafka.producer;

import com.srs.common.kafka.BaseKafkaProducer;
import com.srs.common.kafka.message.AppStatusUpdatedEmailKafkaMessage;
import com.srs.common.kafka.message.core.email.EmailDelegatedService;
import com.srs.market.MarketType;
import com.srs.rental.WorkflowStatus;
import com.srs.rental.entity.ApplicationEntity;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.srs.common.kafka.KafkaTopic.EMAIL_FLOW;
import static com.srs.rental.ApplicationType.NEW_STALL_APP_VALUE;
import static com.srs.rental.ApplicationType.RENEWAL_STALL_APP_VALUE;


@Component
@Log4j2
public class EmailKafkaProducer extends BaseKafkaProducer {


    public EmailKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void sendPaymentInformationRequestedEmail(ApplicationEntity application,
                                                     String comment) {
        this.sendApplicationStatusUpdatedEmail(application, comment);
    }

    private void sendApplicationStatusUpdatedEmail(ApplicationEntity application, String comment) {
        var message = new AppStatusUpdatedEmailKafkaMessage();

        message.setDelegateTo(EmailDelegatedService.ACCOUNT);

        message.setToEmail(application.getOwner().getEmail());
        message.setMarketCode(application.getMarketCode());
        message.setFirstName(application.getOwner().getFirstName());
        message.setLastName(application.getOwner().getLastName());
        message.setApplicationType(this.getApplicationTypeString(application.getType()));
        message.setApplicationTypeNumber(application.getType());
        message.setApplicationNumber(application.getCode());
        message.setComment(comment);
        message.setMenuSection("City-Owned Market");
        message.setApplicationStatus(this.getApplicationStatusString(application.getStatus()));
        message.setApplicationStatusNumber(application.getStatus());
        message.setMarketTypeNumber(MarketType.MARKET_TYPE_PUBLIC_VALUE);

        this.sendThenLogResult(EMAIL_FLOW, message, log);
    }

    private String getApplicationTypeString(int type) {
        switch (type) {
            case NEW_STALL_APP_VALUE:
                return "New Stall Application";
            case RENEWAL_STALL_APP_VALUE:
                return "Renewal of Stall Application";
            default:
                return "";
        }
    }

    private String getApplicationStatusString(int status) {
        switch (status) {
            case WorkflowStatus.PAYMENT_INFO_REQUESTED_VALUE:
                return "Payment Information Requested";
            case WorkflowStatus.APPROVED_VALUE:
                return "Approved";
            case WorkflowStatus.DISAPPROVED_VALUE:
                return "Disapproved";
            default:
                return "";
        }
    }

}
